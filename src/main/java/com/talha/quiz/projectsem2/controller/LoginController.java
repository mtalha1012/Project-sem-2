package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.service.AuthService;
import com.talha.quiz.projectsem2.util.Helper;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                passwordField.requestFocus();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                handleLogin(event);
        });
    }

    @FXML
    public void handleLogin(Event event) {
        try {
            if (AuthService.login(usernameField.getText(), passwordField.getText())) {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                Helper.changeScene(event, "/com/talha/quiz/projectsem2/fxml/dashboard.fxml");
            } else {
                errorLabel.setText("Invalid username or password.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        } catch(RuntimeException e) {
            errorLabel.setText("Login failed. Please try again.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    @FXML
    public void handleRegisterLink(ActionEvent event) {
        Helper.changeScene(event, "/com/talha/quiz/projectsem2/fxml/register.fxml");
    }
}
