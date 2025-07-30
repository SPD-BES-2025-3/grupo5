package com.middleware.sync_integrator.orm.dto.profissional;

import java.util.List;

public record ProfissionalRequestDTO(
        String nome,
        String sobrenome,
        String cpf,
        String email,
        String password,
        String urlFotoPerfil,
        String numeroRegistro,
        Double percentualComissao,
        List<Long> especialidadesIds
) {
}
