package com.bookingbarber.sys.messaging.publisher;

import com.bookingbarber.sys.messaging.config.RabbitMQConfig;
import com.bookingbarber.sys.messaging.event.AgendamentoSalvoEventDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AgendamentoOrmPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicarAgendamento(Long agendamentoId, String tipoEvento){
        var evento = new AgendamentoSalvoEventDTO(agendamentoId, tipoEvento);
        System.out.println("Publicando evento'"+ tipoEvento + "' para o agendamento ID: " + agendamentoId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORM_EVENTS_EXCHANGE,
                RabbitMQConfig.AGENDAMENTO_SALVO_ROUTING_KEY,
                evento
        );
    }
}
