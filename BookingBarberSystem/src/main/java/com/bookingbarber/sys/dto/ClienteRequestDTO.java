package com.bookingbarber.sys.dto;

public record ClienteRequestDTO(
        String nome,
        String sobrenome,
        String cpf,
        String email,
        String password, // A senha só será usada na criação
        String fotoPerfil
) {}