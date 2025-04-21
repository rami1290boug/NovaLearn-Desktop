package com.novalearn.controller;

import com.novalearn.entity.AccessibilitySettings;
import com.novalearn.service.AccessibilityService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class AccessibilitySettingsController {
    @FXML private CheckBox highContrastCheckBox;
    @FXML private CheckBox textToSpeechCheckBox;
    @FXML private CheckBox voiceInputCheckBox;
    @FXML private CheckBox wordSuggestionsCheckBox;
    @FXML private Slider fontSizeSlider;
    @FXML private ComboBox<String> fontFamilyComboBox;
    @FXML private CheckBox lineHighlightCheckBox;
    @FXML private Slider lineSpacingSlider;

    private AccessibilityService accessibilityService;
    private AccessibilitySettings settings;
    private Stage stage;

    @FXML
    public void initialize() {
        accessibilityService = AccessibilityService.getInstance();
        settings = accessibilityService.getSettings();
        
        // Initialiser les contrôles avec les valeurs actuelles
        highContrastCheckBox.setSelected(settings.isHighContrast());
        textToSpeechCheckBox.setSelected(settings.isTextToSpeech());
        voiceInputCheckBox.setSelected(settings.isVoiceInput());
        wordSuggestionsCheckBox.setSelected(settings.isWordSuggestions());
        fontSizeSlider.setValue(settings.getFontSize());
        lineHighlightCheckBox.setSelected(settings.isLineHighlight());
        lineSpacingSlider.setValue(settings.getLineSpacing());

        // Initialiser la liste des polices
        ObservableList<String> fonts = FXCollections.observableArrayList(
            "OpenDyslexic",
            "Arial",
            "Verdana",
            "Comic Sans MS",
            "Times New Roman"
        );
        fontFamilyComboBox.setItems(fonts);
        fontFamilyComboBox.setValue(settings.getFontFamily());
    }

    @FXML
    private void handleApply() {
        // Mettre à jour les paramètres
        settings.setHighContrast(highContrastCheckBox.isSelected());
        settings.setTextToSpeech(textToSpeechCheckBox.isSelected());
        settings.setVoiceInput(voiceInputCheckBox.isSelected());
        settings.setWordSuggestions(wordSuggestionsCheckBox.isSelected());
        settings.setFontSize((int) fontSizeSlider.getValue());
        settings.setFontFamily(fontFamilyComboBox.getValue());
        settings.setLineHighlight(lineHighlightCheckBox.isSelected());
        settings.setLineSpacing((int) lineSpacingSlider.getValue());

        // Sauvegarder les paramètres
        accessibilityService.setSettings(settings);

        // Fermer la fenêtre
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    private void handleCancel() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
} 