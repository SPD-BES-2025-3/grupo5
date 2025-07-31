package com.bookingbarber.sys.odm.controller;

import com.bookingbarber.sys.odm.dto.AgendamentoOdmRequestDTO;
import com.bookingbarber.sys.odm.dto.AgendamentoOdmResponseDTO;
import com.bookingbarber.sys.odm.service.AgendamentoODMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api-odm/agendamentos")
public class AgendamentoOdmController {
    @Autowired
    private AgendamentoODMService agendamentoOdmService;

    @PostMapping
    public ResponseEntity<AgendamentoOdmResponseDTO> criarDocumento(@RequestBody AgendamentoOdmRequestDTO dto) {
        var docCriado = agendamentoOdmService.criarAgendamentoOdm(dto);
        return new ResponseEntity<>(docCriado, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoOdmResponseDTO> buscarDocumentoPorId(@PathVariable String id) {
        return ResponseEntity.ok(agendamentoOdmService.findAgendamentoById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AgendamentoOdmResponseDTO>> listarDocumentos(Pageable pageable) {
        return ResponseEntity.ok(agendamentoOdmService.listarTodos(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoOdmResponseDTO> atualizarDocumento(@PathVariable String id, @RequestBody AgendamentoOdmRequestDTO dto) {
        return ResponseEntity.ok(agendamentoOdmService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDocumento(@PathVariable String id) {
        agendamentoOdmService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
