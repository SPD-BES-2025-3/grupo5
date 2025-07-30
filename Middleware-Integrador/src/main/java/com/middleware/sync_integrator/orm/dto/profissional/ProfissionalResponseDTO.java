package com.middleware.sync_integrator.orm.dto.profissional;

import com.middleware.sync_integrator.orm.entities.Especialidade;

import java.util.Set;

public record ProfissionalResponseDTO(
        Long id,
        String nome,
        String sobrenome,
        String email,
        String numeroRegistro,
        Double percentualComissao,
        Set<Especialidade> especialidades,
        boolean ativo
) {

}
