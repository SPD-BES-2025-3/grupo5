package com.bookingbarber.sys.config;


import com.bookingbarber.sys.JavaFxApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StageInitializer implements ApplicationListener<JavaFxApplication.StageReadyEvent> {

    private final ApplicationContext applicationContext;

    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(JavaFxApplication.StageReadyEvent event) {
        try {
            Stage stage = event.getStage();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login.fxml"));
            fxmlLoader.setControllerFactory(applicationContext::getBean); // MÁGICA ACONTECE AQUI!

            Scene scene = new Scene(fxmlLoader.load(), 400, 250);
            stage.setTitle("Login - Sistema de Agendamento");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar a view FXML.", e);
        }
    }
}
