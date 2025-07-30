package com.bookingbarber.sys.orm.dto.profissional;

import com.bookingbarber.sys.orm.entities.Especialidade;

import java.util.Set;

public record ProfissionalResponseDTO(
        Long id,
        String nome,
        String sobrenome,
        String email,
        String numeroRegistro,
        Double percentualComissao,
        Set<String> especialidades,
        boolean ativo
) {
    @Override
    public String toString(){
        return nome + " " + sobrenome;

    }

}
