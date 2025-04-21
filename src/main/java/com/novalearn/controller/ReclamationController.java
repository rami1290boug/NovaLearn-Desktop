package com.novalearn.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.novalearn.entity.Genre;
import com.novalearn.entity.Reclamation;
import com.novalearn.service.ReclamationService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReclamationController {
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<Genre> genreComboBox;
    @FXML
    private TableView<Reclamation> reclamationTable;
    @FXML
    private TableColumn<Reclamation, Long> idColumn;
    @FXML
    private TableColumn<Reclamation, String> titleColumn;
    @FXML
    private TableColumn<Reclamation, String> statusColumn;
    @FXML
    private TableColumn<Reclamation, String> priorityColumn;
    @FXML
    private TableColumn<Reclamation, LocalDateTime> dateColumn;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> priorityFilter;
    @FXML
    private DatePicker dateFilter;
    
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();
    private ObservableList<Genre> genreList = FXCollections.observableArrayList();
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/novalearn";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Reclamation currentReclamation;
    private ReclamationService reclamationService;
    private ObservableList<Reclamation> reclamations;

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            reclamationService = ReclamationService.getInstance();
            reclamations = FXCollections.observableArrayList();
            
            setupTable();
            setupFilters();
            loadGenres();
            loadReclamations();
        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        reclamationTable.setItems(reclamationList);
        
        reclamationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                currentReclamation = newSelection;
            }
        });
    }

    private void setupFilters() {
        // Implementation of setupFilters method
    }

    private void loadGenres() {
        String query = "SELECT * FROM genre ORDER BY libelle";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            genreList.clear();
            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("id"),
                    rs.getString("libelle"),
                    rs.getString("description")
                );
                genreList.add(genre);
            }
            
            genreComboBox.setItems(genreList);
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les types de réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadReclamations() {
        String query = "SELECT r.*, g.libelle as genre_libelle, g.description as genre_description " +
                      "FROM reclamation r " +
                      "LEFT JOIN genre g ON r.genre_id = g.id " +
                      "ORDER BY r.date_creation DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            
            reclamationList.clear();
            while (rs.next()) {
                Genre genre = null;
                if (rs.getObject("genre_id") != null) {
                    genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_libelle"),
                        rs.getString("genre_description")
                    );
                }
                
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getLong("id"));
                reclamation.setTitle(rs.getString("titre"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setStatus(rs.getString("statut"));
                reclamation.setCreatedAt(rs.getTimestamp("date_creation").toLocalDateTime());
                reclamation.setGenre(genre);
                reclamationList.add(reclamation);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les réclamations", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddReclamation() {
        if (titreField.getText().isEmpty() || descriptionField.getText().isEmpty() || genreComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.WARNING);
            return;
        }

        String query = "INSERT INTO reclamation (titre, description, statut, date_creation, genre_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, titreField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setString(3, "EN_ATTENTE");
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(5, genreComboBox.getValue().getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Reclamation reclamation = new Reclamation();
                        reclamation.setId(generatedKeys.getLong(1));
                        reclamation.setTitle(titreField.getText());
                        reclamation.setDescription(descriptionField.getText());
                        reclamation.setStatus("EN_ATTENTE");
                        reclamation.setCreatedAt(LocalDateTime.now());
                        reclamation.setGenre(genreComboBox.getValue());
                        
                        reclamationList.add(0, reclamation);
                        clearFields();
                        showAlert("Succès", "La réclamation a été ajoutée", Alert.AlertType.INFORMATION);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible d'ajouter la réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateReclamation() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
            return;
        }

        if (!selectedReclamation.getStatus().equals("EN_ATTENTE")) {
            showAlert("Erreur", "Seules les réclamations en attente peuvent être modifiées", Alert.AlertType.WARNING);
            return;
        }

        if (titreField.getText().isEmpty() || descriptionField.getText().isEmpty() || genreComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.WARNING);
            return;
        }

        String query = "UPDATE reclamation SET titre = ?, description = ?, genre_id = ? WHERE id = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, titreField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setInt(3, genreComboBox.getValue().getId());
            pstmt.setLong(4, selectedReclamation.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                selectedReclamation.setTitle(titreField.getText());
                selectedReclamation.setDescription(descriptionField.getText());
                selectedReclamation.setGenre(genreComboBox.getValue());
                reclamationTable.refresh();
                showAlert("Succès", "La réclamation a été mise à jour", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de mettre à jour la réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteReclamation() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
            return;
        }

        if (!selectedReclamation.getStatus().equals("EN_ATTENTE")) {
            showAlert("Erreur", "Seules les réclamations en attente peuvent être supprimées", Alert.AlertType.WARNING);
            return;
        }

        String query = "DELETE FROM reclamation WHERE id = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setLong(1, selectedReclamation.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                reclamationList.remove(selectedReclamation);
                clearFields();
                showAlert("Succès", "La réclamation a été supprimée", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer la réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenChat() {
        if (currentReclamation == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune réclamation sélectionnée");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une réclamation pour ouvrir le chat.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/chat.fxml"));
            Parent root = loader.load();
            
            ChatController chatController = loader.getController();
            chatController.setReclamation(currentReclamation);
            
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat - Réclamation #" + currentReclamation.getId());
            chatStage.setScene(new Scene(root));
            
            // Charger les styles
            chatStage.getScene().getStylesheets().add(
                getClass().getResource("/com/novalearn/view/styles/chat.css").toExternalForm()
            );
            
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'ouverture du chat.");
            alert.showAndWait();
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        genreComboBox.setValue(null);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEdit() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
            return;
        }

        titreField.setText(selectedReclamation.getTitle());
        descriptionField.setText(selectedReclamation.getDescription());
        genreComboBox.setValue(selectedReclamation.getGenre());
    }

    @FXML
    private void handleNew() {
        clearFields();
    }

    @FXML
    private void handleDelete() {
        handleDeleteReclamation();
    }

    @FXML
    private void handleFilter() {
        // Implementation of filter method
    }

    @FXML
    private void handleReset() {
        // Implementation of reset method
    }
} 