package com.bookingbarber.sys.messaging.event;

public record AgendamentoOrmSalvoEventDTO(
        Long agendamentoId,
        String tipoEvento
) {
}
