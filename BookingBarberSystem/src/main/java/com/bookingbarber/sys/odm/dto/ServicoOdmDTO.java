package com.bookingbarber.sys.odm.dto;

import java.math.BigDecimal;

public record ServicoOdmDTO(
        Long servicoId,
        String nome,
        BigDecimal valor,
        Integer duracaoTotal
) {
}
