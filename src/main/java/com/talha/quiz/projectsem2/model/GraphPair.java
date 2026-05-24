package com.talha.quiz.projectsem2.model;

import java.util.Objects;

public class GraphPair {
    // Attributes
    private Integer id;
    private String name;
    private Integer userId;
    private Graph graphA;
    private Graph graphB;

    // Constructor
    public GraphPair(String name, Graph graphA, Graph graphB, Integer userId) {
        this.name = name;
        this.graphA = graphA;
        this.graphB = graphB;
        this.userId = userId;
    }

    public GraphPair() {}

    // Methods
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Graph getGraphA() { return graphA; }
    public void setGraphA(Graph graphA) { this.graphA = graphA; }
    public Graph getGraphB() { return graphB; }
    public void setGraphB(Graph graphB) { this.graphB = graphB; }

    @Override
    public boolean equals(Object o) {
        return (o instanceof GraphPair) && Objects.equals(((GraphPair) o).getId(), getId());
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return name != null ? name : "Unnamed Pair"; }
}
