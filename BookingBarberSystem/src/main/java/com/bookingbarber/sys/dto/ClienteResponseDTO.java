package com.bookingbarber.sys.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String sobrenome,
        String cpf,
        String email,
        String fotoPerfil,
        OffsetDateTime dataCriacao,
        Integer pontosFidelidade,
        boolean ativo
) {}
