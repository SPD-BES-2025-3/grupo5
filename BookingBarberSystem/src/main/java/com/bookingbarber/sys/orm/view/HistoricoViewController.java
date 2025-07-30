package com.bookingbarber.sys.orm.view;

import com.bookingbarber.sys.javafx.UserSession;
import com.bookingbarber.sys.orm.dto.agendamento.AgendamentoResponseDTO;
import com.bookingbarber.sys.orm.service.AgendamentoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class HistoricoViewController {

    @FXML private Label tituloLabel;
    @FXML private TableView<AgendamentoResponseDTO> historicoTableView;
    @FXML private TableColumn<AgendamentoResponseDTO, String> colunaData; // Alterado para String para formatação
    @FXML private TableColumn<AgendamentoResponseDTO, String> colunaProfissional;
    @FXML private TableColumn<AgendamentoResponseDTO, String> colunaServicos; // Alterado para String
    @FXML private TableColumn<AgendamentoResponseDTO, BigDecimal> colunaValor;
    @FXML private TableColumn<AgendamentoResponseDTO, String> colunaStatus;
    @FXML private Button cancelarButton;
    @FXML private Label statusLabel;

    private final AgendamentoService agendamentoService;
    private final UserSession userSession;

    public HistoricoViewController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
        this.userSession = UserSession.getInstance();
    }

    @FXML
    public void initialize() {
        tituloLabel.setText("Histórico de " + userSession.getCliente().nome());

        // ==================================================================
        // CORREÇÃO: Configurando as colunas com expressões lambda
        // ==================================================================
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colunaData.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().horaInicio().format(formatter))
        );
        colunaProfissional.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().nomeProfissional())
        );
        colunaServicos.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join(", ", cellData.getValue().servicos()))
        );
        colunaValor.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().valorTotal())
        );
        colunaStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().status())
        );
        // ==================================================================

        carregarHistorico();
    }

    private void carregarHistorico() {
        statusLabel.setText("Carregando histórico...");
        new Thread(() -> {
            try {
                Long clienteId = userSession.getCliente().id();
                List<AgendamentoResponseDTO> historico = agendamentoService.listarPorCliente(clienteId);
                Platform.runLater(() -> {
                    historicoTableView.getItems().setAll(historico);
                    statusLabel.setText("");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Erro ao carregar histórico: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleCancelarAgendamento() {
        AgendamentoResponseDTO selecionado = historicoTableView.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            showError("Por favor, selecione um agendamento para cancelar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Você tem certeza que deseja cancelar o agendamento selecionado?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Cancelamento");
        alert.setHeaderText("Cancelar Agendamento");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                statusLabel.setText("Cancelando...");
                new Thread(() -> {
                    try {
                        agendamentoService.cancelar(selecionado.id());
                        Platform.runLater(() -> {
                            showSuccess("Agendamento cancelado com sucesso!");
                            carregarHistorico(); // Recarrega a lista para mostrar o status atualizado
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Erro ao cancelar: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleVoltar() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.RED);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.GREEN);
    }
}
