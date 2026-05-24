package com.talha.quiz.projectsem2.model;

import java.util.Objects;

public class Node {
    // Attributes
    private Integer id;
    private String label;
    private double x;
    private double y;

    // Constructor
    public Node() {}
    public Node(String label, double x, double y) {
        this.label = label;
        this.x = x;
        this.y = y;
    }

    public Integer getId() {
        return id;
    }

    // Only to be used by service classes when loading from database
    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Node) && Objects.equals(((Node) o).getId(), getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}