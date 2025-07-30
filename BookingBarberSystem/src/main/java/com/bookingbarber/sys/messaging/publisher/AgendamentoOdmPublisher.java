package com.bookingbarber.sys.messaging.publisher;

import com.bookingbarber.sys.messaging.config.RabbitMQConfig;
import com.bookingbarber.sys.messaging.event.AgendamentoOdmSalvoEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AgendamentoOdmPublisher {
    private final RabbitTemplate rabbitTemplate;
    public AgendamentoOdmPublisher(RabbitTemplate rabbitTemplate) { this.rabbitTemplate = rabbitTemplate; }

    public void publicarAgendamentoSalvo(String documentoId) {
        var evento = new AgendamentoOdmSalvoEventDTO(documentoId);
        System.out.println(">>> Publicando evento ODM para RabbitMQ. Routing Key: " + RabbitMQConfig.ROUTING_KEY_AGENDAMENTO_SALVO_ODM);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ODM_EVENTS_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_AGENDAMENTO_SALVO_ODM,
                evento
        );
    }
}
