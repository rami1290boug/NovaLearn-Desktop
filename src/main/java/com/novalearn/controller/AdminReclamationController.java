package com.novalearn.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private TableColumn<Reclamation, String> titreColumn;
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TableColumn<Reclamation, String> genreColumn;
    @FXML
    private TableColumn<Reclamation, LocalDateTime> dateCreationColumn;
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
    private Stage primaryStage;

    @FXML
    private void initialize() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            setupColumns();
            loadGenres();
            setupComboBoxes();
            loadReclamations();
            setupTableSelection();
            
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
            e.printStackTrace();
        }
    }

    private void setupColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        genreColumn.setCellValueFactory(cellData -> {
            Genre genre = cellData.getValue().getGenre();
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> genre != null ? genre.getLibelle() : ""
            );
        });
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
    }

    private void setupComboBoxes() {
        statutComboBox.getItems().addAll("EN_ATTENTE", "EN_COURS", "RESOLUE");
        genreComboBox.setItems(genreList);
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
        } catch (SQLException e) {
            showError("Erreur de chargement des types de réclamation");
            e.printStackTrace();
        }
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
            reclamationTable.setItems(reclamationList);
        } catch (SQLException e) {
            showError("Erreur de chargement des réclamations");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Veuillez sélectionner une réclamation");
            return;
        }

        if (validateInputs()) {
            String query = "UPDATE reclamation SET titre = ?, description = ?, statut = ?, genre_id = ? WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, titreField.getText());
                pstmt.setString(2, descriptionField.getText());
                pstmt.setString(3, statutComboBox.getValue());
                Genre selectedGenre = genreComboBox.getValue();
                if (selectedGenre != null) {
                    pstmt.setInt(4, selectedGenre.getId());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                }
                pstmt.setInt(5, selectedReclamation.getId());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    selectedReclamation.setTitre(titreField.getText());
                    selectedReclamation.setDescription(descriptionField.getText());
                    selectedReclamation.setStatut(statutComboBox.getValue());
                    selectedReclamation.setGenre(selectedGenre);
                    reclamationTable.refresh();
                    showAlert("Succès", "La réclamation a été mise à jour", Alert.AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showError("Erreur de mise à jour de la réclamation");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDelete() {
        Reclamation selectedReclamation = reclamationTable.getSelectionModel().getSelectedItem();
        if (selectedReclamation == null) {
            showError("Veuillez sélectionner une réclamation");
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
                showError("Erreur de suppression de la réclamation");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/main_menu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            showError("Erreur lors du retour au menu principal");
            e.printStackTrace();
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

            // Ajouter un écouteur pour rafraîchir la liste quand la fenêtre est fermée
            stage.setOnHidden(e -> {
                loadGenres();
                loadReclamations();
            });
        } catch (IOException e) {
            showError("Impossible d'ouvrir la fenêtre de gestion des types");
            e.printStackTrace();
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        statutComboBox.setValue(null);
        genreComboBox.setValue(null);
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            errors.append("- Le titre est obligatoire\n");
        } else if (titre.length() < 5) {
            errors.append("- Le titre doit contenir au moins 5 caractères\n");
        } else if (titre.length() > 100) {
            errors.append("- Le titre ne doit pas dépasser 100 caractères\n");
        }
        
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            errors.append("- La description est obligatoire\n");
        } else if (description.length() < 10) {
            errors.append("- La description doit contenir au moins 10 caractères\n");
        } else if (description.length() > 500) {
            errors.append("- La description ne doit pas dépasser 500 caractères\n");
        }
        
        if (statutComboBox.getValue() == null || statutComboBox.getValue().trim().isEmpty()) {
            errors.append("- Le statut est obligatoire\n");
        }

        if (genreComboBox.getValue() == null) {
            errors.append("- Le type de réclamation est obligatoire\n");
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
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

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 