package com.bookingbarber.sys.orm.controller;

import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoResponseDTO;
import com.bookingbarber.sys.orm.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api-orm/agendamento")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> agendar(@RequestBody AgendamentoRequestDTO dto){
        var  agendamentoCriado = agendamentoService.insertAgendamento(dto);
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoResponseDTO>> findAllAgendamentos(){
        var  agendamentoCriado = agendamentoService.findAllAgendamentos();
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.CREATED);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarHistoricoPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(agendamentoService.listarPorCliente(clienteId));
    }

    @DeleteMapping(value = "/deletar-registro/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        agendamentoService.deletarRegistro(id);
        return ResponseEntity.noContent().build();
    }


}
