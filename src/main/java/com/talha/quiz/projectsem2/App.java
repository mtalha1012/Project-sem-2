package com.talha.quiz.projectsem2;

import com.talha.quiz.projectsem2.service.DatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        DatabaseService db = DatabaseService.getInstance();

        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource(
                "/com/talha/quiz/projectsem2/fxml/login.fxml"));
        Scene loginScene = new Scene(loginLoader.load());
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.setTitle("IsoGrapher");
        primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/com/talha/quiz/projectsem2/LOGO.png")));
        primaryStage.setMaximized(true);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            try {
                db.closeConnection();
            } catch (SQLException e) {
                throw new RuntimeException("Unable to close db connection", e);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
