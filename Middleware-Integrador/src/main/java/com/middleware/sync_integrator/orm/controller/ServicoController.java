package com.middleware.sync_integrator.orm.controller;

import com.middleware.sync_integrator.orm.dto.servico.ServicoRequestDTO;
import com.middleware.sync_integrator.orm.dto.servico.ServicoResponseDTO;
import com.middleware.sync_integrator.orm.service.ServicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-orm/servico")
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> criarServico(@RequestBody ServicoRequestDTO dto) {
        var novoServico = servicoService.insertServico(dto);
        return new ResponseEntity<>(novoServico, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarServicoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.findServicoPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<ServicoResponseDTO>> listarServicos(Pageable pageable) {
        return ResponseEntity.ok(servicoService.findAllServicos(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizarServico(@PathVariable Long id, @RequestBody ServicoRequestDTO dto) {
        return ResponseEntity.ok(servicoService.updateServico(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        servicoService.deleteServico(id);
        return ResponseEntity.noContent().build();
    }
}
