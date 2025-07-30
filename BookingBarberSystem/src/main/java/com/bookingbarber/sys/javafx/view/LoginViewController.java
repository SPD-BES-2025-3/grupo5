package com.bookingbarber.sys.javafx.view;

import com.bookingbarber.sys.javafx.UserSession;
import com.bookingbarber.sys.orm.dto.auth.LoginRequestDTO;
import com.bookingbarber.sys.orm.dto.cliente.ClienteResponseDTO;
import com.bookingbarber.sys.orm.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;


@Component
public class LoginViewController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final AuthService authService;
    private final ApplicationContext context;

    public LoginViewController(AuthService authService, ApplicationContext context) {
        this.authService = authService;
        this.context = context;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            statusLabel.setText("Email e senha são obrigatórios.");
            return;
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        statusLabel.setText("Autenticando...");

        // Executa a lógica de negócio em uma thread separada para não travar a UI
        new Thread(() -> {
            try {
                // CHAMA O SERVIÇO DO BACKEND DIRETAMENTE
                ClienteResponseDTO clienteLogado = authService.loginCliente(loginRequest);

                // Armazena o cliente logado na sessão
                UserSession.getInstance().setCliente(clienteLogado);

                // Pede para a thread da UI abrir a próxima tela
                Platform.runLater(this::abrirTelaDeAgendamento);

            } catch (Exception e) {
                // Em caso de erro, atualiza a UI com a mensagem
                Platform.runLater(() -> statusLabel.setText("Falha no login: " + e.getMessage()));
            }
        }).start();
    }

    private void abrirTelaDeAgendamento() {
        // ESTE MÉTODO AGORA ABRE O FRAME PRINCIPAL
        try {
            String fxmlPath = "/main-view.fxml"; // <-- MUDANÇA AQUI
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) throw new IOException("Arquivo FXML não encontrado: " + fxmlPath);

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            fxmlLoader.setControllerFactory(context::getBean);

            Scene scene = new Scene(fxmlLoader.load()); // O tamanho é definido no FXML
            Stage stage = new Stage();
            stage.setTitle("Booking Barber System"); // Título da janela principal
            stage.setScene(scene);

            Stage loginStage = (Stage) statusLabel.getScene().getWindow();
            loginStage.close();

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Erro crítico ao carregar a tela principal."));
        }
    }
}