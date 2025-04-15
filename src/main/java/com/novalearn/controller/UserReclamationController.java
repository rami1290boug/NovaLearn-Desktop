package com.novalearn.controller;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.novalearn.entity.Genre;
import com.novalearn.entity.Reclamation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class UserReclamationController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Genre> genreComboBox;
    @FXML private ListView<Reclamation> reclamationListView;

    private Connection connection;
    private Stage primaryStage;
    private int currentUserId = 1; // À remplacer par l'ID de l'utilisateur connecté
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupListView();
        setupConnection();
        loadGenres();
        loadReclamations();
        setupListViewSelection();
    }

    private void setupConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/novalearn", "root", "");
        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
        }
    }

    private void loadGenres() {
        String query = "SELECT * FROM genre ORDER BY libelle";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            ObservableList<Genre> genres = FXCollections.observableArrayList();
            
            while (rs.next()) {
                Genre genre = new Genre();
                genre.setId(rs.getInt("id"));
                genre.setLibelle(rs.getString("libelle"));
                genre.setDescription(rs.getString("description"));
                genres.add(genre);
            }
            
            genreComboBox.setItems(genres);
            
            // Configuration de l'affichage du genre dans le ComboBox
            genreComboBox.setCellFactory(param -> new ListCell<Genre>() {
                @Override
                protected void updateItem(Genre item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getLibelle());
                    }
                }
            });
            
            genreComboBox.setButtonCell(new ListCell<Genre>() {
                @Override
                protected void updateItem(Genre item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getLibelle());
                    }
                }
            });
            
        } catch (SQLException e) {
            showError("Erreur lors du chargement des types de réclamation");
        }
    }

    private void setupListView() {
        reclamationListView.setCellFactory(param -> new ListCell<Reclamation>() {
            private VBox content;
            private Label titre;
            private Text description;
            private HBox statusBox;
            private Label status;
            private Label genre;
            private Label date;

            {
                titre = new Label();
                titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                description = new Text();
                description.setWrappingWidth(380);

                status = new Label();
                status.setStyle("-fx-padding: 2 8; -fx-background-radius: 4;");

                genre = new Label();
                genre.setStyle("-fx-padding: 2 8; -fx-background-radius: 4; -fx-background-color: #9C27B0; -fx-text-fill: white;");

                date = new Label();
                date.setStyle("-fx-text-fill: #666;");

                statusBox = new HBox(10);
                statusBox.getChildren().addAll(status, genre, date);

                content = new VBox(5);
                content.getChildren().addAll(titre, description, statusBox);
                content.setStyle("-fx-padding: 10;");
            }

            @Override
            protected void updateItem(Reclamation reclamation, boolean empty) {
                super.updateItem(reclamation, empty);

                if (empty || reclamation == null) {
                    setGraphic(null);
                } else {
                    titre.setText(reclamation.getTitre());
                    description.setText(reclamation.getDescription());
                    status.setText(reclamation.getStatut());
                    genre.setText(reclamation.getGenre() != null ? reclamation.getGenre().getLibelle() : "");
                    
                    // Style du statut
                    switch (reclamation.getStatut()) {
                        case "EN_ATTENTE":
                            status.setStyle(status.getStyle() + "-fx-background-color: #FFA726; -fx-text-fill: white;");
                            break;
                        case "EN_COURS":
                            status.setStyle(status.getStyle() + "-fx-background-color: #42A5F5; -fx-text-fill: white;");
                            break;
                        case "RESOLUE":
                            status.setStyle(status.getStyle() + "-fx-background-color: #66BB6A; -fx-text-fill: white;");
                            break;
                    }
                    
                    date.setText(DATE_FORMATTER.format(reclamation.getDateCreation()));
                    setGraphic(content);
                }
            }
        });
    }

    private void setupListViewSelection() {
        reclamationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titreField.setText(newSelection.getTitre());
                descriptionField.setText(newSelection.getDescription());
                genreComboBox.setValue(newSelection.getGenre());
            }
        });
    }

    private void loadReclamations() {
        String query = "SELECT r.*, g.id as genre_id, g.libelle as genre_libelle, g.description as genre_description " +
                      "FROM reclamation r " +
                      "LEFT JOIN genre g ON r.genre_id = g.id " +
                      "WHERE r.user_id = ? " +
                      "ORDER BY r.date_creation DESC";
        ObservableList<Reclamation> reclamations = FXCollections.observableArrayList();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setTitre(rs.getString("titre"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setStatut(rs.getString("statut"));
                reclamation.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                reclamation.setUserId(rs.getInt("user_id"));

                if (rs.getObject("genre_id") != null) {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("genre_id"));
                    genre.setLibelle(rs.getString("genre_libelle"));
                    genre.setDescription(rs.getString("genre_description"));
                    reclamation.setGenre(genre);
                }

                reclamations.add(reclamation);
            }

            reclamationListView.setItems(reclamations);
        } catch (SQLException e) {
            showError("Erreur lors du chargement des réclamations");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) return;

        String query = "INSERT INTO reclamation (titre, description, statut, date_creation, user_id, genre_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, titreField.getText());
            stmt.setString(2, descriptionField.getText());
            stmt.setString(3, "EN_ATTENTE");
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, currentUserId);
            
            if (genreComboBox.getValue() != null) {
                stmt.setInt(6, genreComboBox.getValue().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.executeUpdate();
            showInfo("Réclamation ajoutée avec succès");
            clearFields();
            loadReclamations();
        } catch (SQLException e) {
            showError("Erreur lors de l'ajout de la réclamation");
        }
    }

    @FXML
    private void handleUpdate() {
        Reclamation selected = reclamationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une réclamation à modifier");
            return;
        }

        if (!selected.getStatut().equals("EN_ATTENTE")) {
            showError("Seules les réclamations en attente peuvent être modifiées");
            return;
        }

        if (!validateInputs()) return;

        String query = "UPDATE reclamation SET titre = ?, description = ?, genre_id = ? WHERE id = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, titreField.getText());
            stmt.setString(2, descriptionField.getText());
            
            if (genreComboBox.getValue() != null) {
                stmt.setInt(3, genreComboBox.getValue().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showInfo("Réclamation modifiée avec succès");
                clearFields();
                loadReclamations();
            } else {
                showError("La réclamation ne peut plus être modifiée");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la modification de la réclamation");
        }
    }

    @FXML
    private void handleDelete() {
        Reclamation selected = reclamationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une réclamation à supprimer");
            return;
        }

        if (!selected.getStatut().equals("EN_ATTENTE")) {
            showError("Seules les réclamations en attente peuvent être supprimées");
            return;
        }

        String query = "DELETE FROM reclamation WHERE id = ? AND statut = 'EN_ATTENTE'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showInfo("Réclamation supprimée avec succès");
                clearFields();
                loadReclamations();
            } else {
                showError("La réclamation ne peut plus être supprimée");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la suppression de la réclamation");
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        reclamationListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/main_menu.fxml"));
            Parent root = loader.load();
            
            MainMenuController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("NovaLearn - Menu Principal");
        } catch (IOException e) {
            showError("Erreur lors du retour au menu principal");
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        genreComboBox.setValue(null);
    }

    private boolean validateInputs() {
        if (titreField.getText().trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return false;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            showError("La description est obligatoire");
            return false;
        }
        if (genreComboBox.getValue() == null) {
            showError("Le type de réclamation est obligatoire");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).show();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK).show();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
} 