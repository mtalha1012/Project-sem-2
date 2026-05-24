package com.talha.quiz.projectsem2.model;

import java.util.Objects;

public class Edge {
    // Attributes
    private Integer id;
    private Node sourceNode;
    private Node targetNode;
    private Double weight;

    // Constructor
    public Edge(Node sourceNode, Node targetNode, Double weight) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.weight = weight;
    }

    public Edge() {
    }

    public Integer getId() {
        return id;
    }

    // Only to be used by service classes when loading from database
    public void setId(Integer id) {
        this.id = id;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Edge) && Objects.equals(((Edge) o).getId(), getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
