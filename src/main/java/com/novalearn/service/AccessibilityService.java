package com.novalearn.service;

import com.novalearn.entity.AccessibilitySettings;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class AccessibilityService {
    private static AccessibilityService instance;
    private AccessibilitySettings settings;

    private AccessibilityService() {
        settings = new AccessibilitySettings();
    }

    public static AccessibilityService getInstance() {
        if (instance == null) {
            instance = new AccessibilityService();
        }
        return instance;
    }

    public void applyAccessibilitySettings(Region region) {
        if (region instanceof TextInputControl) {
            applyTextInputSettings((TextInputControl) region);
        } else if (region instanceof Label) {
            applyLabelSettings((Label) region);
        } else if (region instanceof TableView) {
            applyTableViewSettings((TableView<?>) region);
        } else if (region instanceof ListView) {
            applyListViewSettings((ListView<?>) region);
        } else if (region instanceof ComboBox) {
            applyComboBoxSettings((ComboBox<?>) region);
        }
    }

    private void applyTextInputSettings(TextInputControl textInput) {
        // Appliquer la police et la taille
        textInput.setFont(Font.font(settings.getFontFamily(), 
                                  FontWeight.NORMAL, 
                                  FontPosture.REGULAR, 
                                  settings.getFontSize()));

        // Appliquer l'espacement des lignes pour TextArea
        if (textInput instanceof TextArea) {
            textInput.setStyle(String.format("-fx-line-spacing: %d;", settings.getLineSpacing()));
        }

        // Appliquer le mode contraste élevé
        if (settings.isHighContrast()) {
            textInput.setStyle(textInput.getStyle() + 
                             "-fx-text-fill: white; -fx-background-color: black;");
        }
    }

    private void applyLabelSettings(Label label) {
        label.setFont(Font.font(settings.getFontFamily(), 
                              FontWeight.NORMAL, 
                              FontPosture.REGULAR, 
                              settings.getFontSize()));
        
        if (settings.isHighContrast()) {
            label.setTextFill(Color.WHITE);
        }
    }

    private void applyTableViewSettings(TableView<?> table) {
        table.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpx;", 
                                   settings.getFontFamily(), 
                                   settings.getFontSize()));

        if (settings.isHighContrast()) {
            table.setStyle(table.getStyle() + 
                         "-fx-text-fill: white; -fx-background-color: black;");
        }

        // Appliquer les paramètres aux colonnes
        for (TableColumn<?, ?> column : table.getColumns()) {
            column.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpx;", 
                                        settings.getFontFamily(), 
                                        settings.getFontSize()));
        }
    }

    private void applyListViewSettings(ListView<?> listView) {
        listView.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpx;", 
                                      settings.getFontFamily(), 
                                      settings.getFontSize()));

        if (settings.isHighContrast()) {
            listView.setStyle(listView.getStyle() + 
                            "-fx-text-fill: white; -fx-background-color: black;");
        }
    }

    private void applyComboBoxSettings(ComboBox<?> comboBox) {
        comboBox.setStyle(String.format("-fx-font-family: %s; -fx-font-size: %dpx;", 
                                      settings.getFontFamily(), 
                                      settings.getFontSize()));

        if (settings.isHighContrast()) {
            comboBox.setStyle(comboBox.getStyle() + 
                            "-fx-text-fill: white; -fx-background-color: black;");
        }
    }

    public AccessibilitySettings getSettings() {
        return settings;
    }

    public void setSettings(AccessibilitySettings settings) {
        this.settings = settings;
    }
} 