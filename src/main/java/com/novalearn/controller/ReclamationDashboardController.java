package com.novalearn.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import com.novalearn.service.ReclamationStatsService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ReclamationDashboardController {
    @FXML private Label totalReclamationsLabel;
    @FXML private Label avgResolutionTimeLabel;
    @FXML private Label resolutionRateLabel;
    @FXML private PieChart statusChart;
    @FXML private PieChart priorityChart;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private FlowPane tagsFlowPane;

    private ReclamationStatsService statsService;
    private Stage stage;

    @FXML
    public void initialize() {
        statsService = ReclamationStatsService.getInstance();
        loadStatistics();
    }

    private void loadStatistics() {
        // Charger les statistiques générales
        Map<String, Integer> statusStats = statsService.getReclamationsByStatus();
        int total = statusStats.values().stream().mapToInt(Integer::intValue).sum();
        int resolved = statusStats.getOrDefault("RESOLUE", 0);
        
        totalReclamationsLabel.setText(String.valueOf(total));
        
        double avgTime = statsService.getAverageResolutionTime();
        avgResolutionTimeLabel.setText(String.format("%.1f minutes", avgTime));
        
        double resolutionRate = total > 0 ? (resolved * 100.0 / total) : 0;
        resolutionRateLabel.setText(String.format("%.1f%%", resolutionRate));

        // Charger le graphique des statuts
        statusChart.setData(FXCollections.observableArrayList(
            statusStats.entrySet().stream()
                .map(entry -> new PieChart.Data(
                    formatStatus(entry.getKey()), 
                    entry.getValue()
                ))
                .collect(Collectors.toList())
        ));

        // Charger le graphique des priorités
        Map<String, Integer> priorityStats = statsService.getReclamationsByPriority();
        priorityChart.setData(FXCollections.observableArrayList(
            priorityStats.entrySet().stream()
                .map(entry -> new PieChart.Data(
                    formatPriority(entry.getKey()), 
                    entry.getValue()
                ))
                .collect(Collectors.toList())
        ));

        // Charger le graphique des tendances
        trendChart.getData().clear();
        trendChart.getData().add(statsService.getReclamationsTrend());

        // Charger les tags les plus fréquents
        loadTags();
    }

    private void loadTags() {
        tagsFlowPane.getChildren().clear();
        Map<String, Integer> tagStats = statsService.getMostCommonTags();
        
        tagStats.forEach((tag, count) -> {
            VBox tagBox = new VBox(5);
            tagBox.getStyleClass().add("tag-box");
            
            Label tagLabel = new Label(tag);
            tagLabel.getStyleClass().add("tag-label");
            
            Label countLabel = new Label(String.valueOf(count));
            countLabel.getStyleClass().add("tag-count");
            
            tagBox.getChildren().addAll(tagLabel, countLabel);
            tagsFlowPane.getChildren().add(tagBox);
        });
    }

    private String formatStatus(String status) {
        switch (status) {
            case "EN_ATTENTE": return "En attente";
            case "EN_COURS": return "En cours";
            case "RESOLUE": return "Résolue";
            default: return status;
        }
    }

    private String formatPriority(String priority) {
        switch (priority) {
            case "URGENT": return "Urgent";
            case "NORMAL": return "Normal";
            case "FAIBLE": return "Faible";
            default: return priority;
        }
    }

    @FXML
    private void handleRefresh() {
        loadStatistics();
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            exportToPDF(file);
        }
    }

    private void exportToPDF(File file) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Rapport des Réclamations");
                contentStream.endText();

                // Statistiques générales
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Total des réclamations : " + totalReclamationsLabel.getText());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Temps moyen de résolution : " + avgResolutionTimeLabel.getText());
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Taux de résolution : " + resolutionRateLabel.getText());
                contentStream.endText();

                // Date du rapport
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Rapport généré le " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                contentStream.endText();
            }

            document.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 