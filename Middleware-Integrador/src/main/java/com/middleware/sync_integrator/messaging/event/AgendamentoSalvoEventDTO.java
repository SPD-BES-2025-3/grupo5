package com.middleware.sync_integrator.messaging.event;

public record AgendamentoSalvoEventDTO(
        Long agendamentoId,
        String tipoEvento
) {
}
