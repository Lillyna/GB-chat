package com.example.gbchat1;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ClientController {

    private final ChatClient client;
    @FXML
    public ListView<String> clientList;

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
        client.sendMessage(Command.AUTH, loginField.getText() , passwordField.getText());
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

    public void showError(String[] error){
        new Alert(Alert.AlertType.ERROR, error[0], new ButtonType(
                "OK", ButtonBar.ButtonData.OK_DONE
        ));
    }
    public void selectClient(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            String text = textField.getText();
            String nick = clientList.getSelectionModel().getSelectedItem();
            textField.setText(Command.PRIVATE_MESSAGE.collectMessage(nick, text));
            textField.requestFocus();
            textField.selectEnd();
        }
    }


    public void updateListClients(String[]params) {
        clientList.getItems().clear();
        clientList.getItems().addAll(params);
    }
}