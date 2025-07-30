package com.bookingbarber.sys.javafx.view;

import com.bookingbarber.sys.javafx.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MainViewController {

    @FXML
    private Label usuarioLogadoLabel;
    @FXML private TabPane mainTabPane;
    @FXML private AnchorPane ormContentPane;
    @FXML private AnchorPane odmContentPane;

    private final ApplicationContext context;

    public MainViewController(ApplicationContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        // Exibe o nome do usuário logado
        var cliente = UserSession.getInstance().getCliente();
        if (cliente != null) {
            usuarioLogadoLabel.setText("(Usuário: " + cliente.nome() + ")");
        }

        // Carrega a view ORM por padrão
        loadViewIntoPane("/orm/agendamento-orm-view.fxml", ormContentPane);

        // Adiciona um listener para carregar a view ODM quando a aba for selecionada
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().contains("ODM") && odmContentPane.getChildren().isEmpty()) {
                loadViewIntoPane("/odm/agendamento-odm-view.fxml", odmContentPane);
            }
        });
    }

    private void loadViewIntoPane(String fxmlPath, AnchorPane pane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean); // Essencial para injeção de dependência
            Node view = loader.load();

            // Configura a view para preencher todo o AnchorPane
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            pane.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
            // Em uma aplicação real, exibiríamos um erro mais amigável
            pane.getChildren().add(new Label("Erro ao carregar a view: " + fxmlPath));
        }
    }
}