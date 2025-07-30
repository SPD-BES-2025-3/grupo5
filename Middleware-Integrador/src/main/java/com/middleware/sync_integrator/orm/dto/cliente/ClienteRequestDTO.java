package com.middleware.sync_integrator.orm.dto.cliente;

public record ClienteRequestDTO(
        String nome,
        String sobrenome,
        String cpf,
        String email,
        String password,
        String fotoPerfil
) {}