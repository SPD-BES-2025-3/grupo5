package com.bookingbarber.sys.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AgendamentoResponseDTO(Long id, String nomeCliente, String nomeProfissional, OffsetDateTime horaInicio, OffsetDateTime horaFim, List<String> servicos,
                                     BigDecimal valorTotal, String status) {
}
