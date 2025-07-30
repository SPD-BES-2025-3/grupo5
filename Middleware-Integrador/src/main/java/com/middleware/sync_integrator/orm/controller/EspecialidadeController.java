package com.middleware.sync_integrator.orm.controller;

import com.middleware.sync_integrator.orm.dto.especialidade.EspecialidadeRequestDTO;
import com.middleware.sync_integrator.orm.dto.especialidade.EspecialidadeResponseDTO;
import com.middleware.sync_integrator.orm.service.EspecialidadeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-orm/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    public EspecialidadeController(EspecialidadeService especialidadeService) {
        this.especialidadeService = especialidadeService;
    }

    @PostMapping
    public ResponseEntity<EspecialidadeResponseDTO> criarEspecialidade(@RequestBody EspecialidadeRequestDTO dto) {
        var novaEspecialidade = especialidadeService.insertEspecialidade(dto);
        return new ResponseEntity<>(novaEspecialidade, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponseDTO> buscarEspecialidadePorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadeService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<EspecialidadeResponseDTO>> listarEspecialidades(Pageable pageable) {
        return ResponseEntity.ok(especialidadeService.listarTodos(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponseDTO> atualizarEspecialidade(@PathVariable Long id, @RequestBody EspecialidadeRequestDTO dto) {
        return ResponseEntity.ok(especialidadeService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEspecialidade(@PathVariable Long id) {
        especialidadeService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
