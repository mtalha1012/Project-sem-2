package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.service.AuthService;
import com.talha.quiz.projectsem2.util.Helper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        emailField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                usernameField.requestFocus();
        });
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                passwordField.requestFocus();
        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                confirmPasswordField.requestFocus();
        });
        confirmPasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER)
                handleRegister();
        });
    }
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        try {
            AuthService.registerUser(username, password, email);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            // Navigate back to login after successful registration
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/login.fxml");
        } catch (RuntimeException e) {
            showError("Registration failed. Username or email may already exist.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    public void handleLoginLink(ActionEvent event) {
        Helper.changeScene(event, "/com/talha/quiz/projectsem2/fxml/login.fxml");
    }
}
