package com.bookingbarber.sys.orm.service;

import com.bookingbarber.sys.orm.dto.cliente.ClienteRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.entities.Cliente;
import com.bookingbarber.sys.orm.entities.Role;
import com.bookingbarber.sys.orm.repositories.ClienteRepository;
import com.bookingbarber.sys.orm.repositories.RoleRepository;
import com.bookingbarber.sys.orm.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(readOnly = true)
    public ClienteResponseDTO findClienteById(Long id){
        return clienteRepository.findById(id).map(this::mapToResponseDTO)
                .orElseThrow(()-> new EntityNotFoundException("Cliente não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> findAllClientes(Pageable pageable){
        return clienteRepository.findAll(pageable).map(this::mapToResponseDTO);
    }
    @Transactional
    public ClienteResponseDTO updateCliente(Long id, ClienteRequestDTO dto){
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Cliente não encontrado."));

        usuarioRepository.findByEmailAndIdNot(dto.email(), id).ifPresent(u->{
            throw new IllegalArgumentException("Este email já pertence a outro usuario.");
        });

        usuarioRepository.findByCpfAndIdNot(dto.cpf(), id).ifPresent(u->{
            throw new IllegalArgumentException("Este CPF já pertence a outro usuário.");
        });

        cliente.setNome(dto.nome());
        cliente.setSobrenome(dto.sobrenome());
        cliente.setEmail(dto.email());
        cliente.setCpf(dto.cpf());
        cliente.setUrlFotoPerfil(dto.fotoPerfil());

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return  mapToResponseDTO(clienteAtualizado);
    }

    @Transactional
    public void desativarCliente(Long id){
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
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
