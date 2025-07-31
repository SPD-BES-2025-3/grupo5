package com.bookingbarber.sys.odm;

import com.bookingbarber.sys.messaging.event.AgendamentoOdmSalvoEventDTO;
import com.bookingbarber.sys.odm.dto.*;
import com.bookingbarber.sys.odm.entities.AgendamentoFlexODM;
import com.bookingbarber.sys.odm.entities.AgendamentoODM;
import com.bookingbarber.sys.odm.repositories.AgendamentoFlexODMRepository;
import com.bookingbarber.sys.odm.repositories.AgendamentoODMRespository;
import com.bookingbarber.sys.odm.service.AgendamentoODMService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoODMServiceTest {

    // Mocks para as dependências do serviço
    @Mock
    private AgendamentoFlexODMRepository agendamentoFlexRepository;

    @Mock
    private AgendamentoODMRespository agendamentoOdmRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AgendamentoODMService agendamentoOdmService;

    // Variáveis de teste
    private AgendamentoFlexOdmDTO flexRequestDTO;
    private AgendamentoOdmRequestDTO odmRequestDTO;
    private AgendamentoODM agendamentoOdm;

    @BeforeEach
    void setUp() {
        // Arrange: Cria objetos de mock comuns para os testes
        flexRequestDTO = new AgendamentoFlexOdmDTO(
                1L,
                "profissional@email.com",
                List.of("Corte", "Barba"),
                OffsetDateTime.now(ZoneOffset.of("-03:00")),
                Map.of("observacao", "cliente prefere maquina 2")
        );

        var clienteOdmDTO = new ClienteOdmDTO(1L, "Cliente Teste");
        var profissionalOdmDTO = new ProfissionalOdmDTO(1L, "Profissional Teste");
        var servicoOdmDTO = new ServicoOdmDTO(1L, "Corte Teste", new BigDecimal("50.00"), 30);

        odmRequestDTO = new AgendamentoOdmRequestDTO(
                100L,
                OffsetDateTime.now(ZoneOffset.of("-03:00")),
                clienteOdmDTO,
                profissionalOdmDTO,
                List.of(servicoOdmDTO)
        );

        agendamentoOdm = new AgendamentoODM();
        agendamentoOdm.setId("mongo-id-123");
        agendamentoOdm.setAgendamentoId(100L);
    }

    // --- Testes para o Fluxo de Agendamento Flexível (ODM -> ORM) ---
    @Nested
    @DisplayName("Testes para Agendamento Flexível (ODM -> ORM)")
    class AgendamentoFlexivelTests {

        @Test
        @DisplayName("Deve criar documento flexível e publicar evento com sucesso")
        void criarAgendamentoFlexivel_ComDadosValidos_DeveSalvarDocumentoEPublicarEvento() {
            // Arrange
            var docSalvo = new AgendamentoFlexODM();
            docSalvo.setId("flex-doc-id-456");
            when(agendamentoFlexRepository.save(any(AgendamentoFlexODM.class))).thenReturn(docSalvo);
            ArgumentCaptor<AgendamentoFlexODM> docCaptor = ArgumentCaptor.forClass(AgendamentoFlexODM.class);

            // Act
            agendamentoOdmService.criarAgendamentoFlexivel(flexRequestDTO);

            // Assert
            verify(agendamentoFlexRepository).save(docCaptor.capture());
            assertEquals("PENDENTE_NORMALIZACAO", docCaptor.getValue().getStatus());
            assertEquals("profissional@email.com", docCaptor.getValue().getProfissionalEmail());

            verify(eventPublisher, times(1)).publishEvent(any(AgendamentoOdmSalvoEventDTO.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar agendamento flexível com email do profissional em branco")
        void criarAgendamentoFlexivel_ComEmailProfissionalNulo_DeveLancarExcecao() {
            // Arrange
            var dtoInvalido = new AgendamentoFlexOdmDTO(1L, " ", List.of("Corte"), OffsetDateTime.now(), Map.of());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                agendamentoOdmService.criarAgendamentoFlexivel(dtoInvalido);
            });
            verify(agendamentoFlexRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    // --- Testes para o CRUD Administrativo do Modelo de Leitura (AgendamentoODM) ---
    @Nested
    @DisplayName("Testes para CRUD Administrativo (AgendamentoODM)")
    class CrudAdministrativoTests {

        @Test
        @DisplayName("Deve criar um documento ODM com sucesso")
        void criarAgendamentoOdm_ComDadosValidos_DeveRetornarResponseDTO() {
            // Arrange
            when(agendamentoOdmRepository.save(any(AgendamentoODM.class))).thenReturn(agendamentoOdm);

            // Act
            AgendamentoOdmResponseDTO resultado = agendamentoOdmService.criarAgendamentoOdm(odmRequestDTO);

            // Assert
            assertNotNull(resultado);
            assertEquals("mongo-id-123", resultado.id());
            verify(agendamentoOdmRepository, times(1)).save(any(AgendamentoODM.class));
        }

        @Test
        @DisplayName("Deve buscar um documento ODM por ID com sucesso")
        void findAgendamentoById_ComIdExistente_DeveRetornarDTO() {
            // Arrange
            when(agendamentoOdmRepository.findById("mongo-id-123")).thenReturn(Optional.of(agendamentoOdm));

            // Act
            AgendamentoOdmResponseDTO resultado = agendamentoOdmService.findAgendamentoById("mongo-id-123");

            // Assert
            assertNotNull(resultado);
            assertEquals(100L, resultado.agendamentoId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar documento ODM por ID inexistente")
        void findAgendamentoById_ComIdInexistente_DeveLancarExcecao() {
            // Arrange
            when(agendamentoOdmRepository.findById("id-invalido")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> {
                agendamentoOdmService.findAgendamentoById("id-invalido");
            });
        }

        @Test
        @DisplayName("Deve atualizar um documento ODM com sucesso")
        void atualizar_ComIdExistente_DeveSalvarDocumentoAtualizado() {
            // Arrange
            when(agendamentoOdmRepository.findById("mongo-id-123")).thenReturn(Optional.of(agendamentoOdm));
            when(agendamentoOdmRepository.save(any(AgendamentoODM.class))).thenReturn(agendamentoOdm);
            ArgumentCaptor<AgendamentoODM> captor = ArgumentCaptor.forClass(AgendamentoODM.class);

            // Act
            agendamentoOdmService.atualizar("mongo-id-123", odmRequestDTO);

            // Assert
            verify(agendamentoOdmRepository).save(captor.capture());
            assertEquals(100L, captor.getValue().getAgendamentoId());
        }

        @Test
        @DisplayName("Deve deletar um documento ODM com sucesso")
        void deletar_ComIdExistente_DeveChamarDelete() {
            // Arrange
            when(agendamentoOdmRepository.existsById("mongo-id-123")).thenReturn(true);
            doNothing().when(agendamentoOdmRepository).deleteById("mongo-id-123");

            // Act
            agendamentoOdmService.deletar("mongo-id-123");

            // Assert
            verify(agendamentoOdmRepository, times(1)).deleteById("mongo-id-123");
        }
    }
}