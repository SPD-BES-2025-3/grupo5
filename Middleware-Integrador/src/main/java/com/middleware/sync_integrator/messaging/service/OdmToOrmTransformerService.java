package com.middleware.sync_integrator.messaging.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OdmToOrmTransformerService {

    // Repositórios para ler do ODM
    private final AgendamentoFlexODMRepository agendamentoFlexODMRepository;

    // Repositórios para ler e escrever no ORM
    private final AgendamentoRepository agendamentoOrmRepository;
    private final ClienteRepository clienteOrmRepository;
    private final ProfissionalRepository profissionalOrmRepository;
    private final ServicoRepository servicoOrmRepository;

    public OdmToOrmTransformerService(AgendamentoFlexODMRepository agendamentoFlexODMRepository, AgendamentoRepository agendamentoOrmRepository, ClienteRepository clienteOrmRepository, ProfissionalRepository profissionalOrmRepository, ServicoRepository servicoOrmRepository) {
        this.agendamentoFlexODMRepository = agendamentoFlexODMRepository;
        this.agendamentoOrmRepository = agendamentoOrmRepository;
        this.clienteOrmRepository = clienteOrmRepository;
        this.profissionalOrmRepository = profissionalOrmRepository;
        this.servicoOrmRepository = servicoOrmRepository;
    }

    /**
     * Orquestra o processo de normalização de um documento ODM para entidades ORM.
     */
    @Transactional // A transação garante que todas as escritas no PostgreSQL sejam atômicas
    public void normalizarESalvar(String documentoId) {
        System.out.println("Iniciando normalização para o Documento ID: " + documentoId);

        // 1. Busca o documento de origem no MongoDB
        var doc = agendamentoFlexODMRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento de agendamento flexível " + documentoId + " não encontrado."));

        // 2. Busca as entidades correspondentes no PostgreSQL (a parte de "lookup")
        var cliente = clienteOrmRepository.findById(doc.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + doc.getClienteId()+ " não encontrado no banco relacional."));

        var profissional = profissionalOrmRepository.findByEmail(doc.getProfissionalEmail())
                .orElseThrow(() -> new EntityNotFoundException("Profissional com email " + doc.getProfissionalEmail() + " não encontrado."));

        List<Servico> servicos = servicoOrmRepository.findByNomeIn(doc.getServicosNomes());
        if (servicos.size() != doc.getServicosNomes().size()) {
            throw new EntityNotFoundException("Um ou mais serviços (" + doc.getServicosNomes() + ") não foram encontrados no banco relacional.");
        }

        // 3. Constrói as novas entidades ORM
        var novoAgendamento = new Agendamento();
        novoAgendamento.setCliente(cliente);
        novoAgendamento.setProfissional(profissional);
        novoAgendamento.setHorarioInicio(doc.getDataHora());
        novoAgendamento.setStatus(StatusAgendamento.AGENDADO); // Define o status inicial

        // 4. Calcula os campos derivados (duração, valor, horário de fim)
        int duracaoTotal = servicos.stream().mapToInt(Servico::getDuracao).sum();
        BigDecimal valorTotal = servicos.stream().map(Servico::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        novoAgendamento.setHorarioFim(doc.getDataHora().plusMinutes(duracaoTotal));
        novoAgendamento.setValorTotal(valorTotal);

        // 5. Cria as entidades da tabela de associação (ServicoAgendado)
        Set<ServicoAgendado> servicosAgendados = new HashSet<>();
        for (Servico servico : servicos) {
            var sa = new ServicoAgendado();
            sa.setAgendamento(novoAgendamento);
            sa.setServico(servico);
            sa.setValor(servico.getValor()); // "Congela" o preço do serviço
            servicosAgendados.add(sa);
        }
        novoAgendamento.setServicosAgendados(servicosAgendados);

        // 6. Salva o novo Agendamento no PostgreSQL (o Cascade salvará os ServicoAgendado)
        agendamentoOrmRepository.save(novoAgendamento);
        System.out.println("Agendamento normalizado e salvo no PostgreSQL com sucesso.");

        // 7. (Opcional, mas recomendado) Atualiza o status do documento no MongoDB
        doc.setStatus(("CONCLUIDO"));
        agendamentoFlexODMRepository.save(doc);
    }
}