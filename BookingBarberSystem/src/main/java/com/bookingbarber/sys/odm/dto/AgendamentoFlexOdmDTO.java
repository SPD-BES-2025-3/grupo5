package com.bookingbarber.sys.odm.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record AgendamentoFlexOdmDTO(
        Long clienteId,
        String profissionalEmail,
        List<String> servicosNomes,
        OffsetDateTime horario,
        Map<String, String> atributosDinamicos
) {
}
