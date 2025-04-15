package com.novalearn.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.novalearn.entity.Genre;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminGenreController {
    @FXML
    private TextField libelleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private TableView<Genre> genreTable;
    @FXML
    private TableColumn<Genre, String> libelleColumn;
    @FXML
    private TableColumn<Genre, String> descriptionColumn;

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
            setupTableSelection();
        } catch (SQLException e) {
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTable() {
        libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        genreTable.setItems(genreList);
    }

    private void setupTableSelection() {
        genreTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                libelleField.setText(newSelection.getLibelle());
                descriptionField.setText(newSelection.getDescription());
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
            showAlert("Erreur", "Impossible de charger les types de réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        if (libelleField.getText().isEmpty()) {
            showAlert("Erreur", "Le libellé est obligatoire", Alert.AlertType.WARNING);
            return;
        }

        String query = "INSERT INTO genre (libelle, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, libelleField.getText());
            pstmt.setString(2, descriptionField.getText());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Genre genre = new Genre(
                            generatedKeys.getInt(1),
                            libelleField.getText(),
                            descriptionField.getText()
                        );
                        genreList.add(genre);
                        clearFields();
                        showAlert("Succès", "Le type de réclamation a été ajouté", Alert.AlertType.INFORMATION);
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Code d'erreur MySQL pour violation de contrainte UNIQUE
                showAlert("Erreur", "Ce libellé existe déjà", Alert.AlertType.ERROR);
            } else {
                showAlert("Erreur", "Impossible d'ajouter le type de réclamation", Alert.AlertType.ERROR);
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Genre selectedGenre = genreTable.getSelectionModel().getSelectedItem();
        if (selectedGenre == null) {
            showAlert("Erreur", "Veuillez sélectionner un type de réclamation", Alert.AlertType.WARNING);
            return;
        }

        if (libelleField.getText().isEmpty()) {
            showAlert("Erreur", "Le libellé est obligatoire", Alert.AlertType.WARNING);
            return;
        }

        String query = "UPDATE genre SET libelle = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, libelleField.getText());
            pstmt.setString(2, descriptionField.getText());
            pstmt.setInt(3, selectedGenre.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                selectedGenre.setLibelle(libelleField.getText());
                selectedGenre.setDescription(descriptionField.getText());
                genreTable.refresh();
                showAlert("Succès", "Le type de réclamation a été mis à jour", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showAlert("Erreur", "Ce libellé existe déjà", Alert.AlertType.ERROR);
            } else {
                showAlert("Erreur", "Impossible de mettre à jour le type de réclamation", Alert.AlertType.ERROR);
            }
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Genre selectedGenre = genreTable.getSelectionModel().getSelectedItem();
        if (selectedGenre == null) {
            showAlert("Erreur", "Veuillez sélectionner un type de réclamation", Alert.AlertType.WARNING);
            return;
        }

        // Vérifier si le genre est utilisé dans des réclamations
        String checkQuery = "SELECT COUNT(*) FROM reclamation WHERE genre_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, selectedGenre.getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Erreur", "Ce type de réclamation est utilisé et ne peut pas être supprimé", Alert.AlertType.ERROR);
                return;
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de vérifier l'utilisation du type de réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
            return;
        }

        String query = "DELETE FROM genre WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, selectedGenre.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                genreList.remove(selectedGenre);
                clearFields();
                showAlert("Succès", "Le type de réclamation a été supprimé", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de supprimer le type de réclamation", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        genreTable.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        libelleField.clear();
        descriptionField.clear();
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