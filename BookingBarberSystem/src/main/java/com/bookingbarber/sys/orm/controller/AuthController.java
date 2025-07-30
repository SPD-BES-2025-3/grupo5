package com.bookingbarber.sys.orm.controller;

import com.bookingbarber.sys.orm.dto.auth.LoginRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.entities.Cliente;
import com.bookingbarber.sys.orm.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ClienteResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        try {
            ClienteResponseDTO clienteLogado = authService.loginCliente(dto);
            return ResponseEntity.ok(clienteLogado);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}