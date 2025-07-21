package com.bookingbarber.sys.controller;

import com.bookingbarber.sys.dto.ClienteRequestDTO;
import com.bookingbarber.sys.dto.ClienteResponseDTO;
import com.bookingbarber.sys.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api-orm/cliente")
public class ClienteController {

    @Autowired
    private ClienteService  clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> criarCliente(@RequestBody ClienteRequestDTO dto){
        var novoCliente  = clienteService.insertCliente(dto);
        return new ResponseEntity<>(novoCliente, HttpStatus.CREATED);
    }
}
