package com.bookingbarber.sys.service;

import com.bookingbarber.sys.dto.AgendamentoRequestDTO;
import com.bookingbarber.sys.dto.AgendamentoResponseDTO;
import com.bookingbarber.sys.dto.ReagendamentorRequestDTO;
import com.bookingbarber.sys.entities.*;
import com.bookingbarber.sys.entities.enums.StatusAgendamento;
import com.bookingbarber.sys.repositorys.AgendamentoRepository;
import com.bookingbarber.sys.repositorys.ClienteRepository;
import com.bookingbarber.sys.repositorys.ProfissionalRepository;
import com.bookingbarber.sys.repositorys.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProfissionalRepository profissionalRepository;
    @Autowired
    private ServicoRepository servicoRepository;

    private static final LocalTime HORA_INICIO_EXPEDIENTE = LocalTime.of(8,0);
    private static final LocalTime HORA_FIM_EXPEDIENTE = LocalTime.of(18,0);

    private static final int MINUTOS_ANTECEDENCIA_CANCELAMENTO = 60;
    private static final ZoneId FUSO_HORARIO_LOCAL = ZoneId.of("America/Sao_Paulo");



    @Transactional
    public AgendamentoResponseDTO insertAgendamento(AgendamentoRequestDTO dto){
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(()-> new EntityNotFoundException("Cliente não encontrado."));
        Profissional profissional = profissionalRepository.findById(dto.profissionalId())
                .orElseThrow(()-> new EntityNotFoundException("Profissional não encontrado."));

        List<Servico> servicos = servicoRepository.findAllById(dto.servicosId());
        if(servicos.size() != dto.servicosId().size()){
            throw new EntityNotFoundException("Um ou mais serviços não foram encontrados.");
        }

        int duracaoTotal = servicos.stream()
                .mapToInt(Servico::getDuracao)
                .sum();

        OffsetDateTime horarioFim = dto.horario().plusMinutes(duracaoTotal);

        validarDisponibilidadeEHorario(dto.horario(), horarioFim, profissional.getId(), null);

        Agendamento novoAgendamento = new Agendamento();
        novoAgendamento.setCliente(cliente);
        novoAgendamento.setProfissional(profissional);
        novoAgendamento.setHorarioInicio(dto.horario());
        novoAgendamento.setHorarioFim(horarioFim);
        novoAgendamento.setStatus(StatusAgendamento.AGENDADO);

        Set<ServicoAgendado> servicosAgendados = servicos.stream().map(servico -> {
            ServicoAgendado servicoAgendado = new ServicoAgendado();
            servicoAgendado.setServico(servico);
            servicoAgendado.setAgendamento(novoAgendamento);
            servicoAgendado.setValor(servico.getValor());
            return servicoAgendado;
        }).collect(Collectors.toSet());
        novoAgendamento.setServicosAgendados(servicosAgendados);

        BigDecimal valorTotal = servicosAgendados.stream().map(ServicoAgendado::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        novoAgendamento.setValorTotal(valorTotal);

        Agendamento agendamentoSalvo = agendamentoRepository.save(novoAgendamento);

        return mapToDetalhadoDTO(agendamentoSalvo);

    }
    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarAgendamentoPorId(Long id){
        return agendamentoRepository.findById(id)
                .map(this::mapToDetalhadoDTO)
                .orElseThrow(()-> new EntityNotFoundException("Agendamento não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorProfissionalEData(Long profissionalId, LocalDate data) {
        OffsetDateTime inicioDoDia = data.atTime(LocalTime.MIN).atZone(FUSO_HORARIO_LOCAL).toOffsetDateTime();
        OffsetDateTime fimDoDia = data.atTime(LocalTime.MAX).atZone(FUSO_HORARIO_LOCAL).toOffsetDateTime();

        return agendamentoRepository.findAllByProfissionalIdAndHorarioInicioBetween(profissionalId, inicioDoDia, fimDoDia)
                .stream()
                .map(this::mapToDetalhadoDTO)
                .collect(Collectors.toList());
    }

    // 4. ATUALIZAR (Reagendar)
    @Transactional
    public AgendamentoResponseDTO reagendar(Long agendamentoId, ReagendamentorRequestDTO dto) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId).orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));

        long duracao = Duration.between(agendamento.getHorarioInicio(), agendamento.getHorarioFim()).toMinutes();
        OffsetDateTime novoHorarioFim = dto.novoHorario().plusMinutes(duracao);

        validarDisponibilidadeEHorario(dto.novoHorario(), novoHorarioFim, agendamento.getProfissional().getId(), agendamentoId);

        agendamento.setHorarioInicio(dto.novoHorario());
        agendamento.setHorarioFim(novoHorarioFim);
        Agendamento agendamentoSalvo = agendamentoRepository.save(agendamento);

        return mapToDetalhadoDTO(agendamentoSalvo);
    }

    // 5. DELETAR (Cancelar)
    @Transactional
    public void cancelar(Long agendamentoId) {
        var agendamento = agendamentoRepository.findById(agendamentoId).orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));

        if (agendamento.getStatus() == StatusAgendamento.CONCLUIDO || agendamento.getStatus() == StatusAgendamento.CANCELADO) {
            throw new IllegalStateException("Agendamento não pode ser cancelado pois seu status é '" + agendamento.getStatus() + "'.");
        }

        var agora = OffsetDateTime.now(FUSO_HORARIO_LOCAL);
        if (agendamento.getHorarioInicio().isBefore(agora.plusMinutes(MINUTOS_ANTECEDENCIA_CANCELAMENTO))) {
            throw new IllegalArgumentException("Cancelamento deve ser feito com no mínimo " + MINUTOS_ANTECEDENCIA_CANCELAMENTO + " minutos de antecedência.");
        }

        agendamento.setStatus(StatusAgendamento.CANCELADO);
        // Em um sistema real, aqui você poderia disparar um evento para notificar o cliente e o profissional.
    }




    public List<LocalTime> buscarHorariosDisponiveis(Long profissionalId, List<Long> servicoIds, LocalDate data) {
        // Busca a LISTA de serviços
        List<Servico> servicos = servicoRepository.findAllById(servicoIds);
        if (servicos.size() != servicoIds.size()) {
            throw new EntityNotFoundException("Um ou mais serviços não foram encontrados para calcular a duração.");
        }

        // SOMA as durações para saber o tamanho do "bloco" de tempo necessário
        int duracaoTotalServicos = servicos.stream()
                .mapToInt(Servico::getDuracao)
                .sum();

        // O resto da lógica é a mesma de antes, mas usando a `duracaoTotalServicos`
        // ... (Define expediente, busca agendamentos existentes, itera e verifica conflitos) ...

        // Exemplo da parte principal do loop:
        // LocalTime horarioFimPotencial = horarioAtual.plusMinutes(duracaoTotalServicos);
        // ...

        return new ArrayList<>(); // Placeholder para a lógica completa do loop
    }


    private void validarDisponibilidadeEHorario(OffsetDateTime inicio, OffsetDateTime fim, Long profissionalId, Long agendamentoIdParaIgnorar) {
        if (inicio.isBefore(OffsetDateTime.now(FUSO_HORARIO_LOCAL))) throw new IllegalArgumentException("Data do agendamento não pode ser no passado.");
        if (inicio.toLocalTime().isBefore(HORA_INICIO_EXPEDIENTE) || fim.toLocalTime().isAfter(HORA_FIM_EXPEDIENTE)) throw new IllegalArgumentException("Fora do horário de funcionamento.");

        var conflitos = agendamentoRepository.findOverlappingAppointments(profissionalId, inicio, fim);
        if (agendamentoIdParaIgnorar != null) {
            conflitos.removeIf(a -> a.getId().equals(agendamentoIdParaIgnorar));
        }
        if (!conflitos.isEmpty()) throw new IllegalStateException("Profissional já possui um agendamento conflitante neste horário.");
    }

    private AgendamentoResponseDTO mapToDetalhadoDTO(Agendamento agendamento) {
        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getCliente().getNome(),
                agendamento.getProfissional().getNome(),
                agendamento.getHorarioInicio(),
                agendamento.getHorarioFim(),
                agendamento.getServicosAgendados().stream().map(sa -> sa.getServico().getNome()).collect(Collectors.toList()),
                agendamento.getValorTotal(),
                agendamento.getStatus().toString()
        );
    }
}
