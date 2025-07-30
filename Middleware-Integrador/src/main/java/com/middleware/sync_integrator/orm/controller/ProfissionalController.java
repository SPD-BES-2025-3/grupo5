package com.middleware.sync_integrator.orm.controller;

import com.middleware.sync_integrator.orm.dto.profissional.ProfissionalRequestDTO;
import com.middleware.sync_integrator.orm.dto.profissional.ProfissionalResponseDTO;
import com.middleware.sync_integrator.orm.service.ProfissionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api-orm/profissional")
public class ProfissionalController {

    @Autowired
    private ProfissionalService profissionalService;

    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> criarCliente(@RequestBody ProfissionalRequestDTO dto){
        var novoProfissional  = profissionalService.insertProfissional(dto);
        return new ResponseEntity<>(novoProfissional, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<ProfissionalResponseDTO>> findAllProfissionais(Pageable pageable){
        return ResponseEntity.ok(profissionalService.findAllProfissionais(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> findProfissionalById(@PathVariable Long id){
        return ResponseEntity.ok(profissionalService.findProfissionalById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> updateProfissional(@PathVariable Long id, @RequestBody ProfissionalRequestDTO dto){
        return ResponseEntity.ok(profissionalService.updateProfissional(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarProfissional(@PathVariable Long id){
        profissionalService.desativarProfissional(id);
        return ResponseEntity.noContent().build();
    }
}
