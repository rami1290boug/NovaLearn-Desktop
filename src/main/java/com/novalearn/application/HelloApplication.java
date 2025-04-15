package com.novalearn.application;

import com.novalearn.controller.MainMenuController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/novalearn/view/main_menu.fxml")
        );
        Parent root = loader.load();
        
        MainMenuController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        
        primaryStage.setTitle("NovaLearn - Menu Principal");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
