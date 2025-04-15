package com.novalearn.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

public class MainMenuController {
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/user/UserView.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Gestion des Utilisateurs");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la vue utilisateurs");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/quiz/quizView.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Gestion des Quiz");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la vue quiz");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUserReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/user_reclamation.fxml"));
            Parent root = loader.load();
            
            UserReclamationController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Mes Réclamations");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la vue réclamations");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminReclamations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/admin_reclamation.fxml"));
            Parent root = loader.load();
            
            AdminReclamationController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Administration des Réclamations");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la vue administration des réclamations");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/login.fxml"));
            Parent root = loader.load();
            
            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("NovaLearn - Connexion");
        } catch (IOException e) {
            showError("Erreur lors du chargement de la vue connexion");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 