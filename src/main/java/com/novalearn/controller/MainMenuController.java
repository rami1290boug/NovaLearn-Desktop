package com.novalearn.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainMenuController {
    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/user/UserView.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Gestion des Utilisateurs");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleQuizManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/quiz/quizView.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Gestion des Quiz");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUserReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/user_reclamation.fxml"));
            Parent root = loader.load();
            
            UserReclamationController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Mes Réclamations");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdminReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/novalearn/view/admin_reclamation.fxml"));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Administration des Réclamations");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 