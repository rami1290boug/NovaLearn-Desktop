package com.novalearn.controller;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

import com.novalearn.entity.Genre;
import com.novalearn.entity.Reclamation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AdminReclamationController {
    @FXML
    private TableView<Reclamation> reclamationTable;
    @FXML
    private TableColumn<Reclamation, Integer> idColumn;
    @FXML
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    @FXML
    private TableColumn<Reclamation, String> genreColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<Reclamation, Integer> userIdColumn;
    
    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private ComboBox<Genre> genreComboBox;

    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();
    private ObservableList<Genre> genreList = FXCollections.observableArrayList();
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/novalearn";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            setupTable();
            loadGenres();
            setupComboBox();
            loadReclamations();
            setupTableSelection();
        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        genreColumn.setCellValueFactory(cellData -> {
            Genre genre = cellData.getValue().getGenre();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> genre != null ? genre.getLibelle() : ""
            );
        });
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
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

    private void setupComboBox() {
        statutComboBox.getItems().addAll("EN_ATTENTE", "EN_COURS", "RESOLUE");
    }

    private void setupTableSelection() {
        reclamationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titreField.setText(newSelection.getTitre());
                descriptionField.setText(newSelection.getDescription());
                statutComboBox.setValue(newSelection.getStatut());
                genreComboBox.setValue(newSelection.getGenre());
            }
        });
    }

    private void loadReclamations() {
        String query = "SELECT r.*, g.libelle as genre_libelle, g.description as genre_description " +
                      "FROM reclamation r " +
                      "LEFT JOIN genre g ON r.genre_id = g.id " +
                      "ORDER BY r.date_creation DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
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
    private void handleUpdateReclamation() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation", Alert.AlertType.WARNING);
            return;
        }

        if (titreField.getText().isEmpty() || descriptionField.getText().isEmpty() || 
            genreComboBox.getValue() == null || statutComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.WARNING);
            return;
        }

        String query = "UPDATE reclamation SET titre = ?, description = ?, statut = ?, genre_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, titreField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setString(3, statutComboBox.getValue());
            pstmt.setInt(4, genreComboBox.getValue().getId());
            pstmt.setInt(5, selectedReclamation.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                selectedReclamation.setTitre(titreField.getText());
                selectedReclamation.setDescription(descriptionField.getText());
                selectedReclamation.setStatut(statutComboBox.getValue());
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

        Optional<ButtonType> result = showConfirmation(
            "Confirmation de suppression",
            "Êtes-vous sûr de vouloir supprimer cette réclamation ?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "DELETE FROM reclamation WHERE id = ?";
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
    }

    @FXML
    private void handleRefresh() {
        loadReclamations();
        loadGenres();
        clearFields();
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        statutComboBox.setValue(null);
        genreComboBox.setValue(null);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
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
    private void handleManageGenres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/admin_genre.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Gestion des Types de Réclamation");
            stage.setScene(new Scene(root));
            stage.show();

            // Ajouter un écouteur pour rafraîchir la liste des genres quand la fenêtre est fermée
            stage.setOnHidden(e -> {
                loadGenres();
                loadReclamations();
            });
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de gestion des types", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
} 