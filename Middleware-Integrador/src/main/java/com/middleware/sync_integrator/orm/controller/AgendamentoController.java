package com.middleware.sync_integrator.orm.controller;

import com.middleware.sync_integrator.orm.dto.agendamento.AgendamentoRequestDTO;
import com.middleware.sync_integrator.orm.dto.agendamento.AgendamentoResponseDTO;
import com.middleware.sync_integrator.orm.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api-orm/agendamento")
public class AgendamentoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> agendar(@RequestBody AgendamentoRequestDTO dto){
        var  agendamentoCriado = agendamentoService.insertAgendamento(dto);
        return new ResponseEntity<>(agendamentoCriado, HttpStatus.CREATED);
    }
}
