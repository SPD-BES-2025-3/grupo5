package com.bookingbarber.sys.odm.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AgendamentoOdmResponseDTO(
        String id,
        Long agendamentoId,
        OffsetDateTime horarioInicio,
        OffsetDateTime horarioFim,
        BigDecimal valorTotal,
        ClienteOdmDTO clienteOdmDTO,
        ProfissionalOdmDTO profissionalOdmDTO,
        List<ServicoOdmDTO> servicoOdmDTOS
) {
}
