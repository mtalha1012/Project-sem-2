package com.talha.quiz.projectsem2.util;

import javafx.scene.paint.Color;
import java.util.Properties;

public class ColorLoader {
    private static final Properties colors = LoadProperties.loadProperties(
            "colors.properties", ColorLoader.class.getClassLoader());

    public static Color getNodeColor() {
        return Color.web(colors.getProperty("canvas.node.color"));
    }

    public static Color getNodeEdgeSourceColor() {
        return Color.web(colors.getProperty("canvas.node.edge-source.color"));
    }

    public static Color getNodeSelectedColor() {
        return Color.web(colors.getProperty("canvas.node.selected.color"));
    }

    public static Color getEdgeColor() {
        return Color.web(colors.getProperty("canvas.edge.color"));
    }

    public static Color getEdgeSelectedColor() {
        return Color.web(colors.getProperty("canvas.edge.selected.color"));
    }

    public static Color getLabelColor() {
        return Color.web(colors.getProperty("canvas.label.color"));
    }

    public static Color getCanvasBackground() {
        return Color.web(colors.getProperty("canvas.background"));
    }

    public static Color getGridColor() {
        return Color.web(colors.getProperty("canvas.grid.color"));
    }

    public static double getNodeRadius() {
        return Double.parseDouble(colors.getProperty("canvas.node.radius"));
    }
}
