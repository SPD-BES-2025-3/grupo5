package com.bookingbarber.sys.orm.service;

import com.bookingbarber.sys.messaging.event.AgendamentoOrmSalvoEventDTO;
import com.bookingbarber.sys.messaging.publisher.AgendamentoOrmPublisher;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoResponseDTO;
import com.bookingbarber.sys.orm.dto.agendamento.ReagendamentorRequestDTO;
import com.bookingbarber.sys.orm.entities.*;
import com.bookingbarber.sys.orm.entities.enums.StatusAgendamento;
import com.bookingbarber.sys.orm.repositories.AgendamentoRepository;
import com.bookingbarber.sys.orm.repositories.ClienteRepository;
import com.bookingbarber.sys.orm.repositories.ProfissionalRepository;
import com.bookingbarber.sys.orm.repositories.ServicoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    @Autowired
    private ApplicationEventPublisher eventPublisher; //publicador de eventos do spring

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
        eventPublisher.publishEvent(new AgendamentoOrmSalvoEventDTO(agendamentoSalvo.getId(),"CRIADO"));

        return mapToDetalhadoDTO(agendamentoSalvo);

    }
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> findAllAgendamentos(){
        return agendamentoRepository.findAll().stream().map(this::mapToDetalhadoDTO).toList();

    }
    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente não encontrado.");
        }
        return agendamentoRepository.findAllByClienteIdOrderByHorarioInicioDesc(clienteId)
                .stream()
                .map(this::mapToDetalhadoDTO)
                .collect(Collectors.toList());
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
    }

    @Transactional
    public void deletarRegistro(Long agendamentoId){
        agendamentoRepository.deleteById(agendamentoId);
    }


    @Transactional(readOnly = true)
    public List<LocalTime> buscarHorariosDisponiveis(Long profissionalId, List<Long> servicoIds, LocalDate data) {
        System.out.println("\n--- INICIANDO BUSCA DE HORÁRIOS ---");
        System.out.println("Data: " + data + ", Profissional ID: " + profissionalId + ", Serviços IDs: " + servicoIds);

        if (!profissionalRepository.existsById(profissionalId)) {
            throw new EntityNotFoundException("Profissional não encontrado.");
        }
        List<Servico> servicos = servicoRepository.findAllById(servicoIds);
        if (servicos.size() != servicoIds.size()) {
            throw new EntityNotFoundException("Um ou mais serviços não foram encontrados.");
        }

        int duracaoTotalServicos = servicos.stream().mapToInt(Servico::getDuracao).sum();
        System.out.println("Duração Total Calculada: " + duracaoTotalServicos + " minutos.");

        final LocalTime inicioExpediente = LocalTime.of(8, 0);
        final LocalTime fimExpediente = LocalTime.of(18, 0);
        System.out.println("Expediente Definido: " + inicioExpediente + " - " + fimExpediente);

        ZoneOffset fusoHorarioLocal = OffsetDateTime.now().getOffset();
        OffsetDateTime inicioDoDia = data.atTime(inicioExpediente).atOffset(fusoHorarioLocal);
        OffsetDateTime fimDoDia = data.atTime(fimExpediente).atOffset(fusoHorarioLocal);

        List<Agendamento> agendamentosDoDia = agendamentoRepository
                .findAllByProfissionalIdAndHorarioInicioBetween(profissionalId, inicioDoDia, fimDoDia);
        System.out.println("Agendamentos existentes encontrados para o dia: " + agendamentosDoDia.size());
        agendamentosDoDia.forEach(ag -> System.out.println("  - Ocupado de " + ag.getHorarioInicio().toLocalTime() + " até " + ag.getHorarioFim().toLocalTime()));

        List<LocalTime> horariosDisponiveis = new ArrayList<>();
        LocalTime horarioAtual = inicioExpediente;
        final int intervaloSlots = 30;

        System.out.println("Verificando slots a cada " + intervaloSlots + " minutos...");
        while (!horarioAtual.isAfter(fimExpediente.minusMinutes(duracaoTotalServicos))) {
            LocalTime horarioFimPotencial = horarioAtual.plusMinutes(duracaoTotalServicos);
            System.out.print("  - Verificando slot: " + horarioAtual + " até " + horarioFimPotencial + " -> ");

            LocalTime finalHorarioAtual = horarioAtual;
            boolean temConflito = agendamentosDoDia.stream().anyMatch(agendamentoExistente -> {
                LocalTime inicioAgendamento = agendamentoExistente.getHorarioInicio().toLocalTime();
                LocalTime fimAgendamento = agendamentoExistente.getHorarioFim().toLocalTime();
                return finalHorarioAtual.isBefore(fimAgendamento) && horarioFimPotencial.isAfter(inicioAgendamento);
            });

            if (!temConflito) {
                System.out.println("Disponível!");
                horariosDisponiveis.add(horarioAtual);
            } else {
                System.out.println("Conflito Encontrado.");
            }

            horarioAtual = horarioAtual.plusMinutes(intervaloSlots);
        }

        System.out.println("--- FIM DA BUSCA DE HORÁRIOS ---");
        return horariosDisponiveis;
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

@Component
class AgendamentoEventListener {
    @Autowired
    private AgendamentoOrmPublisher rabbitmqPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAgendamentoSalvo(AgendamentoOrmSalvoEventDTO evento){
        rabbitmqPublisher.publicarAgendamento(evento.agendamentoId(), evento.tipoEvento());
    }

}
