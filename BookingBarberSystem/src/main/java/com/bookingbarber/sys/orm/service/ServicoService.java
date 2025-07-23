package com.bookingbarber.sys.orm.service;

import com.bookingbarber.sys.orm.dto.servico.ServicoRequestDTO;
import com.bookingbarber.sys.orm.dto.servico.ServicoResponseDTO;
import com.bookingbarber.sys.orm.entities.Servico;
import com.bookingbarber.sys.orm.repositories.ServicoAgendadoRepository;
import com.bookingbarber.sys.orm.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoAgendadoRepository servicoAgendadoRepository;

    public ServicoService(ServicoRepository servicoRepository, ServicoAgendadoRepository servicoAgendadoRepository) {
        this.servicoRepository = servicoRepository;
        this.servicoAgendadoRepository = servicoAgendadoRepository;
    }

    @Transactional
    public ServicoResponseDTO insertServico(ServicoRequestDTO dto) {

        servicoRepository.findByNomeIgnoreCase(dto.nome()).ifPresent(s -> {
            throw new IllegalArgumentException("Um serviço com este nome já existe.");
        });

        var servico = new Servico();
        mapDtoToEntity(dto, servico);

        var servicoSalvo = servicoRepository.save(servico);
        return mapToResponseDTO(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO findServicoPorId(Long id) {
        return servicoRepository.findById(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> findAllServicos(Pageable pageable) {
        return servicoRepository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional
    public ServicoResponseDTO updateServico(Long id, ServicoRequestDTO dto) {
        var servico = servicoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado."));

        servicoRepository.findByNomeIgnoreCaseAndIdNot(dto.nome(), id).ifPresent(s -> {
            throw new IllegalArgumentException("Um serviço com este nome já existe.");
        });

        mapDtoToEntity(dto, servico);

        var servicoAtualizado = servicoRepository.save(servico);
        return mapToResponseDTO(servicoAtualizado);
    }

    @Transactional
    public void deleteServico(Long id) {
        if (!servicoRepository.existsById(id)) {
            throw new EntityNotFoundException("Serviço não encontrado.");
        }

        if (servicoAgendadoRepository.existsByServicoId(id)) {
            throw new IllegalStateException("Não é possível excluir este serviço, pois ele já faz parte de um ou mais agendamentos históricos.");
        }

        servicoRepository.deleteById(id);
    }

    private ServicoResponseDTO mapToResponseDTO(Servico servico) {
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValor(),
                servico.getDuracao()
        );
    }

    private void mapDtoToEntity(ServicoRequestDTO dto, Servico servico) {
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setValor(dto.valor());
        servico.setDuracao(dto.duracaoEmMinutos());
    }
}