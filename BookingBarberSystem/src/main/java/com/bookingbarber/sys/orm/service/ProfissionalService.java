package com.bookingbarber.sys.orm.service;

import com.bookingbarber.sys.orm.dto.profissional.ProfissionalRequestDTO;
import com.bookingbarber.sys.orm.dto.profissional.ProfissionalResponseDTO;
import com.bookingbarber.sys.orm.entities.Especialidade;
import com.bookingbarber.sys.orm.entities.Profissional;
import com.bookingbarber.sys.orm.entities.Role;
import com.bookingbarber.sys.orm.repositories.EspecialidadeRepository;
import com.bookingbarber.sys.orm.repositories.ProfissionalRepository;
import com.bookingbarber.sys.orm.repositories.RoleRepository;
import com.bookingbarber.sys.orm.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class ProfissionalService {
    @Autowired
    private ProfissionalRepository profissionalRepository;
    @Autowired
    private EspecialidadeRepository especialidadeRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RoleRepository roleRepository;


    public ProfissionalResponseDTO insertProfissional(ProfissionalRequestDTO dto){
        Profissional profissional = new Profissional();
        profissional.setNome(dto.nome());
        profissional.setSobrenome(dto.sobrenome());
        profissional.setEmail(dto.email());
        profissional.setCpf(dto.cpf());
        profissional.setPassword(dto.password());
        profissional.setUrlFotoPerfil(dto.urlFotoPerfil());

        profissional.setNumeroRegistro(dto.numeroRegistro());
        profissional.setPercentualComissao(dto.percentualComissao());
        profissional.setDataContratacao(OffsetDateTime.now());
        profissional.setAtivo(true);


        HashSet<Especialidade> especialidades = new HashSet<>(especialidadeRepository.findAllById(dto.especialidadesIds()));
        if(especialidades.size() != dto.especialidadesIds().size()){
            throw new EntityNotFoundException("Uma ou mais especilidades não foram encontradas.");
        }
        profissional.setEspecialidades(especialidades);

        Role role = roleRepository.findById(2L)
                .orElseThrow(()-> new IllegalStateException("ROLE_PROFISSIONAL não encontrada."));
        profissional.setRoles(Set.of(role));

        Profissional profissionalSalvo = profissionalRepository.save(profissional);
        return mapToResponseDTO(profissionalSalvo);
    }

    @Transactional(readOnly = true)
    public Page<ProfissionalResponseDTO> findAllProfissionais(Pageable pageable){
        return profissionalRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProfissionalResponseDTO findProfissionalById(Long id){
        return profissionalRepository.findById(id).map(this::mapToResponseDTO)
                .orElseThrow(()-> new EntityNotFoundException("Profissional não encontrado."));
    }
    @Transactional
    public ProfissionalResponseDTO updateProfissional(Long id, ProfissionalRequestDTO dto) {
        var profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profissional com ID " + id + " não encontrado."));
        usuarioRepository.findByEmailAndIdNot(dto.email(), id).ifPresent(u -> {
            throw new IllegalArgumentException("O email '" + dto.email() + "' já está em uso por outro usuário.");
        });
        usuarioRepository.findByCpfAndIdNot(dto.cpf(), id).ifPresent(u -> {
            throw new IllegalArgumentException("O CPF '" + dto.cpf() + "' já está em uso por outro usuário.");
        });
        profissionalRepository.findByNumeroRegistroAndIdNot(dto.numeroRegistro(), id).ifPresent(p -> {
            throw new IllegalArgumentException("O número de registro '" + dto.numeroRegistro() + "' já está em uso por outro profissional.");
        });

        profissional.setNome(dto.nome());
        profissional.setSobrenome(dto.sobrenome());
        profissional.setEmail(dto.email());
        profissional.setCpf(dto.cpf());
        profissional.setUrlFotoPerfil(dto.urlFotoPerfil());
        profissional.setNumeroRegistro(dto.numeroRegistro());
        profissional.setPercentualComissao(dto.percentualComissao());

        if (dto.especialidadesIds() != null && !dto.especialidadesIds().isEmpty()) {
            var especialidades = new HashSet<>(especialidadeRepository.findAllById(dto.especialidadesIds()));
            if (especialidades.size() != dto.especialidadesIds().size()) {
                throw new EntityNotFoundException("Uma ou mais especialidades fornecidas são inválidas.");
            }
            profissional.setEspecialidades(especialidades);
        } else {
            profissional.getEspecialidades().clear();
        }
        var profissionalAtualizado = profissionalRepository.save(profissional);
        return mapToResponseDTO(profissionalAtualizado);
    }

    @Transactional
    public void desativarProfissional(Long id){
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado"));
        profissional.setAtivo(false);
        profissionalRepository.save(profissional);
    }
    private ProfissionalResponseDTO mapToResponseDTO(Profissional profissional) {
        return new ProfissionalResponseDTO(
                profissional.getId(),
                profissional.getNome(),
                profissional.getSobrenome(),
                profissional.getEmail(),
                profissional.getNumeroRegistro(),
                profissional.getPercentualComissao(),
                profissional.getEspecialidades(),
                profissional.getAtivo()
        );
    }
}
