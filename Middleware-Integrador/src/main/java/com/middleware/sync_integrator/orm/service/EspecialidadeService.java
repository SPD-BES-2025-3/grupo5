package com.middleware.sync_integrator.orm.service;

import com.middleware.sync_integrator.orm.dto.especialidade.EspecialidadeRequestDTO;
import com.middleware.sync_integrator.orm.dto.especialidade.EspecialidadeResponseDTO;
import com.middleware.sync_integrator.orm.entities.Especialidade;
import com.middleware.sync_integrator.orm.repositories.EspecialidadeRepository;
import com.middleware.sync_integrator.orm.repositories.ProfissionalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EspecialidadeService {
    @Autowired
    private EspecialidadeRepository especialidadeRepository;
    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Transactional
    public EspecialidadeResponseDTO insertEspecialidade(EspecialidadeRequestDTO dto){
        especialidadeRepository.findByNomeIgnoreCase(dto.nome()).ifPresent(
                e -> {
                    throw new IllegalArgumentException("Uma especialidade com este nome já existe.");
                }
        );

        Especialidade especialidade = new Especialidade();
        especialidade.setNome(dto.nome());
        especialidade.setTipo(dto.tipo());

        Especialidade especialidadeSalva = especialidadeRepository.save(especialidade);

        return mapToResponseDTO(especialidadeSalva);
    }

    @Transactional(readOnly = true)
    public EspecialidadeResponseDTO buscarPorId(Long id) {
        return especialidadeRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada."));
    }

    @Transactional(readOnly = true)
    public Page<EspecialidadeResponseDTO> listarTodos(Pageable pageable) {
        return especialidadeRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional
    public EspecialidadeResponseDTO atualizar(Long id, EspecialidadeRequestDTO dto) {
        var especialidade = especialidadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada."));

        // Regra: Valida se o novo nome já não está em uso por outra especialidade.
        especialidadeRepository.findByNomeIgnoreCaseAndIdNot(dto.nome(), id).ifPresent(e -> {
            throw new IllegalArgumentException("Uma especialidade com este nome já existe.");
        });

        especialidade.setNome(dto.nome());
        especialidade.setTipo(dto.tipo());

        var especialidadeAtualizada = especialidadeRepository.save(especialidade);
        return mapToResponseDTO(especialidadeAtualizada);
    }

    @Transactional
    public void deletar(Long id) {
        if (!especialidadeRepository.existsById(id)) {
            throw new EntityNotFoundException("Especialidade não encontrada.");
        }

        // Regra de Negócio CRÍTICA: Não permitir exclusão se a especialidade estiver em uso.
        if (profissionalRepository.existsByEspecialidadesId(id)) {
            throw new IllegalStateException("Não é possível excluir esta especialidade, pois ela está associada a um ou mais profissionais.");
        }

        // Se passar pela validação, a exclusão é permitida.
        especialidadeRepository.deleteById(id);
    }

    private EspecialidadeResponseDTO mapToResponseDTO(Especialidade especialidade) {
        return new EspecialidadeResponseDTO(
                especialidade.getId(),
                especialidade.getNome(),
                especialidade.getTipo()
        );
    }


}
