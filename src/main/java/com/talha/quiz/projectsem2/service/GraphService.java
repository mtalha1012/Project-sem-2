package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.model.Edge;
import com.talha.quiz.projectsem2.model.Graph;
import com.talha.quiz.projectsem2.model.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphService {
    private static final DatabaseService database = DatabaseService.getInstance();

    public static void saveGraph(Graph graph, Integer userId) {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphQuery("graph.save"), Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, graph.getName());
            if (userId != null) ps.setInt(2, userId);
            else ps.setNull(2, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) graph.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save graph", e);
        }

        for (Node node : graph.getNodes()) {
            try (PreparedStatement ps = database.getConnection().prepareStatement(
                    database.getNodeQuery("node.save"), Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, graph.getId());
                ps.setString(2, node.getLabel());
                ps.setDouble(3, node.getX());
                ps.setDouble(4, node.getY());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) node.setId(keys.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save node", e);
            }
        }

        for (Edge edge : graph.getEdges()) {
            try (PreparedStatement ps = database.getConnection().prepareStatement(
                    database.getEdgeQuery("edge.save"), Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, graph.getId());
                ps.setInt(2, edge.getSourceNode().getId());
                ps.setInt(3, edge.getTargetNode().getId());
                if (edge.getWeight() != null) ps.setDouble(4, edge.getWeight());
                else ps.setNull(4, Types.DOUBLE);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) edge.setId(keys.getInt(1));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to save edge", e);
            }
        }
    }

    public static Graph loadGraph(Integer graphId) {
        Graph graph = new Graph();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphQuery("graph.load"))) {
            ps.setInt(1, graphId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    graph.setId(rs.getInt("id"));
                    graph.setName(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load graph", e);
        }

        List<Node> nodes = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getNodeQuery("node.loadByGraph"))) {
            ps.setInt(1, graphId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Node n = new Node(rs.getString("label"), rs.getDouble("x"), rs.getDouble("y"));
                    n.setId(rs.getInt("id"));
                    nodes.add(n);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load nodes", e);
        }
        graph.setNodes(nodes);

        List<Edge> edges = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getEdgeQuery("edge.loadByGraph"))) {
            ps.setInt(1, graphId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int srcId = rs.getInt("source_node_id");
                    int tgtId = rs.getInt("target_node_id");
                    Node src = nodes.stream().filter(n -> Objects.equals(n.getId(), srcId)).findFirst().orElse(null);
                    Node tgt = nodes.stream().filter(n -> Objects.equals(n.getId(), tgtId)).findFirst().orElse(null);
                    Double weight = rs.getObject("weight") != null ? rs.getDouble("weight") : null;
                    Edge e = new Edge(src, tgt, weight);
                    e.setId(rs.getInt("id"));
                    edges.add(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load edges", e);
        }
        graph.setEdges(edges);
        return graph;
    }

    public static List<Graph> getUserGraphs(Integer userId) {
        List<Graph> graphs = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphQuery("graph.loadByUser"))) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Graph g = new Graph(rs.getString("name"));
                    g.setId(rs.getInt("id"));
                    graphs.add(g);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user graphs", e);
        }
        return graphs;
    }

    public static void deleteGraph(Integer graphId) {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphQuery("graph.delete"))) {
            ps.setInt(1, graphId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete graph", e);
        }
    }
}
