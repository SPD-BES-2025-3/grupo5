package com.bookingbarber.sys.odm.view;

import com.bookingbarber.sys.javafx.UserSession;
import com.bookingbarber.sys.odm.dto.AgendamentoFlexOdmDTO;
import com.bookingbarber.sys.odm.service.AgendamentoODMService;
import com.bookingbarber.sys.orm.dto.servico.ServicoResponseDTO;
import com.bookingbarber.sys.orm.service.ServicoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AgendamentoFlexOdmController {

    // Classe interna para a TableView de atributos dinâmicos
    public static class AtributoDinamico {
        private final SimpleStringProperty chave;
        private final SimpleStringProperty valor;
        public AtributoDinamico(String chave, String valor) { this.chave = new SimpleStringProperty(chave); this.valor = new SimpleStringProperty(valor); }
        public String getChave() { return chave.get(); }
        public String getValor() { return valor.get(); }
    }

    @FXML private TextField profissionalEmailField;
    @FXML private ListView<ServicoResponseDTO> servicosListView;
    @FXML private DatePicker dataPicker;
    @FXML private TextField horaField;
    @FXML private TableView<AtributoDinamico> atributosTableView;
    @FXML private TableColumn<AtributoDinamico, String> chaveColumn;
    @FXML private TableColumn<AtributoDinamico, String> valorColumn;
    @FXML private TextField chaveField;
    @FXML private TextField valorField;
    @FXML private Label statusLabel;

    private final AgendamentoODMService agendamentoOdmService;
    private final ServicoService servicoService;
    private final ObservableList<AtributoDinamico> atributos = FXCollections.observableArrayList();

    public AgendamentoFlexOdmController(AgendamentoODMService agendamentoOdmService, ServicoService servicoService) {
        this.agendamentoOdmService = agendamentoOdmService;
        this.servicoService = servicoService;
    }

    @FXML
    public void initialize() {
        // Configura a tabela de atributos dinâmicos
        atributosTableView.setItems(atributos);
        chaveColumn.setCellValueFactory(cellData -> cellData.getValue().chave);
        valorColumn.setCellValueFactory(cellData -> cellData.getValue().valor);

        // CORREÇÃO: Configura a seleção múltipla e a exibição da ListView aqui
        servicosListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        servicosListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(ServicoResponseDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nome());
            }
        });

        // Carrega a lista de serviços do backend
        loadInitialData();
    }

    private void loadInitialData() {
        new Thread(() -> {
            try {
                List<ServicoResponseDTO> todosOsServicos = servicoService.findAllServicos(Pageable.unpaged()).getContent();
                Platform.runLater(() -> servicosListView.getItems().setAll(todosOsServicos));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Falha ao carregar lista de serviços: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleAddAtributo() {
        String chave = chaveField.getText();
        String valor = valorField.getText();
        if (!chave.isBlank()) {
            atributos.add(new AtributoDinamico(chave, valor));
            chaveField.clear();
            valorField.clear();
        }
    }

    @FXML
    private void handleAgendarOdm() {
        try {
            String profissionalEmail = profissionalEmailField.getText();
            ObservableList<ServicoResponseDTO> servicosSelecionados = servicosListView.getSelectionModel().getSelectedItems();
            LocalDate data = dataPicker.getValue();
            LocalTime hora = LocalTime.parse(horaField.getText());

            if (profissionalEmail.isBlank() || servicosSelecionados.isEmpty() || data == null) {
                showError("Email, serviços e data são obrigatórios.");
                return;
            }

            List<String> servicosNomes = servicosSelecionados.stream()
                    .map(ServicoResponseDTO::nome)
                    .toList();

            Map<String, String> atributosDinamicos = atributos.stream()
                    .collect(Collectors.toMap(AtributoDinamico::getChave, AtributoDinamico::getValor));

            Long clienteId = UserSession.getInstance().getCliente().id();
            OffsetDateTime dataHora = OffsetDateTime.of(data, hora, ZoneOffset.of("-03:00"));
            var request = new AgendamentoFlexOdmDTO(clienteId, profissionalEmail, servicosNomes, dataHora, atributosDinamicos);

            statusLabel.setText("Processando agendamento...");

            new Thread(() -> {
                try {
                    agendamentoOdmService.criarAgendamentoFlexivel(request);
                    Platform.runLater(() -> {
                        showSuccess("Agendamento recebido! Sincronização ocorrerá em breve.");
                        resetFields();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Erro ao processar: " + e.getMessage()));
                }
            }).start();

        } catch (DateTimeParseException e) {
            showError("Formato de hora inválido. Use HH:mm.");
        } catch (Exception e) {
            showError("Erro inesperado: " + e.getMessage());
        }
    }

    private void resetFields() {
        profissionalEmailField.clear();
        servicosListView.getSelectionModel().clearSelection();
        dataPicker.setValue(null);
        horaField.clear();
        atributos.clear();
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
