package com.bookingbarber.sys.service;

import com.bookingbarber.sys.dto.ClienteRequestDTO;
import com.bookingbarber.sys.dto.ClienteResponseDTO;
import com.bookingbarber.sys.entities.Cliente;
import com.bookingbarber.sys.entities.Role;
import com.bookingbarber.sys.repositorys.ClienteRepository;
import com.bookingbarber.sys.repositorys.RoleRepository;
import com.bookingbarber.sys.repositorys.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    public ClienteResponseDTO insertCliente(ClienteRequestDTO dto){
        if(usuarioRepository.existsByEmail(dto.email())){
            throw new IllegalArgumentException("Este email já está em uso.");
        }
        if(usuarioRepository.existsByCpf(dto.cpf())){
            throw new IllegalArgumentException("Este CPF já está em uso.");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setSobrenome(dto.sobrenome());
        cliente.setEmail(dto.email());
        cliente.setCpf(dto.cpf());
        cliente.setPassword(dto.password());
        cliente.setUrlFotoPerfil(dto.fotoPerfil());

        cliente.setDataCriacao(OffsetDateTime.now());
        cliente.setPontosFidelidade(0);
        cliente.setAtivo(true);

        Role role = roleRepository.findById(1L).orElseThrow(()-> new RuntimeException("Não foi possivel criar role"));
        cliente.setRoles(Set.of(role));

        Cliente clienteSalvo = clienteRepository.save(cliente);
        return mapToResponseDTO(clienteSalvo);
    }

    private ClienteResponseDTO mapToResponseDTO(Cliente clienteSalvo) {

        return new ClienteResponseDTO(
                clienteSalvo.getId(),
                clienteSalvo.getNome(),
                clienteSalvo.getSobrenome(),
                clienteSalvo.getCpf(),
                clienteSalvo.getEmail(),
                clienteSalvo.getUrlFotoPerfil(),
                clienteSalvo.getDataCriacao(),
                clienteSalvo.getPontosFidelidade(),
                clienteSalvo.getAtivo()
        );
    }


}
