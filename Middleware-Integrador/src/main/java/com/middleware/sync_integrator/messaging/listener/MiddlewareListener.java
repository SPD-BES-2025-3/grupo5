package com.middleware.sync_integrator.messaging.listener;

import com.middleware.sync_integrator.messaging.config.RabbitMQConfig;
import com.middleware.sync_integrator.messaging.event.AgendamentoSalvoEventDTO;
import com.middleware.sync_integrator.messaging.service.OrmToOdmTransformerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MiddlewareListener {

    @Autowired
    private OrmToOdmTransformerService transformerService;
    @RabbitListener(queues = RabbitMQConfig.AGENDAMENTO_SYNC_ODM_QUEUE)
    public void processarEventoDeAgendamento(AgendamentoSalvoEventDTO eventDTO){
        System.out.println("Middleware recebeu evento: " + eventDTO);
        try {
            if("CANCELADO".equals(eventDTO.tipoEvento())|| "DELETADO".equals(eventDTO.tipoEvento())){
                transformerService.deletarDocumento(eventDTO.agendamentoId());
            } else {
                transformerService.transformar(eventDTO.agendamentoId());
            }
        } catch (Exception e) {
            System.err.println("Falha ao processar evento para o agendamento ID " + eventDTO.agendamentoId() + ": " + e.getMessage());
            // Lançar exceção aqui fará o RabbitMQ tentar reprocessar a mensagem (se configurado)
            // ou enviá-la para uma Dead-Letter Queue.
            throw new RuntimeException("Erro no processamento do evento.", e);
        }
    }


}
