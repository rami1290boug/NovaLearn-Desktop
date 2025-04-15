package com.novalearn.controller;

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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, String> genreColumn;
    @FXML
    private TableColumn<Reclamation, LocalDateTime> dateColumn;
    
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();
    private ObservableList<Genre> genreList = FXCollections.observableArrayList();
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/novalearn";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            setupTable();
            loadGenres();
            loadReclamations();
        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTable() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        genreColumn.setCellValueFactory(cellData -> {
            Genre genre = cellData.getValue().getGenre();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> genre != null ? genre.getLibelle() : ""
            );
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        reclamationTable.setItems(reclamationList);
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
                reclamation.setId(rs.getInt("id"));
                reclamation.setTitre(rs.getString("titre"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setStatut(rs.getString("statut"));
                reclamation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
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
                        reclamation.setId(generatedKeys.getInt(1));
                        reclamation.setTitre(titreField.getText());
                        reclamation.setDescription(descriptionField.getText());
                        reclamation.setStatut("EN_ATTENTE");
                        reclamation.setDateCreation(LocalDateTime.now());
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

        if (!selectedReclamation.getStatut().equals("EN_ATTENTE")) {
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
            pstmt.setInt(4, selectedReclamation.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                selectedReclamation.setTitre(titreField.getText());
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

        if (!selectedReclamation.getStatut().equals("EN_ATTENTE")) {
            showAlert("Erreur", "Seules les réclamations en attente peuvent être supprimées", Alert.AlertType.WARNING);
            return;
        }

        String query = "DELETE FROM reclamation WHERE id = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, selectedReclamation.getId());
            
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
} 