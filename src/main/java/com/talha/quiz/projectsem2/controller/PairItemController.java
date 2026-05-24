package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.model.GraphPair;
import com.talha.quiz.projectsem2.util.Helper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class PairItemController {
    @FXML private Label nameLabel;
    @FXML private Label infoLabel;
    @FXML private Label hintLabel;
    @FXML private HBox root;

    private GraphPair pair;

    public void setPair(GraphPair pair, Stage stage) {
        this.pair = pair;

        nameLabel.setText(pair.getName());

        int vA = pair.getGraphA() != null ? pair.getGraphA().getNodes().size() : 0;
        int eA = pair.getGraphA() != null ? pair.getGraphA().getEdges().size() : 0;
        int vB = pair.getGraphB() != null ? pair.getGraphB().getNodes().size() : 0;
        int eB = pair.getGraphB() != null ? pair.getGraphB().getEdges().size() : 0;
        infoLabel.setText("A: " + vA + "V " + eA + "E  |  B: " + vB + "V " + eB + "E");

        root.setOnMouseClicked(e -> openPair(stage));
    }

    private void openPair(Stage stage) {
        GraphCanvas.preload = pair;
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/graph-canvas.fxml");
    }
}
