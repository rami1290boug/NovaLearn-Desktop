package com.novalearn.controller;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

import com.novalearn.entity.Genre;
import com.novalearn.entity.Reclamation;
import com.novalearn.service.AccessibilityService;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class UserReclamationController {
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<Genre> genreComboBox;
    @FXML private ListView<Reclamation> reclamationListView;
    @FXML private Button settingsButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> sortField;
    @FXML private ComboBox<String> sortOrder;
    @FXML private Label totalLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label resoluesLabel;

    private Connection connection;
    private Stage primaryStage;
    private int currentUserId = 1; // À remplacer par l'ID de l'utilisateur connecté
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private AccessibilityService accessibilityService;
    private ObservableList<Reclamation> reclamations;

    @FXML
    public void initialize() {
        accessibilityService = AccessibilityService.getInstance();
        setupListView();
        setupConnection();
        setupFilters();
        loadGenres();
        loadReclamations();
        setupListViewSelection();
        setupAccessibility();
        setupKeyboardShortcuts();
        updateStatistics();
    }

    private void setupAccessibility() {
        // Appliquer les paramètres d'accessibilité
        accessibilityService.applyAccessibilitySettings(titreField);
        accessibilityService.applyAccessibilitySettings(descriptionField);
        accessibilityService.applyAccessibilitySettings(genreComboBox);
        accessibilityService.applyAccessibilitySettings(reclamationListView);

        // Ajouter le bouton des paramètres d'accessibilité
        settingsButton = new Button("Paramètres d'Accessibilité");
        settingsButton.setOnAction(e -> showAccessibilitySettings());
    }

    private void setupKeyboardShortcuts() {
        // Raccourcis clavier pour la synthèse vocale
        titreField.setOnKeyPressed(this::handleKeyPress);
        descriptionField.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.F1) {
            // Lire le texte du champ actif
            TextInputControl source = (TextInputControl) event.getSource();
            readText(source.getText());
        }
    }

    private void readText(String text) {
        if (accessibilityService.getSettings().isTextToSpeech()) {
            // TODO: Implémenter la synthèse vocale
            // Pour l'instant, on affiche juste une alerte
            showInfo("Lecture du texte : " + text);
        }
    }

    private void showAccessibilitySettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/accessibility_settings.fxml"));
            Parent root = loader.load();
            
            AccessibilitySettingsController controller = loader.getController();
            Stage settingsStage = new Stage();
            controller.setStage(settingsStage);
            
            settingsStage.setScene(new Scene(root));
            settingsStage.setTitle("Paramètres d'Accessibilité");
            settingsStage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement des paramètres d'accessibilité");
            e.printStackTrace();
        }
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
                    titre.setText(reclamation.getTitle());
                    description.setText(reclamation.getDescription());
                    status.setText(reclamation.getStatus());
                    genre.setText(reclamation.getGenre() != null ? reclamation.getGenre().getLibelle() : "");
                    
                    // Style du statut
                    switch (reclamation.getStatus()) {
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
                    
                    date.setText(reclamation.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    setGraphic(content);
                }
            }
        });

        // Ajouter le bouton des paramètres d'accessibilité à la liste
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().add(settingsButton);
        // Ajouter buttonBox à l'interface appropriée
    }

    private void setupListViewSelection() {
        reclamationListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titreField.setText(newSelection.getTitle());
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
                      "ORDER BY r.created_at DESC";
        reclamations = FXCollections.observableArrayList();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getLong("id"));
                reclamation.setTitle(rs.getString("title"));
                reclamation.setDescription(rs.getString("description"));
                reclamation.setStatus(rs.getString("status"));
                reclamation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                reclamation.setUserId(rs.getLong("user_id"));

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
            e.printStackTrace();
        }
    }

    private void setupFilters() {
        // Configuration des filtres de statut
        statusFilter.getItems().addAll("Tous", "EN_ATTENTE", "EN_COURS", "RESOLVED");
        statusFilter.setValue("Tous");
        
        // Configuration du filtre de type
        typeFilter.setValue("Tous");
        
        // Configuration du tri
        sortField.getItems().addAll("Date", "Titre", "Statut", "Type");
        sortField.setValue("Date");
        sortOrder.getItems().addAll("Croissant", "Décroissant");
        sortOrder.setValue("Décroissant");
        
        // Listeners pour appliquer les filtres automatiquement
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        String type = typeFilter.getValue();
        LocalDate date = dateFilter.getValue();

        ObservableList<Reclamation> filteredList = reclamations.filtered(reclamation -> {
            boolean matchesSearch = searchText.isEmpty() ||
                                  reclamation.getTitle().toLowerCase().contains(searchText) ||
                                  reclamation.getDescription().toLowerCase().contains(searchText);
            
            boolean matchesStatus = status.equals("Tous") ||
                                  reclamation.getStatus().equals(status);
            
            boolean matchesType = type.equals("Tous") ||
                                (reclamation.getGenre() != null &&
                                 reclamation.getGenre().getLibelle().equals(type));
            
            boolean matchesDate = date == null ||
                                reclamation.getCreatedAt().toLocalDate().equals(date);
            
            return matchesSearch && matchesStatus && matchesType && matchesDate;
        });

        reclamationListView.setItems(filteredList);
        updateStatistics();
    }

    @FXML
    private void handleSort() {
        String field = sortField.getValue();
        boolean ascending = sortOrder.getValue().equals("Croissant");
        
        reclamationListView.getItems().sort((r1, r2) -> {
            int result = 0;
            switch (field) {
                case "Date":
                    result = r1.getCreatedAt().compareTo(r2.getCreatedAt());
                    break;
                case "Titre":
                    result = r1.getTitle().compareTo(r2.getTitle());
                    break;
                case "Statut":
                    result = r1.getStatus().compareTo(r2.getStatus());
                    break;
                case "Type":
                    String type1 = r1.getGenre() != null ? r1.getGenre().getLibelle() : "";
                    String type2 = r2.getGenre() != null ? r2.getGenre().getLibelle() : "";
                    result = type1.compareTo(type2);
                    break;
            }
            return ascending ? result : -result;
        });
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        statusFilter.setValue("Tous");
        typeFilter.setValue("Tous");
        dateFilter.setValue(null);
        sortField.setValue("Date");
        sortOrder.setValue("Décroissant");
        loadReclamations();
    }

    private void updateStatistics() {
        ObservableList<Reclamation> items = reclamationListView.getItems();
        
        totalLabel.setText(String.valueOf(items.size()));
        
        long enAttente = items.stream()
            .filter(r -> r.getStatus().equals("EN_ATTENTE"))
            .count();
        enAttenteLabel.setText(String.valueOf(enAttente));
        
        long enCours = items.stream()
            .filter(r -> r.getStatus().equals("EN_COURS"))
            .count();
        enCoursLabel.setText(String.valueOf(enCours));
        
        long resolues = items.stream()
            .filter(r -> r.getStatus().equals("RESOLVED"))
            .count();
        resoluesLabel.setText(String.valueOf(resolues));
    }

    @FXML
    private void handleExportPDF() {
        // TODO: Implémenter l'export PDF
        showInfo("Export PDF en cours de développement");
    }

    @FXML
    private void handleExportExcel() {
        // TODO: Implémenter l'export Excel
        showInfo("Export Excel en cours de développement");
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) return;

        String query = "INSERT INTO reclamation (title, description, status, created_at, user_id, genre_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, titreField.getText());
            stmt.setString(2, descriptionField.getText());
            stmt.setString(3, "EN_ATTENTE");
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(5, currentUserId);
            
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
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Reclamation selected = reclamationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une réclamation à modifier");
            return;
        }

        if (!selected.getStatus().equals("EN_ATTENTE")) {
            showError("Seules les réclamations en attente peuvent être modifiées");
            return;
        }

        if (!validateInputs()) return;

        String query = "UPDATE reclamation SET title = ?, description = ?, genre_id = ? WHERE id = ? AND status = 'EN_ATTENTE'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, titreField.getText());
            stmt.setString(2, descriptionField.getText());
            
            if (genreComboBox.getValue() != null) {
                stmt.setInt(3, genreComboBox.getValue().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setLong(4, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showInfo("Réclamation modifiée avec succès");
                clearFields();
                loadReclamations();
            } else {
                showError("La réclamation ne peut plus être modifiée");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la modification de la réclamation");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Reclamation selected = reclamationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une réclamation à supprimer");
            return;
        }

        if (!selected.getStatus().equals("EN_ATTENTE")) {
            showError("Seules les réclamations en attente peuvent être supprimées");
            return;
        }

        String query = "DELETE FROM reclamation WHERE id = ? AND status = 'EN_ATTENTE'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, selected.getId());

            if (stmt.executeUpdate() > 0) {
                showInfo("Réclamation supprimée avec succès");
                clearFields();
                loadReclamations();
            } else {
                showError("La réclamation ne peut plus être supprimée");
            }
        } catch (SQLException e) {
            showError("Erreur lors de la suppression de la réclamation");
            e.printStackTrace();
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

    @FXML
    private void handleNewReclamation() {
        clearFields();
        // Reset selection
        reclamationListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSettings() {
        showAccessibilitySettings();
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        genreComboBox.setValue(null);
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        // Validation du titre
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) {
            errors.append("- Le titre is obligatoire\n");
        } else if (titre.length() < 5) {
            errors.append("- Le titre doit contenir au moins 5 caractères\n");
        } else if (titre.length() > 100) {
            errors.append("- Le titre ne doit pas dépasser 100 caractères\n");
        }
        
        // Validation de la description
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            errors.append("- La description is obligatoire\n");
        } else if (description.length() < 10) {
            errors.append("- La description doit contenir au moins 10 caractères\n");
        } else if (description.length() > 500) {
            errors.append("- La description ne doit pas dépasser 500 caractères\n");
        }
        
        // Validation du genre
        if (genreComboBox.getValue() == null) {
            errors.append("- Le type de réclamation is obligatoire\n");
        }
        
        // Si des erreurs sont présentes
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