package com.middleware.sync_integrator.messaging.listener;

import com.middleware.sync_integrator.messaging.config.RabbitMQConfig;
import com.middleware.sync_integrator.messaging.event.AgendamentoOdmSalvoEventDTO;
import com.middleware.sync_integrator.messaging.event.AgendamentoOrmSalvoEventDTO;
import com.middleware.sync_integrator.messaging.service.OdmToOrmTransformerService;
import com.middleware.sync_integrator.messaging.service.OrmToOdmTransformerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MiddlewareListener {

    @Autowired
    private OrmToOdmTransformerService transformerService;

    @Autowired
    private OdmToOrmTransformerService odmToOrmTransformer;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORM_TO_ODM)
    public void processarEventoDeAgendamento(AgendamentoOrmSalvoEventDTO eventDTO){
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
    @RabbitListener(queues = RabbitMQConfig.QUEUE_ODM_TO_ORM)
    public void processarEventoDeAgendamentoOdm(AgendamentoOdmSalvoEventDTO evento) {
        System.out.println("Middleware recebeu evento do ODM: " + evento);
        try {
            odmToOrmTransformer.normalizarESalvar(evento.documentoId());
        } catch (Exception e) {
            System.err.println("Falha ao processar evento do ODM para o documento ID " + evento.documentoId() + ": " + e.getMessage());
            throw new RuntimeException("Erro no processamento do evento ODM.", e);
        }
    }


}
