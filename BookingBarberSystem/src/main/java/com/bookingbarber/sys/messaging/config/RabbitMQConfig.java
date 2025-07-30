package com.bookingbarber.sys.messaging.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    //roteador de mensagens do ORM
    public static final String ORM_EVENTS_EXCHANGE = "orm.events.exchange";

    // Fila: Onde as mensagens de agendamento ficam para serem processadas pelo middleware
    public static final String AGENDAMENTO_SYNC_ODM_QUEUE = "agendamento.sync.odm.queue";

    // Routing Key: A "etiqueta" da mensagem
    public static final String AGENDAMENTO_SALVO_ROUTING_KEY = "agendamento.salvo";

    @Bean
    public TopicExchange ormEventsExchange(){
        return new TopicExchange(ORM_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue agendamentoSyncOdmQueue(){
        return new Queue(AGENDAMENTO_SYNC_ODM_QUEUE);
    }

    @Bean
    public Binding binding(Queue agendamentoSyncOdmQueue, TopicExchange ormEventsExchange){
        return BindingBuilder.bind(agendamentoSyncOdmQueue)
                .to(ormEventsExchange)
                .with(AGENDAMENTO_SALVO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
