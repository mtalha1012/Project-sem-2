package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.model.Graph;
import com.talha.quiz.projectsem2.model.GraphPair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GraphPairService {
    private static final DatabaseService database = DatabaseService.getInstance();

    // Methods
    public static void saveGraphPair(GraphPair pair) {
        // persist both graphs first so they get IDs
        GraphService.saveGraph(pair.getGraphA(), pair.getUserId());
        GraphService.saveGraph(pair.getGraphB(), pair.getUserId());

        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphPairQuery("pair.save"), Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pair.getName());
            if (pair.getUserId() != null) ps.setInt(2, pair.getUserId());
            else ps.setNull(2, Types.INTEGER);
            ps.setInt(3, pair.getGraphA().getId());
            ps.setInt(4, pair.getGraphB().getId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) pair.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save graph pair", e);
        }
    }

    public static List<GraphPair> getUserGraphPairs(Integer userId) {
        List<GraphPair> pairs = new ArrayList<>();
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphPairQuery("pair.loadByUser"))) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GraphPair pair = new GraphPair();
                    pair.setId(rs.getInt("id"));
                    pair.setName(rs.getString("name"));
                    pair.setUserId(rs.getInt("user_id"));
                    Graph gA = GraphService.loadGraph(rs.getInt("graph_a_id"));
                    Graph gB = GraphService.loadGraph(rs.getInt("graph_b_id"));
                    pair.setGraphA(gA);
                    pair.setGraphB(gB);
                    pairs.add(pair);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load graph pairs", e);
        }
        return pairs;
    }

    public static void deleteGraphPair(Integer pairId) {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                database.getGraphPairQuery("pair.delete"))) {
            ps.setInt(1, pairId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete graph pair", e);
        }
    }
}
