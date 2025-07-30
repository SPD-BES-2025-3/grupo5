package com.middleware.sync_integrator.orm.dto.cliente;

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
