package com.example.gbchat1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ClientController {

    private final ChatClient client;

    public ClientController() {
        this.client = new ChatClient(this);
        try {
            client.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private HBox loginBox;
    @FXML
    private VBox messageBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;

    public void authButtonClick(ActionEvent authButton) {
        client.sendMessage("/auth " + loginField.getText() + " " + passwordField.getText());
        loginField.clear();
        passwordField.clear();
    }

    public void sendButtonClick(ActionEvent actionEvent) {
        String text = textField.getText();
        if (text.trim().isEmpty()) {
            return;
        }
        client.sendMessage(text);
        textField.clear();
        textField.requestFocus();

    }

    public void addMessage(String s) {
        messageArea.appendText(s + "\n");
    }

    public void toogleBoxesVisibility(boolean isSuccess) {
        loginBox.setVisible(!isSuccess);
        messageBox.setVisible(isSuccess);

    }


}