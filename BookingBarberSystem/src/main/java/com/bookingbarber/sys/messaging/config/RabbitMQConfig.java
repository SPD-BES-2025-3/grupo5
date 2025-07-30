package com.bookingbarber.sys.messaging.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    // --- FLUXO 1: ORM -> ODM ---
    public static final String ORM_EVENTS_EXCHANGE = "orm.events.exchange";
    public static final String QUEUE_ORM_TO_ODM = "q.orm_to_odm.sync";
    public static final String ROUTING_KEY_AGENDAMENTO_SALVO_ORM = "agendamento.salvo.orm";

    @Bean public TopicExchange ormEventsExchange() { return new TopicExchange(ORM_EVENTS_EXCHANGE); }
    @Bean public Queue queueOrmToOdm() { return new Queue(QUEUE_ORM_TO_ODM, true); }
    @Bean public Binding bindingOrmToOdm(Queue queueOrmToOdm, TopicExchange ormEventsExchange) {
        return BindingBuilder.bind(queueOrmToOdm).to(ormEventsExchange).with(ROUTING_KEY_AGENDAMENTO_SALVO_ORM);
    }

    // --- FLUXO 2: ODM -> ORM ---
    public static final String ODM_EVENTS_EXCHANGE = "odm.events.exchange";
    public static final String QUEUE_ODM_TO_ORM = "q.odm_to_orm.sync";
    public static final String ROUTING_KEY_AGENDAMENTO_SALVO_ODM = "agendamento.salvo.odm";

    @Bean public TopicExchange odmEventsExchange() { return new TopicExchange(ODM_EVENTS_EXCHANGE); }
    @Bean public Queue queueOdmToOrm() { return new Queue(QUEUE_ODM_TO_ORM, true); }
    @Bean public Binding bindingOdmToOrm(Queue queueOdmToOrm, TopicExchange odmEventsExchange) {
        return BindingBuilder.bind(queueOdmToOrm).to(odmEventsExchange).with(ROUTING_KEY_AGENDAMENTO_SALVO_ODM);
    }

    // --- CONFIGURAÇÃO GLOBAL ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
