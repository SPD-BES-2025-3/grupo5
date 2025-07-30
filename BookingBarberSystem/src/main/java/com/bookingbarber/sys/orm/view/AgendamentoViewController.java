package com.bookingbarber.sys.orm.view;

import com.bookingbarber.sys.javafx.UserSession;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.dto.profissional.ProfissionalResponseDTO;
import com.bookingbarber.sys.orm.dto.servico.ServicoResponseDTO;
import com.bookingbarber.sys.orm.service.AgendamentoService;
import com.bookingbarber.sys.orm.service.ProfissionalService;
import com.bookingbarber.sys.orm.service.ServicoService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgendamentoViewController {

    // --- Componentes da View (FXML) ---
    @FXML private Label clienteLogadoLabel;
    @FXML private ComboBox<ProfissionalResponseDTO> profissionalComboBox;
    @FXML private ListView<ServicoResponseDTO> servicosListView;
    @FXML private DatePicker dataPicker;
    @FXML private ComboBox<LocalTime> horarioComboBox;
    @FXML private Button buscarHorariosButton;
    @FXML private Button agendarButton;
    @FXML private Label statusLabel;

    @FXML private Button historicoButton;
    @FXML private Button logoutButton;

    // --- Injeção dos Serviços do Backend ---
    private final AgendamentoService agendamentoService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    private ClienteResponseDTO clienteLogado;

    private final ApplicationContext context; // Injetar o ApplicationContext

    public AgendamentoViewController(AgendamentoService agendamentoService, ProfissionalService profissionalService, ServicoService servicoService, ApplicationContext context) {
        this.agendamentoService = agendamentoService;
        this.profissionalService = profissionalService;
        this.servicoService = servicoService;
        this.context = context; // Armazenar o contexto
    }

    @FXML
    public void initialize() {
        this.clienteLogado = UserSession.getInstance().getCliente();
        if (clienteLogado == null) {
            showError("ERRO: Nenhum cliente logado.");
            disableAllControls();
            return;
        }
        clienteLogadoLabel.setText("Agendando como: " + clienteLogado.nome() + " " + clienteLogado.sobrenome());

        // Configura a ListView para permitir que o cliente selecione vários serviços
        servicosListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // ==================================================================
        // NOVA LÓGICA PARA CUSTOMIZAR A EXIBIÇÃO DA LISTVIEW
        // ==================================================================
        servicosListView.setCellFactory(listView -> new ListCell<ServicoResponseDTO>() {
            @Override
            protected void updateItem(ServicoResponseDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); // Se a célula estiver vazia, não mostra nada
                } else {
                    setText(item.nome()); // Mostra apenas o nome do serviço
                }
            }
        });
        // ==================================================================

        loadInitialData();
    }

    /**
     * Carrega os dados de profissionais e TODOS os serviços na inicialização.
     */
    private void loadInitialData() {
        new Thread(() -> {
            try {
                // Chama os serviços do backend diretamente para buscar os dados
                List<ProfissionalResponseDTO> profissionais = profissionalService.findAllProfissionais(Pageable.unpaged()).getContent();
                List<ServicoResponseDTO> todosOsServicos = servicoService.findAllServicos(Pageable.unpaged()).getContent();

                // Atualiza a interface gráfica na thread do JavaFX
                Platform.runLater(() -> {
                    profissionalComboBox.getItems().addAll(profissionais);
                    servicosListView.getItems().addAll(todosOsServicos); // Popula a lista com todos os serviços
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Falha ao carregar dados: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleBuscarHorarios() {
        ProfissionalResponseDTO profissional = profissionalComboBox.getValue();
        // Pega a lista de todos os serviços que o usuário selecionou
        ObservableList<ServicoResponseDTO> servicosSelecionados = servicosListView.getSelectionModel().getSelectedItems();
        LocalDate data = dataPicker.getValue();

        if (profissional == null || servicosSelecionados.isEmpty() || data == null) {
            showError("Selecione profissional, data e pelo menos um serviço.");
            return;
        }

        statusLabel.setText("Buscando horários disponíveis...");
        List<Long> servicoIds = servicosSelecionados.stream().map(ServicoResponseDTO::id).collect(Collectors.toList());

        new Thread(() -> {
            try {
                // Chama o serviço do backend para obter a lista de horários
                List<LocalTime> horarios = agendamentoService.buscarHorariosDisponiveis(profissional.id(), servicoIds, data);
                Platform.runLater(() -> {
                    if (horarios.isEmpty()) {
                        showInfo("Nenhum horário disponível para esta combinação.");
                        horarioComboBox.getItems().clear();
                        horarioComboBox.setDisable(true);
                        agendarButton.setDisable(true);
                    } else {
                        horarioComboBox.getItems().setAll(horarios);
                        horarioComboBox.setDisable(false);
                        agendarButton.setDisable(false);
                        statusLabel.setText("Horários encontrados. Selecione um para continuar.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erro ao buscar horários: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleAgendar() {
        ProfissionalResponseDTO profissional = profissionalComboBox.getValue();
        ObservableList<ServicoResponseDTO> servicosSelecionados = servicosListView.getSelectionModel().getSelectedItems();
        LocalDate data = dataPicker.getValue();
        LocalTime hora = horarioComboBox.getValue();

        if (clienteLogado == null || profissional == null || servicosSelecionados.isEmpty() || data == null || hora == null) {
            showError("Todos os campos são obrigatórios para confirmar.");
            return;
        }

        statusLabel.setText("Processando agendamento...");
        agendarButton.setDisable(true);

        List<Long> servicoIds = servicosSelecionados.stream().map(ServicoResponseDTO::id).collect(Collectors.toList());
        OffsetDateTime horario = OffsetDateTime.of(data, hora, ZoneOffset.of("-03:00"));
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(this.clienteLogado.id(), profissional.id(), horario, servicoIds);

        new Thread(() -> {
            try {
                // Chama o serviço do backend para criar o agendamento
                agendamentoService.insertAgendamento(request);
                Platform.runLater(() -> {
                    showSuccess("Agendamento criado com sucesso!");
                    resetFields();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Falha ao agendar: " + e.getMessage());
                    agendarButton.setDisable(false);
                });
            }
        }).start();
    }
    @FXML
    private void handleAbrirHistorico() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/orm/historico.fxml"));
            fxmlLoader.setControllerFactory(context::getBean);

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Histórico de Agendamentos");
            stage.setScene(scene);

            // Modality.APPLICATION_MODAL impede o usuário de interagir com a tela de agendamento
            // enquanto a tela de histórico estiver aberta.
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro ao abrir o histórico.");
        }
    }

    @FXML
    private void handleLogout() {
        // 1. Limpa a sessão do usuário
        UserSession.getInstance().cleanUserSession();

        try {
            // 2. Abre a tela de login novamente
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
            fxmlLoader.setControllerFactory(context::getBean);

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Login - Sistema de Agendamento");
            stage.setScene(scene);

            // 3. Fecha a janela de agendamento atual
            Stage agendamentoStage = (Stage) logoutButton.getScene().getWindow();
            agendamentoStage.close();

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetFields() {
        profissionalComboBox.getSelectionModel().clearSelection();
        servicosListView.getSelectionModel().clearSelection();
        dataPicker.setValue(null);
        horarioComboBox.getItems().clear();
        horarioComboBox.setDisable(true);
        agendarButton.setDisable(true);
    }

    private void disableAllControls() {
        profissionalComboBox.setDisable(true);
        servicosListView.setDisable(true);
        dataPicker.setDisable(true);
        horarioComboBox.setDisable(true);
        buscarHorariosButton.setDisable(true);
        agendarButton.setDisable(true);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
    }

    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.BLUE);
    }
}
