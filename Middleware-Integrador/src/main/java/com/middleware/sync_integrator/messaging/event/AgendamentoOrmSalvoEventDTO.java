package com.middleware.sync_integrator.messaging.event;

public record AgendamentoOrmSalvoEventDTO(
        Long agendamentoId,
        String tipoEvento
) {
}
