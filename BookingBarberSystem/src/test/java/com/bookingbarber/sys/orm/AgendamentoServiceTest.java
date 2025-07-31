package com.bookingbarber.sys.orm;

import com.bookingbarber.sys.messaging.event.AgendamentoOrmSalvoEventDTO;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoResponseDTO;
import com.bookingbarber.sys.orm.entities.*;
import com.bookingbarber.sys.orm.entities.enums.StatusAgendamento;
import com.bookingbarber.sys.orm.repositories.AgendamentoRepository;
import com.bookingbarber.sys.orm.repositories.ClienteRepository;
import com.bookingbarber.sys.orm.repositories.ProfissionalRepository;
import com.bookingbarber.sys.orm.repositories.ServicoRepository;
import com.bookingbarber.sys.orm.service.AgendamentoService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProfissionalRepository profissionalRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Cliente cliente;
    private Profissional profissional;
    private Servico servico;
    private Agendamento agendamento;
    private static final ZoneOffset FUSO_HORARIO_TESTE = ZoneOffset.of("-03:00");

    @BeforeEach
    void setUp() {
        // Arrange: Cria objetos de mock comuns para os testes
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Cliente Teste");

        profissional = new Profissional();
        profissional.setId(1L);
        profissional.setNome("Profissional Teste");

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte Teste");
        servico.setDuracao(30);
        servico.setValor(new BigDecimal("50.00"));

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setHorarioInicio(OffsetDateTime.now(FUSO_HORARIO_TESTE).plusHours(5));
        agendamento.setHorarioFim(OffsetDateTime.now(FUSO_HORARIO_TESTE).plusHours(5).plusMinutes(30));
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setValorTotal(new BigDecimal("50.00"));

        ServicoAgendado servicoAgendado = new ServicoAgendado();
        servicoAgendado.setServico(servico);
        agendamento.setServicosAgendados(Set.of(servicoAgendado));
    }


    @Test
    @DisplayName("Deve lançar exceção ao agendar com cliente inexistente")
    void agendar_ComClienteInexistente_DeveLancarEntityNotFoundException() {
        // Arrange
        OffsetDateTime horarioValido = LocalDate.now().plusDays(1).atTime(10, 0).atOffset(FUSO_HORARIO_TESTE);

        var requestDTO = new AgendamentoRequestDTO(99L, 1L, horarioValido, List.of(1L));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            agendamentoService.insertAgendamento(requestDTO);
        });
        verify(agendamentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve agendar com sucesso quando todos os dados são válidos")
    void agendar_ComDadosValidos_DeveRetornarDTO() {
        // Arrange
        OffsetDateTime horarioValido = LocalDate.now().plusDays(1).atTime(10, 0).atOffset(FUSO_HORARIO_TESTE);
        var requestDTO = new AgendamentoRequestDTO(1L, 1L, horarioValido, List.of(1L));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(servicoRepository.findAllById(List.of(1L))).thenReturn(List.of(servico));
        when(agendamentoRepository.findOverlappingAppointments(any(), any(), any())).thenReturn(Collections.emptyList());

        // Simula o retorno do save para ter um objeto completo para o mapeamento
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento arg = invocation.getArgument(0);
            arg.setId(1L); // Simula a geração de ID pelo banco
            return arg;
        });

        // Act
        AgendamentoResponseDTO resultado = agendamentoService.insertAgendamento(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Cliente Teste", resultado.nomeCliente());
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
        // CORREÇÃO: Verificação do evento agora usa o tipo correto 'AgendamentoOrmSalvoEventDTO'.
        verify(eventPublisher, times(1)).publishEvent(any(AgendamentoOrmSalvoEventDTO.class));
    }

    @Test
    @DisplayName("Deve buscar agendamento por ID com sucesso")
    void buscarPorId_ComIdExistente_DeveRetornarDTO() {
        // Arrange
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        // Act
        AgendamentoResponseDTO resultado = agendamentoService.buscarAgendamentoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Corte Teste", resultado.servicos().get(0)); // Verifica se o serviço foi mapeado
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar por ID inexistente")
    void buscarPorId_ComIdInexistente_DeveLancarEntityNotFoundException() {
        // Arrange
        when(agendamentoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            agendamentoService.buscarAgendamentoPorId(99L);
        });
    }

    @Test
    @DisplayName("Deve cancelar um agendamento com sucesso")
    void cancelar_ComAgendamentoValido_DeveMudarStatusParaCancelado() {
        // Arrange
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));
        ArgumentCaptor<Agendamento> agendamentoCaptor = ArgumentCaptor.forClass(Agendamento.class);

        // Act
        agendamentoService.cancelar(1L);

        // Assert
        verify(agendamentoRepository, times(1)).save(agendamentoCaptor.capture());
        assertEquals(StatusAgendamento.CANCELADO, agendamentoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Deve lançar exceção ao cancelar agendamento com pouca antecedência")
    void cancelar_ComPoucaAntecedencia_DeveLancarIllegalArgumentException() {
        // Arrange
        // Seta o horário para apenas 1 hora no futuro (menos que o mínimo de 2 horas)
        agendamento.setHorarioInicio(OffsetDateTime.now(FUSO_HORARIO_TESTE).plusHours(1));
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            agendamentoService.cancelar(1L);
        });
    }
}
