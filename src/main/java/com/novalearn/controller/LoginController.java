package com.novalearn.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.novalearn.entity.User;
import com.novalearn.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    private Stage primaryStage;
    private static final String URL = "jdbc:mysql://localhost:3306/novalearn";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs", Alert.AlertType.WARNING);
            return;
        }

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM user WHERE email = ? AND password = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, email);
                pstmt.setString(2, password); // Note: In production, use proper password hashing

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User(
                            rs.getLong("id"),
                            rs.getString("role"),
                            rs.getInt("id_fils"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getInt("age"),
                            rs.getInt("num_tel"),
                            rs.getString("difficulte"),
                            rs.getString("niv_difficulte"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("genre"),
                            rs.getString("specialite"),
                            rs.getBoolean("is_verified"),
                            rs.getString("verification_token")
                        );
                        
                        // Store user in session
                        SessionManager.getInstance().setCurrentUser(user);

                        // Load main menu
                        loadMainMenu();
                    } else {
                        showAlert("Erreur", "Email ou mot de passe incorrect", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de connexion à la base de données", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir la page d'inscription", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/main_menu.fxml"));
            Scene scene = new Scene(loader.load());
            
            MainMenuController controller = loader.getController();
            controller.setPrimaryStage(primaryStage != null ? primaryStage : (Stage) emailField.getScene().getWindow());
            
            Stage stage = primaryStage != null ? primaryStage : (Stage) emailField.getScene().getWindow();
            stage.setTitle("NovaLearn - Menu Principal");
            stage.setScene(scene);
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger le menu principal", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 