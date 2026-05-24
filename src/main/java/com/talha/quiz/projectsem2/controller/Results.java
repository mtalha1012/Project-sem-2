package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.model.Result;
import com.talha.quiz.projectsem2.util.Helper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Results {
    @FXML private Label scoreLabel;
    @FXML private Label scoreSubtitleLabel;
    @FXML private Label feedbackLabel;
    @FXML private Button tryAgainButton;
    @FXML private Button dashboardButton;

    @FXML
    public void initialize() {
        Result result = Quiz.lastResult;
        if (result == null) {
            scoreLabel.setText("—");
            scoreSubtitleLabel.setText("No result available.");
            feedbackLabel.setText("");
            return;
        }

        int score = result.getScore();
        int total = result.getTotalQuestions();
        int percent = total > 0 ? (score * 100) / total : 0;

        scoreLabel.setText(score + " / " + total);
        scoreSubtitleLabel.setText(percent + "% correct");

        feedbackLabel.getStyleClass().removeAll("feedback-pass", "feedback-fail");

        if (percent >= 80) {
            feedbackLabel.setText("Excellent work! You have a strong grasp of graph theory.");
            feedbackLabel.getStyleClass().add("feedback-pass");
        } else if (percent >= 50) {
            feedbackLabel.setText("Good effort! Review the topics you missed and try again.");
            feedbackLabel.getStyleClass().add("feedback-pass");
        } else {
            feedbackLabel.setText("Keep practicing! Graph theory takes time to master.");
            feedbackLabel.getStyleClass().add("feedback-fail");
        }
    }

    @FXML
    public void handleTryAgain() {
        Stage stage = (Stage) tryAgainButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/quiz.fxml");
    }

    @FXML
    public void handleDashboard() {
        Stage stage = (Stage) dashboardButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/dashboard.fxml");
    }
}
