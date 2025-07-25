package com.bookingbarber.sys.orm.dto.servico;

import java.math.BigDecimal;

public record ServicoRequestDTO(
        String nome,
        String descricao,
        BigDecimal valor,
        Integer duracaoEmMinutos
) {}