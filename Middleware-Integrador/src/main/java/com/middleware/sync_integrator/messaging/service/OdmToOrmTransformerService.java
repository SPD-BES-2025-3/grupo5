package com.middleware.sync_integrator.messaging.service;

import com.middleware.sync_integrator.messaging.config.RabbitMQConfig;
import com.middleware.sync_integrator.messaging.event.AgendamentoOrmSalvoEventDTO;
import com.middleware.sync_integrator.odm.entities.AgendamentoFlexODM;
import com.middleware.sync_integrator.odm.repositories.AgendamentoFlexODMRepository;
import com.middleware.sync_integrator.odm.repositories.AgendamentoODMRespository;
import com.middleware.sync_integrator.orm.entities.Agendamento;
import com.middleware.sync_integrator.orm.entities.Servico;
import com.middleware.sync_integrator.orm.entities.ServicoAgendado;
import com.middleware.sync_integrator.orm.entities.enums.StatusAgendamento;
import com.middleware.sync_integrator.orm.repositories.AgendamentoRepository;
import com.middleware.sync_integrator.orm.repositories.ClienteRepository;
import com.middleware.sync_integrator.orm.repositories.ProfissionalRepository;
import com.middleware.sync_integrator.orm.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OdmToOrmTransformerService {

    // Repositórios
    private final AgendamentoFlexODMRepository agendamentoOdmRepository;
    private final AgendamentoRepository agendamentoOrmRepository;
    private final ClienteRepository clienteOrmRepository;
    private final ProfissionalRepository profissionalOrmRepository;
    private final ServicoRepository servicoOrmRepository;

    // RabbitTemplate para atuar como publisher
    private final RabbitTemplate rabbitTemplate;

    public OdmToOrmTransformerService(
            AgendamentoFlexODMRepository agendamentoOdmRepository,
            AgendamentoRepository agendamentoOrmRepository,
            ClienteRepository clienteOrmRepository,
            ProfissionalRepository profissionalOrmRepository,
            ServicoRepository servicoOrmRepository,
            RabbitTemplate rabbitTemplate) {
        this.agendamentoOdmRepository = agendamentoOdmRepository;
        this.agendamentoOrmRepository = agendamentoOrmRepository;
        this.clienteOrmRepository = clienteOrmRepository;
        this.profissionalOrmRepository = profissionalOrmRepository;
        this.servicoOrmRepository = servicoOrmRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Orquestra o processo completo: normaliza, salva no ORM e publica o evento de volta.
     */
    public void normalizarESalvar(String documentoId) {
        System.out.println("Iniciando normalização para o Documento ID: " + documentoId);

        var doc = agendamentoOdmRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento de agendamento flexível " + documentoId + " não encontrado."));

        try {
            // A lógica de normalização agora retorna o agendamento salvo
            Agendamento agendamentoSalvo = executarLogicaDeNormalizacao(doc);

            // Se tudo deu certo, publica o evento para o fluxo ORM -> ODM para fechar o ciclo
            publicarEventoDeConfirmacaoOrm(agendamentoSalvo);

            doc.setStatus("SINCRONIZADO_OK");
            doc.setStatus(null);

        } catch (Exception e) {
            System.err.println("!!! ERRO DE NEGÓCIO ao normalizar doc ID " + documentoId + ": " + e.getMessage());
            doc.setStatus("SINCRONIZACAO_FALHOU");
            doc.setStatus(e.getMessage());
        } finally {
            // Salva o estado final do documento no MongoDB, seja sucesso ou falha
            agendamentoOdmRepository.save(doc);
        }
    }

    @Transactional
    public Agendamento executarLogicaDeNormalizacao(AgendamentoFlexODM doc) {
        // 1. Busca as entidades referenciadas no PostgreSQL
        var cliente = clienteOrmRepository.findById(doc.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + doc.getClienteId() + " não foi encontrado no banco relacional."));

        var profissional = profissionalOrmRepository.findByEmail(doc.getProfissionalEmail())
                .orElseThrow(() -> new EntityNotFoundException("Profissional com email " + doc.getProfissionalEmail() + " não foi encontrado."));

        List<Servico> servicos = servicoOrmRepository.findByNomeIn(doc.getServicosNomes());
        if (servicos.size() != doc.getServicosNomes().size()) {
            throw new EntityNotFoundException("Um ou mais serviços (" + doc.getServicosNomes() + ") não foram encontrados no banco relacional.");
        }

        // 2. Calcula os campos derivados
        int duracaoTotal = servicos.stream().mapToInt(Servico::getDuracao).sum();
        BigDecimal valorTotal = servicos.stream().map(Servico::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Constrói a entidade Agendamento principal com todos os atributos
        var novoAgendamento = new Agendamento();
        novoAgendamento.setCliente(cliente);
        novoAgendamento.setProfissional(profissional);
        novoAgendamento.setHorarioInicio(doc.getDataHora());
        novoAgendamento.setHorarioFim(doc.getDataHora().plusMinutes(duracaoTotal));
        novoAgendamento.setValorTotal(valorTotal);
        novoAgendamento.setStatus(StatusAgendamento.AGENDADO);

        // 4. Constrói as entidades da tabela de associação (ServicoAgendado)
        Set<ServicoAgendado> servicosAgendados = servicos.stream().map(servico -> {
            var sa = new ServicoAgendado();
            sa.setAgendamento(novoAgendamento);
            sa.setServico(servico);
            sa.setValor(servico.getValor()); // "Congela" o preço do serviço no momento do agendamento
            return sa;
        }).collect(Collectors.toSet());

        novoAgendamento.setServicosAgendados(servicosAgendados);

        // 5. Salva o novo Agendamento no PostgreSQL.
        // Graças ao CascadeType.ALL, os ServicoAgendado serão salvos automaticamente.
        System.out.println("Salvando agendamento normalizado no PostgreSQL...");
        return agendamentoOrmRepository.save(novoAgendamento);
    }

    /**
     * Publica o evento de confirmação após a escrita no ORM ser bem-sucedida.
     * Isso fecha o ciclo de sincronização, disparando o fluxo ORM -> ODM.
     */
    private void publicarEventoDeConfirmacaoOrm(Agendamento agendamento) {
        var evento = new AgendamentoOrmSalvoEventDTO(agendamento.getId(), "CRIADO");
        System.out.println(">>> Middleware publicando evento de confirmação ORM. Agendamento ID: " + agendamento.getId());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORM_EVENTS_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_AGENDAMENTO_SALVO_ORM,
                evento
        );
    }
}