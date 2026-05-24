package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.model.Edge;
import com.talha.quiz.projectsem2.model.Graph;
import com.talha.quiz.projectsem2.model.Node;
import com.talha.quiz.projectsem2.model.Result;
import com.talha.quiz.projectsem2.service.IsomorphismService;
import com.talha.quiz.projectsem2.service.QuizService;
import com.talha.quiz.projectsem2.util.ColorLoader;
import com.talha.quiz.projectsem2.util.Helper;
import com.talha.quiz.projectsem2.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private static final int SECONDS_PER_QUESTION = 40;

    // Attributes — FXML
    @FXML private Canvas quizCanvas1;
    @FXML private Canvas quizCanvas2;
    @FXML private Pane quizPane1;
    @FXML private Pane quizPane2;
    @FXML private Label questionCounterLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label timerLabel;
    @FXML private Button yesBtn;
    @FXML private Button noBtn;
    @FXML private Label quizResultLabel;
    @FXML private Button backButton;

    // Attributes — quiz state
    private final List<QuizPair> quizPairs = buildQuizPairs();
    private int currentIndex = 0;
    private int score = 0;
    private Timeline timer;
    private int secondsLeft;
    private boolean answered = false;

    // package-visible so Results can read it
    static Result lastResult;

    // Represents one visual quiz question — adjacency lists only, positions computed at render time
    private record QuizPair(int nA, int[][] edgesA, int nB, int[][] edgesB) {}

    // Constructor (FXML loader handles instantiation)

    // Methods
    @FXML
    public void initialize() {
        quizCanvas1.widthProperty().bind(quizPane1.widthProperty());
        quizCanvas1.heightProperty().bind(quizPane1.heightProperty());
        quizCanvas2.widthProperty().bind(quizPane2.widthProperty());
        quizCanvas2.heightProperty().bind(quizPane2.heightProperty());

        quizPane1.widthProperty().addListener((obs, o, n) -> drawCurrent());
        quizPane1.heightProperty().addListener((obs, o, n) -> drawCurrent());
        quizPane2.widthProperty().addListener((obs, o, n) -> drawCurrent());
        quizPane2.heightProperty().addListener((obs, o, n) -> drawCurrent());

        showQuestion();
    }

    private void showQuestion() {
        if (currentIndex >= quizPairs.size()) { finishQuiz(); return; }

        answered = false;
        quizResultLabel.setText("");
        quizResultLabel.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        setAnswerButtonsDisabled(false);
        questionCounterLabel.setText("Question " + (currentIndex + 1) + " / " + quizPairs.size());
        progressBar.setProgress((double) currentIndex / quizPairs.size());
        drawCurrent();
        startTimer();
    }

    private void drawCurrent() {
        if (currentIndex >= quizPairs.size()) return;
        QuizPair pair = quizPairs.get(currentIndex);
        Graph gA = buildGraph(pair.nA(), pair.edgesA(), quizCanvas1);
        Graph gB = buildGraph(pair.nB(), pair.edgesB(), quizCanvas2);
        drawGraph(quizCanvas1, gA);
        drawGraph(quizCanvas2, gB);
    }

    // Build a Graph with nodes placed in a circle sized to fit the given canvas
    private Graph buildGraph(int n, int[][] edges, Canvas canvas) {
        double w = canvas.getWidth() > 10 ? canvas.getWidth() : 400;
        double h = canvas.getHeight() > 10 ? canvas.getHeight() : 300;
        double cx = w / 2, cy = h / 2;
        double r = Math.min(cx, cy) * 0.72;

        Graph g = new Graph();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            nodes.add(new Node(String.valueOf(i + 1), cx + r * Math.cos(angle), cy + r * Math.sin(angle)));
        }
        g.setNodes(nodes);
        for (int[] e : edges) g.getEdges().add(new Edge(nodes.get(e[0]), nodes.get(e[1]), null));
        return g;
    }

    // Minimal graph renderer — read-only, no interaction needed
    private void drawGraph(Canvas canvas, Graph graph) {
        double w = canvas.getWidth(), h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(ColorLoader.getCanvasBackground());
        gc.fillRect(0, 0, w, h);

        gc.setStroke(ColorLoader.getGridColor());
        gc.setLineWidth(0.5);
        for (double x = 0; x <= w; x += 40) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y <= h; y += 40) gc.strokeLine(0, y, w, y);

        gc.setStroke(ColorLoader.getEdgeColor());
        gc.setLineWidth(2);
        for (Edge e : graph.getEdges()) {
            gc.strokeLine(e.getSourceNode().getX(), e.getSourceNode().getY(),
                          e.getTargetNode().getX(), e.getTargetNode().getY());
        }

        final double R = 18;
        for (Node n : graph.getNodes()) {
            gc.setFill(ColorLoader.getNodeColor());
            gc.fillOval(n.getX() - R, n.getY() - R, R * 2, R * 2);
            gc.setStroke(Color.web("#052e16"));
            gc.setLineWidth(2);
            gc.strokeOval(n.getX() - R, n.getY() - R, R * 2, R * 2);
            gc.setFill(ColorLoader.getLabelColor());
            gc.setFont(Font.font("System", FontWeight.BOLD, 12));
            gc.fillText(n.getLabel(), n.getX() - (n.getLabel().length() * 3.5), n.getY() + 4.5);
        }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void startTimer() {
        if (timer != null) timer.stop();
        secondsLeft = SECONDS_PER_QUESTION;
        timerLabel.setText(secondsLeft + "s");
        timerLabel.getStyleClass().removeAll("timer-label-critical");
        timerLabel.getStyleClass().add("timer-label-normal");
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsLeft--;
            timerLabel.setText(secondsLeft + "s");
            if (secondsLeft <= 10) {
                timerLabel.getStyleClass().remove("timer-label-normal");
                timerLabel.getStyleClass().add("timer-label-critical");
            }
            if (secondsLeft <= 0) { timer.stop(); if (!answered) timeOut(); }
        }));
        timer.setCycleCount(SECONDS_PER_QUESTION);
        timer.play();
    }

    private void timeOut() {
        answered = true;
        setAnswerButtonsDisabled(true);
        quizResultLabel.setText("Time's up! The correct answer has been skipped.");
        quizResultLabel.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        quizResultLabel.getStyleClass().add("fail-text");
        scheduleNext();
    }

    // ── Answer handling ───────────────────────────────────────────────────────

    @FXML
    public void handleYes() { submitAnswer(true); }

    @FXML
    public void handleNo() { submitAnswer(false); }

    private void submitAnswer(boolean userSaysYes) {
        if (answered) return;
        answered = true;
        if (timer != null) timer.stop();
        setAnswerButtonsDisabled(true);

        QuizPair pair = quizPairs.get(currentIndex);
        Graph gA = buildGraph(pair.nA(), pair.edgesA(), quizCanvas1);
        Graph gB = buildGraph(pair.nB(), pair.edgesB(), quizCanvas2);
        boolean actuallyIsomorphic = IsomorphismService.check(gA, gB).isIsomorphic();

        quizResultLabel.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        if (userSaysYes == actuallyIsomorphic) {
            score++;
            quizResultLabel.setText("✓  Correct!  " +
                    (actuallyIsomorphic ? "These graphs ARE isomorphic." : "These graphs are NOT isomorphic."));
            quizResultLabel.getStyleClass().add("pass-text");
        } else {
            quizResultLabel.setText("✗  Wrong.  " +
                    (actuallyIsomorphic ? "They ARE isomorphic." : "They are NOT isomorphic."));
            quizResultLabel.getStyleClass().add("fail-text");
        }
        scheduleNext();
    }

    private void scheduleNext() {
        new Timeline(new KeyFrame(Duration.seconds(1.8), e -> {
            currentIndex++;
            showQuestion();
        })).play();
    }

    private void setAnswerButtonsDisabled(boolean disabled) {
        yesBtn.setDisable(disabled);
        noBtn.setDisable(disabled);
    }

    // ── Finish ────────────────────────────────────────────────────────────────

    private void finishQuiz() {
        if (timer != null) timer.stop();
        Integer userId = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getId() : null;
        Result result = new Result(userId, null, score, quizPairs.size());
        lastResult = result;
        if (SessionManager.isLoggedIn()) QuizService.saveResult(result);
        Stage stage = (Stage) backButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/results.fxml");
    }

    @FXML
    public void handleBack() {
        if (timer != null) timer.stop();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/dashboard.fxml");
    }

    // ── Quiz pair definitions ─────────────────────────────────────────────────
    // All pairs must have the same vertex count AND edge count so counting alone won't reveal the answer.
    // Verified: same n and |E| for each (A, B) in each pair.

    private static List<QuizPair> buildQuizPairs() {
        List<QuizPair> q = new ArrayList<>();

        // Q1 — ISOMORPHIC: both are C6 (6-cycle), different edge order
        q.add(new QuizPair(
            6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}},
            6, new int[][]{{0,2},{2,4},{4,1},{1,3},{3,5},{5,0}}
        ));

        // Q2 — NOT isomorphic: C6 (connected) vs two disjoint triangles (disconnected) — both 6V, 6E
        q.add(new QuizPair(
            6, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,0}},
            6, new int[][]{{0,1},{1,2},{2,0},{3,4},{4,5},{5,3}}
        ));

        // Q3 — ISOMORPHIC: K3,3 vs K3,3 relabeled — 6V, 9E
        // G1: {0,1,2} each connected to {3,4,5}
        // G2: bipartition {0,2,4} connected to {1,3,5}
        q.add(new QuizPair(
            6, new int[][]{{0,3},{0,4},{0,5},{1,3},{1,4},{1,5},{2,3},{2,4},{2,5}},
            6, new int[][]{{0,1},{0,3},{0,5},{2,1},{2,3},{2,5},{4,1},{4,3},{4,5}}
        ));

        // Q4 — NOT isomorphic: K3,3 (bipartite, no triangles) vs triangular prism (has triangles) — 6V, 9E
        // Prism: two triangles {0,1,2} and {3,4,5} bridged by {0-3,1-4,2-5}
        q.add(new QuizPair(
            6, new int[][]{{0,3},{0,4},{0,5},{1,3},{1,4},{1,5},{2,3},{2,4},{2,5}},
            6, new int[][]{{0,1},{1,2},{2,0},{3,4},{4,5},{5,3},{0,3},{1,4},{2,5}}
        ));

        // Q5 — ISOMORPHIC: Star K1,5 vs K1,5 with center relabeled — 6V, 5E
        // G1: center = node 0, leaves = 1-5
        // G2: center = node 5, leaves = 0-4
        q.add(new QuizPair(
            6, new int[][]{{0,1},{0,2},{0,3},{0,4},{0,5}},
            6, new int[][]{{5,0},{5,1},{5,2},{5,3},{5,4}}
        ));

        // Q6 — NOT isomorphic: path P7 vs caterpillar T7 — both 7V, 6E (both trees, different structure)
        // P7: 0-1-2-3-4-5-6
        // Caterpillar: spine 0-1-2-3-4, branches: 5 off node 1, 6 off node 3
        q.add(new QuizPair(
            7, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6}},
            7, new int[][]{{0,1},{1,2},{2,3},{3,4},{1,5},{3,6}}
        ));

        return q;
    }
}
