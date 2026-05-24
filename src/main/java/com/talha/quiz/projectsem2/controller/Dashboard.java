package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.model.GraphPair;
import com.talha.quiz.projectsem2.service.GraphPairService;
import com.talha.quiz.projectsem2.util.Helper;
import com.talha.quiz.projectsem2.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class Dashboard {
    // Attributes
    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private ListView<GraphPair> savedPairsListView;
    @FXML private Label savedPairsSectionLabel;

    // Methods
    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            usernameLabel.setText("Welcome back, " + SessionManager.getCurrentUser().getUsername());
//            loadSavedPairs();
        } else {
            usernameLabel.setText("Guest — log in to save graphs");
            savedPairsSectionLabel.setVisible(false);
            savedPairsSectionLabel.setManaged(false);
            savedPairsListView.setVisible(false);
            savedPairsListView.setManaged(false);
        }
    }

//    private void loadSavedPairs() {
//        List<GraphPair> pairs = GraphPairService.getUserGraphPairs(
//                SessionManager.getCurrentUser().getId());
//
//        if (pairs.isEmpty()) {
//            Label empty = new Label("No saved pairs yet.");
//            empty.getStyleClass().add("pair-empty-message");
//            savedPairsListView.setPlaceholder(empty);
//        }
//
//        savedPairsListView.getItems().addAll(pairs);
//        Stage stage = (Stage) logoutButton.getScene().getWindow();
//        savedPairsListView.setCellFactory(lv -> new PairListCell(stage));
//    }

    private class PairListCell extends ListCell<GraphPair> {
        private final Stage stage;

        PairListCell(Stage stage) {
            this.stage = stage;
        }

        @Override
        protected void updateItem(GraphPair pair, boolean empty) {
            super.updateItem(pair, empty);
            if (empty || pair == null) {
                setGraphic(null);
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/talha/quiz/projectsem2/fxml/pair-item.fxml"));
                    javafx.scene.Node node = loader.load();
                    PairItemController controller = loader.getController();
                    controller.setPair(pair, stage);
                    setGraphic(node);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void handleGraphCanvas() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/graph-canvas.fxml");
    }

    @FXML
    public void handleQuiz() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/quiz.fxml");
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/login.fxml");
    }
}
