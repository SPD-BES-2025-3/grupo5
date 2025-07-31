package com.bookingbarber.sys.odm.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record AgendamentoOdmRequestDTO(
        Long agendamentoId,
        OffsetDateTime horarioInicio,
        ClienteOdmDTO clienteOdmDTO,
        ProfissionalOdmDTO profissionalOdmDTO,
        List<ServicoOdmDTO> servicoOdmDTOList
) {
}
