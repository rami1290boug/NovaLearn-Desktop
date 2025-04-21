package com.novalearn.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.novalearn.entity.Message;
import com.novalearn.entity.Reclamation;
import com.novalearn.service.MessageService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatController {
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private ComboBox<String> filterSender;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private TextField searchKeyword;
    @FXML private CheckBox agentOnlyCheckbox;

    private Reclamation currentReclamation;
    private MessageService messageService;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        messageService = MessageService.getInstance();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());
        
        // Setup filter handlers
        filterSender.setOnAction(e -> applyFilters());
        startDate.setOnAction(e -> applyFilters());
        endDate.setOnAction(e -> applyFilters());
        searchKeyword.setOnAction(e -> applyFilters());
        agentOnlyCheckbox.setOnAction(e -> applyFilters());
    }

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;
        loadMessages();
    }

    private void loadMessages() {
        if (currentReclamation == null) return;
        
        List<Message> messages = messageService.getMessagesByReclamation(currentReclamation);
        chatContainer.getChildren().clear();
        
        for (Message message : messages) {
            addMessageToChat(message);
        }
        
        // Scroll to bottom
        chatScrollPane.setVvalue(1.0);
    }

    private void addMessageToChat(Message message) {
        HBox messageBox = new HBox(10);
        messageBox.getStyleClass().add(message.isAgent() ? "agent-message" : "user-message");
        
        VBox contentBox = new VBox(5);
        
        TextFlow textFlow = new TextFlow(new Text(message.getContent()));
        textFlow.getStyleClass().add("message-content");
        
        Text timeText = new Text(message.getSentAt().format(TIME_FORMATTER));
        timeText.getStyleClass().add("message-time");
        
        contentBox.getChildren().addAll(textFlow, timeText);
        messageBox.getChildren().add(contentBox);
        
        chatContainer.getChildren().add(messageBox);
    }

    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentReclamation == null) return;
        
        Message message = new Message(
            currentReclamation,
            content,
            "User", // TODO: Replace with actual user name
            false
        );
        
        messageService.saveMessage(message);
        addMessageToChat(message);
        messageInput.clear();
        
        // Scroll to bottom
        chatScrollPane.setVvalue(1.0);
    }

    private void applyFilters() {
        if (currentReclamation == null) return;
        
        List<Message> messages = messageService.searchMessages(
            searchKeyword.getText(),
            startDate.getValue() != null ? startDate.getValue().atStartOfDay() : null,
            endDate.getValue() != null ? endDate.getValue().atTime(23, 59, 59) : null,
            filterSender.getValue(),
            agentOnlyCheckbox.isSelected() ? true : null,
            currentReclamation
        );
        
        chatContainer.getChildren().clear();
        for (Message message : messages) {
            addMessageToChat(message);
        }
    }
} 