package com.bookingbarber.sys.orm.dto.servico;

import java.math.BigDecimal;

public record ServicoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        Integer duracaoEmMinutos
) {}