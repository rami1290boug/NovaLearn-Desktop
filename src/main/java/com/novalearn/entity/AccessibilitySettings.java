package com.novalearn.entity;

public class AccessibilitySettings {
    private boolean highContrast;
    private boolean textToSpeech;
    private boolean voiceInput;
    private boolean wordSuggestions;
    private int fontSize;
    private String fontFamily;
    private boolean lineHighlight;
    private int lineSpacing;

    public AccessibilitySettings() {
        // Valeurs par défaut
        this.highContrast = false;
        this.textToSpeech = true;
        this.voiceInput = true;
        this.wordSuggestions = true;
        this.fontSize = 14;
        this.fontFamily = "OpenDyslexic";
        this.lineHighlight = true;
        this.lineSpacing = 1;
    }

    // Getters et Setters
    public boolean isHighContrast() {
        return highContrast;
    }

    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
    }

    public boolean isTextToSpeech() {
        return textToSpeech;
    }

    public void setTextToSpeech(boolean textToSpeech) {
        this.textToSpeech = textToSpeech;
    }

    public boolean isVoiceInput() {
        return voiceInput;
    }

    public void setVoiceInput(boolean voiceInput) {
        this.voiceInput = voiceInput;
    }

    public boolean isWordSuggestions() {
        return wordSuggestions;
    }

    public void setWordSuggestions(boolean wordSuggestions) {
        this.wordSuggestions = wordSuggestions;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public boolean isLineHighlight() {
        return lineHighlight;
    }

    public void setLineHighlight(boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
    }

    public int getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }
} 