package com.talha.quiz.projectsem2.controller;

import com.talha.quiz.projectsem2.model.Edge;
import com.talha.quiz.projectsem2.model.Graph;
import com.talha.quiz.projectsem2.model.GraphPair;
import com.talha.quiz.projectsem2.model.IsomorphismResult;
import com.talha.quiz.projectsem2.model.Node;
import com.talha.quiz.projectsem2.service.GraphPairService;
import com.talha.quiz.projectsem2.service.IsomorphismService;
import com.talha.quiz.projectsem2.util.ColorLoader;
import com.talha.quiz.projectsem2.util.Helper;
import com.talha.quiz.projectsem2.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class GraphCanvas {

    // Attributes — rendering constants (loaded from colors.properties)
    private static final double NODE_RADIUS = ColorLoader.getNodeRadius();
    private static final Color NODE_COLOR          = ColorLoader.getNodeColor();
    private static final Color NODE_EDGE_SRC_COLOR = ColorLoader.getNodeEdgeSourceColor();
    private static final Color NODE_SELECTED_COLOR = ColorLoader.getNodeSelectedColor();
    private static final Color EDGE_COLOR          = ColorLoader.getEdgeColor();
    private static final Color EDGE_SELECTED_COLOR = ColorLoader.getEdgeSelectedColor();
    private static final Color LABEL_COLOR         = ColorLoader.getLabelColor();
    private static final Color CANVAS_BG           = ColorLoader.getCanvasBackground();
    private static final Color GRID_COLOR          = ColorLoader.getGridColor();

    // Attributes — FXML bindings (canvas + panes)
    @FXML private Canvas canvas1;
    @FXML private Canvas canvas2;
    @FXML private Pane canvasPane1;
    @FXML private Pane canvasPane2;

    // Attributes — FXML bindings (toolbar)
    @FXML private ToggleButton nodeModeBtn;
    @FXML private ToggleButton edgeModeBtn;
    @FXML private ToggleButton deleteModeBtn;
    @FXML private ToggleButton matrixToggleBtn;
    @FXML private Button backButton;

    // Attributes — FXML bindings (matrix input panel)
    @FXML private HBox matrixInputPanel;
    @FXML private TextArea matrix1Field;
    @FXML private TextArea matrix2Field;

    // Attributes — FXML bindings (bottom bar)
    @FXML private Label resultLabel;
    @FXML private Label verdictLabel;
    @FXML private Label vertexLabel;
    @FXML private Label edgeLabel;
    @FXML private Label degreeLabel;
    @FXML private Label connectedLabel;
    @FXML private Label bipartiteLabel;
    @FXML private Label eulerLabel;

    // Attributes — FXML bindings (matrix result panel)
    // @FXML private VBox matrixResultPanel;
    // @FXML private TextArea matrixA1Area;
    // @FXML private TextArea matrixPArea;
    // @FXML private TextArea matrixA2Area;

    // Attributes — graph state
    private Graph graph1 = new Graph("Graph 1");
    private Graph graph2 = new Graph("Graph 2");
    private int nodeCounter1 = 1;
    private int nodeCounter2 = 1;

    // Attributes — edge-building state
    private Node edgeSourceNode = null;
    private int edgeSourceCanvas = 0;

    // Attributes — last-added-edge
    private Node lastEdgeSource = null;
    private Node lastEdgeTarget = null;
    private int lastEdgeCanvas = 0;

    // Attributes — selection state
    private Node selectedNode = null;
    private int selectedNodeCanvas = 0;
    private Edge selectedEdge = null;
    private int selectedEdgeCanvas = 0;

    // Attributes — undo / redo stacks
    private final Deque<CanvasState> undoStack = new ArrayDeque<>();
    private final Deque<CanvasState> redoStack = new ArrayDeque<>();

    // Attributes — anti-bounce key tracking
    private final Set<KeyCode> heldKeys = new HashSet<>();

    // Static preload — set by Dashboard before navigating to this screen
    public static GraphPair preload = null;

    private enum Mode { NODE, EDGE, DELETE }
    private Mode currentMode = Mode.NODE;

    // snapshot of both canvases for undo/redo
    private static class CanvasState {
        private final Graph g1;
        private final Graph g2;

        public CanvasState(Graph g1, Graph g2) {
            this.g1 = g1;
            this.g2 = g2;
        }

        public Graph getG1() {
            return g1;
        }

        public Graph getG2() {
            return g2;
        }
    }

    // Methods
    @FXML
    public void initialize() {
        canvas1.setOnMouseClicked(e -> handleCanvasClick(e, 1));
        canvas2.setOnMouseClicked(e -> handleCanvasClick(e, 2));

        nodeModeBtn.setSelected(true);
        nodeModeBtn.setOnAction(e -> setMode(Mode.NODE));
        edgeModeBtn.setOnAction(e -> setMode(Mode.EDGE));
        deleteModeBtn.setOnAction(e -> setMode(Mode.DELETE));

        matrixInputPanel.setVisible(false);
        matrixInputPanel.setManaged(false);
        // matrixResultPanel.setVisible(false);
        // matrixResultPanel.setManaged(false);

        canvas1.widthProperty().bind(canvasPane1.widthProperty());
        canvas1.heightProperty().bind(canvasPane1.heightProperty());
        canvas2.widthProperty().bind(canvasPane2.widthProperty());
        canvas2.heightProperty().bind(canvasPane2.heightProperty());

        canvasPane1.widthProperty().addListener((obs, o, n) -> redraw(1));
        canvasPane1.heightProperty().addListener((obs, o, n) -> redraw(1));
        canvasPane2.widthProperty().addListener((obs, o, n) -> redraw(2));
        canvasPane2.heightProperty().addListener((obs, o, n) -> redraw(2));

        // load pair from dashboard if one was set
        if (preload != null) {
            graph1 = preload.getGraphA();
            graph2 = preload.getGraphB();
            nodeCounter1 = graph1.getNodes().size() + 1;
            nodeCounter2 = graph2.getNodes().size() + 1;
            preload = null;
        }

        // keyboard events are on the scene — attach once the node is in a scene
        canvas1.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED,  this::handleKeyPressed);
                newScene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
            }
        });
    }

    // Mode management

    private void setMode(Mode mode) {
        currentMode = mode;
        nodeModeBtn.setSelected(mode == Mode.NODE);
        edgeModeBtn.setSelected(mode == Mode.EDGE);
        deleteModeBtn.setSelected(mode == Mode.DELETE);
        clearEdgeSelection();
        if (mode != Mode.DELETE) clearSelection();
        redraw(1); redraw(2);
    }

    // Canvas click handling

    private void handleCanvasClick(MouseEvent e, int canvasNum) {
        double x = e.getX(), y = e.getY();
        Graph graph = canvasNum == 1 ? graph1 : graph2;

        switch (currentMode) {
            case NODE -> {
                pushUndo();
                String label = canvasNum == 1
                        ? String.valueOf(nodeCounter1++)
                        : String.valueOf(nodeCounter2++);
                graph.getNodes().add(new Node(label, x, y));
                redraw(canvasNum);
            }
            case EDGE -> handleEdgeMode(x, y, canvasNum, graph);
            case DELETE -> handleDeleteMode(x, y, canvasNum, graph);
        }
    }

    private void handleEdgeMode(double x, double y, int canvasNum, Graph graph) {
        Node clicked = findNodeAt(x, y, graph);
        if (clicked == null) {
            Edge e = findEdgeAt(x, y, graph);
            if (e != null) {
                selectedEdge = e;
                selectedEdgeCanvas = canvasNum;
                selectedNode = null;
            }
            redraw(canvasNum);
            return;
        }

        if (edgeSourceNode == null) {
            edgeSourceNode = clicked;
            edgeSourceCanvas = canvasNum;
        } else if (edgeSourceCanvas == canvasNum && edgeSourceNode != clicked) {
            final Node src = edgeSourceNode;
            boolean duplicate = graph.getEdges().stream().anyMatch(e ->
                    (e.getSourceNode() == src && e.getTargetNode() == clicked) ||
                    (e.getSourceNode() == clicked && e.getTargetNode() == src));
            if (!duplicate) {
                pushUndo();
                graph.getEdges().add(new Edge(src, clicked, null));
                lastEdgeSource = src;
                lastEdgeTarget = clicked;
                lastEdgeCanvas = canvasNum;
            }
            clearEdgeSelection();
        } else {
            // re-select a different source
            edgeSourceNode = clicked;
            edgeSourceCanvas = canvasNum;
        }
        redraw(canvasNum);
    }

    private void handleDeleteMode(double x, double y, int canvasNum, Graph graph) {
        Node node = findNodeAt(x, y, graph);
        if (node != null) {
            pushUndo();
            graph.getEdges().removeIf(e -> e.getSourceNode() == node || e.getTargetNode() == node);
            graph.getNodes().removeIf(n -> n == node);
            renumberNodes(graph, canvasNum);
            clearSelection();
            redraw(canvasNum);
            return;
        }

        Edge edge = findEdgeAt(x, y, graph);
        if (edge != null) {
            pushUndo();
            graph.getEdges().removeIf(e -> e == edge);
            clearSelection();
            redraw(canvasNum);
        }
    }

    // Keyboard handling

    private void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        // anti-bounce: ignore auto-repeat
        if (!heldKeys.add(code)) return;

        if (e.isControlDown()) {
            if (code == KeyCode.Z) { undo(); e.consume(); }
            else if (code == KeyCode.Y) { redo(); e.consume(); }
            else if (code == KeyCode.S) { handleSavePair(); e.consume(); }
            return;
        }

        switch (code) {
            case N:
                setMode(Mode.NODE);
                e.consume();
                break;
            case E:
                setMode(Mode.EDGE);
                e.consume();
                break;
            case D:
                setMode(Mode.DELETE);
                e.consume();
                break;
            case DELETE:
            case BACK_SPACE:
                if (e.getTarget() instanceof TextInputControl) break;
                deleteSelected();
                e.consume();
                break;
            case T:
                // fast-chain: jump to the target of the last added edge
                if (currentMode == Mode.EDGE && lastEdgeTarget != null) {
                    edgeSourceNode = lastEdgeTarget;
                    edgeSourceCanvas = lastEdgeCanvas;
                    redraw(lastEdgeCanvas);
                }
                e.consume();
                break;
            case S:
                // fast-chain: jump to the source of the last added edge
                if (currentMode == Mode.EDGE && lastEdgeSource != null) {
                    edgeSourceNode = lastEdgeSource;
                    edgeSourceCanvas = lastEdgeCanvas;
                    redraw(lastEdgeCanvas);
                }
                e.consume();
                break;
            default:
                break;
        }
    }

    private void handleKeyReleased(KeyEvent e) {
        heldKeys.remove(e.getCode());
    }

    // ── Deletion ──────────────────────────────────────────────────────────────

    private void deleteSelected() {
        if (selectedNode != null) {
            pushUndo();
            Graph g = selectedNodeCanvas == 1 ? graph1 : graph2;
            final Node target = selectedNode;
            g.getEdges().removeIf(e -> e.getSourceNode() == target || e.getTargetNode() == target);
            g.getNodes().removeIf(n -> n == target);
            renumberNodes(g, selectedNodeCanvas);
            clearSelection();
            redraw(selectedNodeCanvas);
        } else if (selectedEdge != null) {
            pushUndo();
            Graph g = selectedEdgeCanvas == 1 ? graph1 : graph2;
            final Edge target = selectedEdge;
            g.getEdges().removeIf(e -> e == target);
            clearSelection();
            redraw(selectedEdgeCanvas);
        }
    }

    private void renumberNodes(Graph g, int canvasNum) {
        int counter = 1;
        for (Node n : g.getNodes()) n.setLabel(String.valueOf(counter++));
        // keep the counter in sync so the next added node gets the right label
        if (canvasNum == 1) nodeCounter1 = counter;
        else nodeCounter2 = counter;
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    private void pushUndo() {
        undoStack.push(new CanvasState(deepCopy(graph1), deepCopy(graph2)));
        redoStack.clear();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(new CanvasState(deepCopy(graph1), deepCopy(graph2)));
        restoreState(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.push(new CanvasState(deepCopy(graph1), deepCopy(graph2)));
        restoreState(redoStack.pop());
    }

    private void restoreState(CanvasState state) {
        graph1 = state.getG1();
        graph2 = state.getG2();
        nodeCounter1 = graph1.getNodes().size() + 1;
        nodeCounter2 = graph2.getNodes().size() + 1;
        clearEdgeSelection();
        clearSelection();
        redraw(1);
        redraw(2);
    }

    // Deep copy of a Graph so undo snapshots are independent of live data
    private Graph deepCopy(Graph original) {
        Graph copy = new Graph(original.getName());
        copy.setId(original.getId());

        List<Node> originalNodes = original.getNodes();
        List<Node> copiedNodes = new java.util.ArrayList<Node>();
        for (Node node : originalNodes) {
            Node copyNode = new Node(node.getLabel(), node.getX(), node.getY());
            copyNode.setId(node.getId());
            copiedNodes.add(copyNode);
            copy.getNodes().add(copyNode);
        }

        for (Edge edge : original.getEdges()) {
            int sourceIndex = indexOfNode(originalNodes, edge.getSourceNode());
            int targetIndex = indexOfNode(originalNodes, edge.getTargetNode());
            if (sourceIndex < 0 || targetIndex < 0) {
                continue;
            }
            Node sourceCopy = copiedNodes.get(sourceIndex);
            Node targetCopy = copiedNodes.get(targetIndex);
            Edge edgeCopy = new Edge(sourceCopy, targetCopy, edge.getWeight());
            edgeCopy.setId(edge.getId());
            copy.getEdges().add(edgeCopy);
        }
        return copy;
    }

    // Use identity comparison because Node.equals relies on database id values.
    private int indexOfNode(List<Node> nodes, Node target) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private void redraw(int canvasNum) {
        if (canvasNum == 1) {
            Node edgeSrc  = edgeSourceCanvas == 1 ? edgeSourceNode : null;
            Node selNode  = selectedNodeCanvas == 1 ? selectedNode : null;
            Edge selEdge  = selectedEdgeCanvas == 1 ? selectedEdge : null;
            drawGraph(canvas1, graph1, edgeSrc, selNode, selEdge);
        } else {
            Node edgeSrc  = edgeSourceCanvas == 2 ? edgeSourceNode : null;
            Node selNode  = selectedNodeCanvas == 2 ? selectedNode : null;
            Edge selEdge  = selectedEdgeCanvas == 2 ? selectedEdge : null;
            drawGraph(canvas2, graph2, edgeSrc, selNode, selEdge);
        }
    }

    private void drawGraph(Canvas canvas, Graph graph,
                            Node edgeSrcHighlight, Node selNodeHighlight, Edge selEdgeHighlight) {
        double w = canvas.getWidth(), h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(CANVAS_BG);
        gc.fillRect(0, 0, w, h);

        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);
        for (double x = 0; x <= w; x += 40) gc.strokeLine(x, 0, x, h);
        for (double y = 0; y <= h; y += 40) gc.strokeLine(0, y, w, y);

        for (Edge e : graph.getEdges()) {
            boolean isSelected = e == selEdgeHighlight;
            gc.setStroke(isSelected ? EDGE_SELECTED_COLOR : EDGE_COLOR);
            gc.setLineWidth(isSelected ? 3.5 : 2);
            gc.strokeLine(e.getSourceNode().getX(), e.getSourceNode().getY(),
                          e.getTargetNode().getX(), e.getTargetNode().getY());
        }

        for (Node n : graph.getNodes()) {
            boolean isEdgeSrc = n == edgeSrcHighlight;
            boolean isSelected = n == selNodeHighlight;
            Color fill = isEdgeSrc ? NODE_EDGE_SRC_COLOR
                       : isSelected ? NODE_SELECTED_COLOR
                       : NODE_COLOR;
            gc.setFill(fill);
            gc.fillOval(n.getX() - NODE_RADIUS, n.getY() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            gc.setStroke(isEdgeSrc ? Color.web("#bbf7d0")
                       : isSelected ? Color.web("#fde68a")
                       : Color.web("#052e16"));
            gc.setLineWidth(2);
            gc.strokeOval(n.getX() - NODE_RADIUS, n.getY() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            gc.setFill(LABEL_COLOR);
            gc.setFont(Font.font("System", FontWeight.BOLD, 13));
            double textX = n.getX() - (n.getLabel().length() * 4.0);
            gc.fillText(n.getLabel(), textX, n.getY() + 5);
        }
    }

    // ── Hit-testing helpers ───────────────────────────────────────────────────

    private Node findNodeAt(double x, double y, Graph graph) {
        for (Node n : graph.getNodes()) {
            double dx = n.getX() - x, dy = n.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS) return n;
        }
        return null;
    }

    private Edge findEdgeAt(double x, double y, Graph graph) {
        Edge closest = null;
        double minDist = 10;
        for (Edge e : graph.getEdges()) {
            double d = distanceToEdge(x, y, e);
            if (d < minDist) { minDist = d; closest = e; }
        }
        return closest;
    }

    private double distanceToEdge(double px, double py, Edge e) {
        double x1 = e.getSourceNode().getX(), y1 = e.getSourceNode().getY();
        double x2 = e.getTargetNode().getX(), y2 = e.getTargetNode().getY();
        double dx = x2 - x1, dy = y2 - y1;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / len2));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }

    // ── Selection helpers ─────────────────────────────────────────────────────

    private void clearEdgeSelection() {
        edgeSourceNode = null;
        edgeSourceCanvas = 0;
    }

    private void clearSelection() {
        selectedNode = null; selectedNodeCanvas = 0;
        selectedEdge = null; selectedEdgeCanvas = 0;
    }

    // ── Adjacency matrix input ─────────────────────────────────────────────────

    @FXML
    public void handleMatrixToggle() {
        boolean show = matrixToggleBtn.isSelected();
        matrixInputPanel.setVisible(show);
        matrixInputPanel.setManaged(show);
    }

    @FXML
    public void handleParseMatrix1() { parseMatrixIntoGraph(matrix1Field.getText(), 1); }

    @FXML
    public void handleParseMatrix2() { parseMatrixIntoGraph(matrix2Field.getText(), 2); }

    private void parseMatrixIntoGraph(String text, int canvasNum) {
        try {
            int[][] matrix = parseMatrix(text);
            int n = matrix.length;
            Canvas canvas = canvasNum == 1 ? canvas1 : canvas2;
            double w = canvas.getWidth() > 10 ? canvas.getWidth() : 400;
            double h = canvas.getHeight() > 10 ? canvas.getHeight() : 300;

            pushUndo();

            Graph graph = new Graph(canvasNum == 1 ? "Graph 1" : "Graph 2");
            if (canvasNum == 1) graph1 = graph; else graph2 = graph;

            double cx = w / 2, cy = h / 2;
            double r = Math.min(cx, cy) * 0.72;
            List<Node> nodes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                double angle = 2 * Math.PI * i / n - Math.PI / 2;
                nodes.add(new Node(String.valueOf(i + 1), cx + r * Math.cos(angle), cy + r * Math.sin(angle)));
            }
            graph.setNodes(nodes);

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (matrix[i][j] != 0) graph.getEdges().add(new Edge(nodes.get(i), nodes.get(j), null));
                }
            }

            if (canvasNum == 1) nodeCounter1 = n + 1; else nodeCounter2 = n + 1;
            clearEdgeSelection(); clearSelection();
            redraw(canvasNum);
            resultLabel.setText("Graph " + canvasNum + " loaded from matrix (" + n + " nodes).");
            resultLabel.setStyle("-fx-text-fill: #86efac; -fx-font-size: 13px;");
        } catch (Exception ex) {
            resultLabel.setText("Matrix parse error: " + ex.getMessage());
            resultLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 13px;");
        }
    }

    private int[][] parseMatrix(String text) {
        String[] rows = text.trim().split("\\r?\\n");
        int n = rows.length;
        if (n == 0) throw new IllegalArgumentException("empty input");
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            String[] cols = rows[i].trim().split("[,\\s]+");
            if (cols.length != n)
                throw new IllegalArgumentException("row " + (i + 1) + " has " + cols.length + " cols, expected " + n);
            for (int j = 0; j < n; j++) m[i][j] = Integer.parseInt(cols[j].trim());
        }
        return m;
    }

    // ── Isomorphism check + matrix display ────────────────────────────────────

    @FXML
    public void handleCheckIsomorphism() {
        IsomorphismResult result = IsomorphismService.check(graph1, graph2);
        resultLabel.setText("");

        setInvariantLabel(vertexLabel,    "VERTICES",      result.isVertexMatch());
        setInvariantLabel(edgeLabel,      "EDGES",         result.isEdgeMatch());
        setInvariantLabel(degreeLabel,    "DEGREE SEQ",    result.isDegreeMatch());
        setInvariantLabel(connectedLabel, "CONNECTED",     result.isConnectedMatch());
        setInvariantLabel(bipartiteLabel, "BIPARTITE",     result.isBipartiteMatch());
        setInvariantLabel(eulerLabel,     "EULER CIRCUIT", result.isEulerMatch());

        verdictLabel.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        if (result.isIsomorphic()) {
            verdictLabel.setText("✓  The two graphs ARE isomorphic.");
            verdictLabel.getStyleClass().add("pass-text");
            // showMatrices(result);
        } else {
            verdictLabel.setText("✗  The two graphs are NOT isomorphic.");
            verdictLabel.getStyleClass().add("fail-text");
            // matrixResultPanel.setVisible(false);
            // matrixResultPanel.setManaged(false);
        }
    }

    /*
    private void showMatrices(IsomorphismResult result) {
        List<Node> nodes1 = graph1.getNodes();
        List<Node> nodes2 = graph2.getNodes();
        int n = nodes1.size();
        if (n == 0) return;

        int[][] a1 = buildAdjMatrix(graph1);
        int[][] a2 = buildAdjMatrix(graph2);

        // Build permutation matrix P where P[i][j]=1 means node i of g1 maps to node j of g2
        IdentityHashMap<Node, Node> mapping = result.getMapping();
        int[][] p = new int[n][n];
        if (mapping != null) {
            for (int i = 0; i < n; i++) {
                Node mapped = mapping.get(nodes1.get(i));
                for (int j = 0; j < n; j++) {
                    if (nodes2.get(j) == mapped) { p[i][j] = 1; break; }
                }
            }
        }

        matrixA1Area.setText(matrixToString(a1));
        matrixPArea.setText(matrixToString(p));
        matrixA2Area.setText(matrixToString(a2));
        matrixResultPanel.setVisible(true);
        matrixResultPanel.setManaged(true);
    }

    private int[][] buildAdjMatrix(Graph g) {
        List<Node> nodes = g.getNodes();
        int n = nodes.size();
        int[][] m = new int[n][n];
        for (Edge e : g.getEdges()) {
            int i = identityIndexOf(nodes, e.getSourceNode());
            int j = identityIndexOf(nodes, e.getTargetNode());
            if (i >= 0 && j >= 0) { m[i][j] = 1; m[j][i] = 1; }
        }
        return m;
    }

    // indexOf using == (identity) because Node.equals is id-based and canvas nodes have null ids
    private int identityIndexOf(List<Node> list, Node target) {
        for (int i = 0; i < list.size(); i++) if (list.get(i) == target) return i;
        return -1;
    }

    private String matrixToString(int[][] m) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : m) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) sb.append("  ");
                sb.append(row[i]);
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }
    */

    private void setInvariantLabel(Label label, String name, boolean passed) {
        label.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        if (passed) { label.setText(name + "  ✓"); label.getStyleClass().add("pass-text"); }
        else        { label.setText(name + "  ✗"); label.getStyleClass().add("fail-text"); }
    }

    private void resetInvariantLabels() {
        java.util.List<Label> invariantLabels = java.util.Arrays.asList(
                vertexLabel, edgeLabel, degreeLabel, connectedLabel, bipartiteLabel, eulerLabel);
        for (Label l : invariantLabels) {
            l.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        }
        vertexLabel.setText("VERTICES  —");
        edgeLabel.setText("EDGES  —");
        degreeLabel.setText("DEGREE SEQ  —");
        connectedLabel.setText("CONNECTED  —");
        bipartiteLabel.setText("BIPARTITE  —");
        eulerLabel.setText("EULER CIRCUIT  —");
        verdictLabel.setText("");
        verdictLabel.getStyleClass().removeIf(c -> c.equals("pass-text") || c.equals("fail-text"));
        // matrixResultPanel.setVisible(false);
        // matrixResultPanel.setManaged(false);
    }

    // ── Canvas clear actions ─────────────────────────────────────────────────

    @FXML
    public void handleClearCanvas1() {
        pushUndo();
        graph1 = new Graph("Graph 1");
        nodeCounter1 = 1;
        clearEdgeSelection(); clearSelection();
        drawGraph(canvas1, graph1, null, null, null);
        resetInvariantLabels();
    }

    @FXML
    public void handleClearCanvas2() {
        pushUndo();
        graph2 = new Graph("Graph 2");
        nodeCounter2 = 1;
        clearEdgeSelection(); clearSelection();
        drawGraph(canvas2, graph2, null, null, null);
        resetInvariantLabels();
    }

    // ── Save Pair (Ctrl+S / button) ───────────────────────────────────────────

    @FXML
    public void handleSavePair() {
        if (!SessionManager.isLoggedIn()) {
            resultLabel.setText("Log in to save pairs.");
            resultLabel.setStyle("-fx-text-fill: #f87171; -fx-font-size: 13px;");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("My Pair");
        dialog.setTitle("Save Graph Pair");
        dialog.setHeaderText("Enter a name for this Graph A / Graph B pair:");
        dialog.setContentText("Pair name:");
        java.util.Optional<String> optionalName = dialog.showAndWait();
        if (optionalName.isPresent()) {
            String name = optionalName.get();
            if (name.trim().length() == 0) {
                return;
            }
            GraphPair pair = new GraphPair(
                    name,
                    deepCopy(graph1),
                    deepCopy(graph2),
                    SessionManager.getCurrentUser().getId());
            GraphPairService.saveGraphPair(pair);
            resultLabel.setText("Pair \"" + name + "\" saved.");
            resultLabel.setStyle("-fx-text-fill: #86efac; -fx-font-size: 13px;");
        }
    }

    @FXML
    public void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Helper.changeScene(stage, "/com/talha/quiz/projectsem2/fxml/dashboard.fxml");
    }
}
