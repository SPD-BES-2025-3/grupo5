package com.bookingbarber.sys.messaging.event;

public record AgendamentoSalvoEventDTO(
        Long agendamentoId,
        String tipoEvento
) {
}
