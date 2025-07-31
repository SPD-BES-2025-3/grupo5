package com.bookingbarber.sys.odm.service;

import com.bookingbarber.sys.messaging.event.AgendamentoOdmSalvoEventDTO;
import com.bookingbarber.sys.messaging.publisher.AgendamentoOdmPublisher;
import com.bookingbarber.sys.odm.dto.AgendamentoFlexOdmDTO;
import com.bookingbarber.sys.odm.dto.AgendamentoOdmRequestDTO;
import com.bookingbarber.sys.odm.dto.AgendamentoOdmResponseDTO;
import com.bookingbarber.sys.odm.dto.ServicoOdmDTO;
import com.bookingbarber.sys.odm.entities.AgendamentoFlexODM;
import com.bookingbarber.sys.odm.entities.AgendamentoODM;
import com.bookingbarber.sys.odm.repositories.AgendamentoFlexODMRepository;
import com.bookingbarber.sys.odm.repositories.AgendamentoODMRespository;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;


@Service
public class AgendamentoODMService {

    private final AgendamentoFlexODMRepository agendamentoDocumentoRepository;

    private final AgendamentoODMRespository agendamentoODMRespository;

    private final ApplicationEventPublisher eventPublisher; // INJETAR O PUBLICADOR DE EVENTOS DO SPRING

    public AgendamentoODMService(
            AgendamentoFlexODMRepository agendamentoDocumentoRepository,
            AgendamentoODMRespository agendamentoODMRespository,
            ApplicationEventPublisher eventPublisher) {
        this.agendamentoDocumentoRepository = agendamentoDocumentoRepository;
        this.agendamentoODMRespository = agendamentoODMRespository;
        this.eventPublisher = eventPublisher;
    }
    /**
     * Recebe os dados, cria um documento no MongoDB e publica um evento interno do Spring.
     */
    @Transactional
    public AgendamentoFlexODM criarAgendamentoFlexivel(AgendamentoFlexOdmDTO request) {
        // 1. Validações de Negócio
        if (request.clienteId() == null) {
            throw new IllegalArgumentException("O ID do cliente não pode ser nulo.");
        }
        if (request.profissionalEmail() == null || request.profissionalEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail do profissional é obrigatório.");
        }
        if (request.servicosNomes() == null || request.servicosNomes().isEmpty()) {
            throw new IllegalArgumentException("É necessário informar pelo menos um serviço.");
        }
        if (request.horario() == null) {
            throw new IllegalArgumentException("A data e hora do agendamento são obrigatórias.");
        }

        // 2. Mapeia os dados do DTO para o Documento MongoDB
        var doc = new AgendamentoFlexODM();

        doc.setClienteId(request.clienteId());
        doc.setProfissionalEmail(request.profissionalEmail());
        doc.setServicosNomes(request.servicosNomes());
        doc.setDataHora(request.horario());
        doc.setAtributosDinamicos(request.atributosDinamicos());
        doc.setStatus("PENDENTE_NORMALIZACAO");

        // 3. Salva o documento no MongoDB
        var docSalvo = agendamentoDocumentoRepository.save(doc);
        System.out.println("Documento de agendamento flexível salvo no MongoDB com ID: " + docSalvo.getId());

        // 4. Dispara um evento do Spring. A mensagem para o RabbitMQ só será enviada
        // se esta transação (com o MongoDB) for bem-sucedida.
        eventPublisher.publishEvent(new AgendamentoOdmSalvoEventDTO(docSalvo.getId()));

        return docSalvo;
    }

    //API

    public AgendamentoOdmResponseDTO criarAgendamentoOdm(AgendamentoOdmRequestDTO dto){
        AgendamentoODM agendamentoODM = new AgendamentoODM();
        mapDtoToDocument(dto, agendamentoODM);

        var docSalvo = agendamentoODMRespository.save(agendamentoODM);

        return mapDocumentToResponseDTO(docSalvo);
    }

    @Transactional(readOnly = true)
    public AgendamentoOdmResponseDTO findAgendamentoById(String id){
        return agendamentoODMRespository.findById(id)
                .map(this::mapDocumentToResponseDTO)
                .orElseThrow(()->new EntityNotFoundException("Documento de agendamento nao encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<AgendamentoOdmResponseDTO> listarTodos(Pageable pageable) {
        return agendamentoODMRespository.findAll(pageable).map(this::mapDocumentToResponseDTO);
    }

    public AgendamentoOdmResponseDTO atualizar(String id, AgendamentoOdmRequestDTO dto) {
        var documento = agendamentoODMRespository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento com ID " + id + " não encontrado para atualização."));

        mapDtoToDocument(dto, documento);

        var docAtualizado = agendamentoODMRespository.save(documento);
        return mapDocumentToResponseDTO(docAtualizado);
    }

    private void mapDtoToDocument(AgendamentoOdmRequestDTO dto, AgendamentoODM documento) {
        documento.setAgendamentoId(dto.agendamentoId());
        documento.setHorarioInicio(dto.horarioInicio());
        documento.setClienteODM(dto.clienteOdmDTO());
        documento.setProfissionalODM(dto.profissionalOdmDTO());
        documento.setServicosODM(dto.servicoOdmDTOList());

        // Calcula e seta os campos derivados
        int duracaoTotal = dto.servicoOdmDTOList().stream().mapToInt(ServicoOdmDTO::duracaoTotal).sum();
        BigDecimal valorTotal = dto.servicoOdmDTOList().stream().map(ServicoOdmDTO::valor).reduce(BigDecimal.ZERO, BigDecimal::add);

        documento.setHorarioFim(dto.horarioInicio().plusMinutes(duracaoTotal));
        documento.setValorTotal(valorTotal);
    }

    public void deletar(String id) {
        if (!agendamentoODMRespository.existsById(id)) {
            throw new EntityNotFoundException("Documento com ID " + id + " não encontrado para exclusão.");
        }
        agendamentoODMRespository.deleteById(id);
    }

    private AgendamentoOdmResponseDTO mapDocumentToResponseDTO(AgendamentoODM doc) {
        return new AgendamentoOdmResponseDTO(
                doc.getId(),
                doc.getAgendamentoId(),
                doc.getHorarioInicio(),
                doc.getHorarioFim(),
                doc.getValorTotal(),
                doc.getClienteODM(),
                doc.getProfissionalODM(),
                doc.getServicosODM()
        );
    }
}

@Component
class AgendamentoOdmEventListener {
    private final AgendamentoOdmPublisher rabbitmqPublisher;
    AgendamentoOdmEventListener(AgendamentoOdmPublisher rabbitmqPublisher) { this.rabbitmqPublisher = rabbitmqPublisher; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAgendamentoOdmSalvo(AgendamentoOdmSalvoEventDTO evento) {
        System.out.println("### EventListener (ODM): Evento recebido! Preparando para publicar no RabbitMQ. ###");
        rabbitmqPublisher.publicarAgendamentoSalvo(evento.documentoId());
    }



}

