package com.bookingbarber.sys.orm.dto.agendamento;

import java.time.OffsetDateTime;
import java.util.List;

public record AgendamentoRequestDTO(Long clienteId, Long profissionalId, OffsetDateTime horario, List<Long> servicosId) {
}
