package com.talha.quiz.projectsem2.util;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Helper {

    // Methods
    public static Stage changeScene(Event event, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(Helper.class.getResource(file));
            Stage window = (Stage) (((Node) event.getSource()).getScene().getWindow());
            window.setMaximized(true);
            return applyScene(window, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Stage changeScene(Stage window, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(Helper.class.getResource(file));
            window.setMaximized(true);
            return applyScene(window, loader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Stage applyScene(Stage window, FXMLLoader loader) throws IOException {
        Scene currentScene = window.getScene();
        if (currentScene != null) {
            // Update the root of the existing scene
            currentScene.setRoot(loader.load());
        } else {
            // Create a new scene if none exists
            Scene newScene = new Scene(loader.load());
            window.setScene(newScene);
        }
        window.setMaximized(true);
        return window;
    }
}
