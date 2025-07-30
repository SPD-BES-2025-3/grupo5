package com.middleware.sync_integrator.messaging.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        // Conversor para escrever OffsetDateTime no formato Date do MongoDB
        converters.add(new OffsetDateTimeWriteConverter());
        // Conversor para ler o formato Date do MongoDB como OffsetDateTime
        converters.add(new OffsetDateTimeReadConverter());
        return new MongoCustomConversions(converters);
    }

    // --- Nossas Classes de Conversão ---

    // Converte de OffsetDateTime para Date na hora de ESCREVER no banco
    static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {
        @Override
        public Date convert(OffsetDateTime source) {
            return source == null ? null : Date.from(source.toInstant());
        }
    }

    // Converte de Date para OffsetDateTime na hora de LER do banco
    static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Date source) {
            return source == null ? null : source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }
}