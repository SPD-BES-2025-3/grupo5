package com.middleware.sync_integrator.odm.dto;

public record ClienteOmdDTO(
        Long clienteId,
        String nomeCompleto,
        String cpf,
        String email
) {
}


