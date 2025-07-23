package com.bookingbarber.sys.orm.dto.agendamento;

import java.time.OffsetDateTime;

public record ReagendamentorRequestDTO(
    OffsetDateTime novoHorario
) {
}
