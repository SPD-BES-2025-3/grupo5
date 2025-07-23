package com.bookingbarber.sys.orm.controller;

import com.bookingbarber.sys.orm.dto.cliente.ClienteRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



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
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> findClienteById(@PathVariable Long id){
        return ResponseEntity.ok(clienteService.findClienteById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> findAllClientes(Pageable pageable){
        return ResponseEntity.ok(clienteService.findAllClientes(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> updateClientee(@PathVariable Long id, @RequestBody ClienteRequestDTO dto){
        return ResponseEntity.ok(clienteService.updateCliente(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarCliente(@PathVariable Long id){
        clienteService.desativarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
