package com.novalearn.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.chart.XYChart;

public class ReclamationStatsService {
    private static ReclamationStatsService instance;
    private Connection connection;

    private ReclamationStatsService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/novalearn", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ReclamationStatsService getInstance() {
        if (instance == null) {
            instance = new ReclamationStatsService();
        }
        return instance;
    }

    public Map<String, Integer> getReclamationsByStatus() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT statut, COUNT(*) as count FROM reclamations GROUP BY statut";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("statut"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public Map<String, Integer> getReclamationsByPriority() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT priorite, COUNT(*) as count FROM reclamations GROUP BY priorite";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                stats.put(rs.getString("priorite"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    public double getAverageResolutionTime() {
        String query = "SELECT AVG(temps_resolution) as avg_time FROM reclamations WHERE statut = 'RESOLUE'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public XYChart.Series<String, Number> getReclamationsTrend() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réclamations par jour");
        
        String query = "SELECT DATE(date_creation) as date, COUNT(*) as count " +
                      "FROM reclamations " +
                      "GROUP BY DATE(date_creation) " +
                      "ORDER BY date_creation DESC LIMIT 30";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(
                    rs.getString("date"),
                    rs.getInt("count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return series;
    }

    public Map<String, Integer> getMostCommonTags() {
        Map<String, Integer> tagStats = new HashMap<>();
        String query = "SELECT tags, COUNT(*) as count " +
                      "FROM reclamations " +
                      "WHERE tags IS NOT NULL " +
                      "GROUP BY tags " +
                      "ORDER BY count DESC " +
                      "LIMIT 10";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String[] tags = rs.getString("tags").split(",");
                for (String tag : tags) {
                    tag = tag.trim();
                    tagStats.put(tag, tagStats.getOrDefault(tag, 0) + rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tagStats;
    }

    public void updateResolutionTime(int reclamationId) {
        String query = "UPDATE reclamations SET temps_resolution = ? " +
                      "WHERE id = ? AND statut = 'RESOLUE' AND date_resolution IS NOT NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // Calculer le temps de résolution
            String timeQuery = "SELECT date_creation, date_resolution FROM reclamations WHERE id = ?";
            PreparedStatement timeStmt = connection.prepareStatement(timeQuery);
            timeStmt.setInt(1, reclamationId);
            ResultSet rs = timeStmt.executeQuery();
            
            if (rs.next()) {
                LocalDateTime creation = rs.getTimestamp("date_creation").toLocalDateTime();
                LocalDateTime resolution = rs.getTimestamp("date_resolution").toLocalDateTime();
                long minutes = ChronoUnit.MINUTES.between(creation, resolution);
                
                stmt.setLong(1, minutes);
                stmt.setInt(2, reclamationId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 