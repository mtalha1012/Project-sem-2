package com.talha.quiz.projectsem2.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Graph {
    // Attributes
    private Integer id;
    private String name;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private HashMap<Node, List<Node>> graph;

    // Constructor
    public Graph(String name, List<Node> nodes, List<Edge> edges) {
        this.name = name;
        this.nodes = nodes;
        this.edges = edges;

        graph = new HashMap<>();
        for (Node n : nodes) {
            graph.put(n, new ArrayList<>());
        }
        for (Edge e : edges) {
            graph.get(e.getSourceNode()).add(e.getTargetNode());
            graph.get(e.getTargetNode()).add(e.getSourceNode());
        }
    }

    public Graph(String name) {
        this.name = name;
    }

    public Graph() {
    }

    public Integer getId() {
        return id;
    }

    // Only to be used by service classes when loading from database
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Graph) && Objects.equals(((Graph) o).getId(), getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
