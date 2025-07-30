package com.middleware.sync_integrator.orm.dto.agendamento;

import java.time.OffsetDateTime;

public record ReagendamentorRequestDTO(
    OffsetDateTime novoHorario
) {
}
