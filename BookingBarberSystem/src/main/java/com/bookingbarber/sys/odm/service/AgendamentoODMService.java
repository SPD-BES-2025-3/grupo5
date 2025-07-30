package com.bookingbarber.sys.odm.service;

import com.bookingbarber.sys.messaging.event.AgendamentoOdmSalvoEventDTO;
import com.bookingbarber.sys.messaging.publisher.AgendamentoOdmPublisher;
import com.bookingbarber.sys.odm.dto.AgendamentoFlexOdmDTO;
import com.bookingbarber.sys.odm.entities.AgendamentoFlexODM;
import com.bookingbarber.sys.odm.repositories.AgendamentoFlexODMRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
public class AgendamentoODMService {

    private final AgendamentoFlexODMRepository agendamentoDocumentoRepository;
    private final ApplicationEventPublisher eventPublisher; // INJETAR O PUBLICADOR DE EVENTOS DO SPRING

    public AgendamentoODMService(AgendamentoFlexODMRepository agendamentoDocumentoRepository, ApplicationEventPublisher eventPublisher) {
        this.agendamentoDocumentoRepository = agendamentoDocumentoRepository;
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