package com.bookingbarber.sys.orm.service;

import com.bookingbarber.sys.orm.dto.auth.LoginRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.entities.Cliente;
import com.bookingbarber.sys.orm.repositories.ClienteRepository;
import com.bookingbarber.sys.orm.repositories.UsuarioRepository;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    // Construtor com injeção de dependências...

    public ClienteResponseDTO loginCliente(LoginRequestDTO dto) throws AuthenticationException {
        // 1. Busca o usuário pelo e-mail
        var usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new AuthenticationException("Credenciais inválidas."));

        // 2. Verifica se a senha corresponde (essencial usar o encoder)
        if (!passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new AuthenticationException("Credenciais inválidas.");
        }

        // 3. Verifica se o usuário é de fato um cliente
        var cliente = clienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new AuthenticationException("Acesso permitido apenas para clientes."));

        System.out.println("Cliente logado!!!!!!!!!!!!!");

        // 4. Se tudo estiver certo, retorna os dados do cliente
        return mapToClienteResponseDTO(cliente);
    }

    private ClienteResponseDTO mapToClienteResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getSobrenome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getUrlFotoPerfil(),
                cliente.getDataCriacao(),
                cliente.getPontosFidelidade(),
                cliente.getAtivo()
        );
    }
}
