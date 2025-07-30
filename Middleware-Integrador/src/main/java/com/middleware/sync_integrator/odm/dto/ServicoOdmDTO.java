package com.middleware.sync_integrator.odm.dto;

import java.math.BigDecimal;

public record ServicoOdmDTO(
        Long servicoId,
        String nome,
        BigDecimal valor,
        Integer duracaoTotal
) {
}
