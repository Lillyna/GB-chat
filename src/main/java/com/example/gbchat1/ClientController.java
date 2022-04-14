package com.example.gbchat1;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;

public class ClientController {

    private final ChatClient client;
    @FXML
    public ListView<String> clientList;

    public ClientController() {
        client = new ChatClient(this);
        while(true) {
            try {
                client.openConnection();
                break;
            } catch (Exception e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подклчиться к серверу.\n" +
                "Проверьте, что сервер запущен",
                new ButtonType("Попробовать еще раз", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE));
        alert.setTitle("Ошибка подключения");
        final Optional<ButtonType> buttonType = alert.showAndWait();
        final Boolean isExit = buttonType.map(btn -> btn.getButtonData().isCancelButton()).orElse(false);
        if(isExit){
            System.exit(0);
        }
    }

    @FXML
    private HBox loginBox;
    @FXML
    private HBox messageBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField textField;
    @FXML
    private Button sendButton;

    public void authButtonClick(ActionEvent authButton) {
        System.out.println("Отправляю сообщение " + Command.AUTH.getCommand() + " " + loginField.getText() + " " + passwordField.getText());
        client.sendMessage(Command.AUTH, loginField.getText(), passwordField.getText());
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
        Alert alert = new Alert(Alert.AlertType.ERROR, error[0], new ButtonType(
                "OK", ButtonBar.ButtonData.OK_DONE
        ));
        alert.setTitle("Ошибка!");
        alert.showAndWait();

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