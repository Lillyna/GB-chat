package com.example.gbchat1;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    final private ClientController controller;

    public ChatClient(ClientController controller) {
//        try {
//            Thread.sleep(120000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        this.controller = controller;
    }

    public void openConnection() throws Exception {
        socket = new Socket("localhost", 8181);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        final Thread readThread = new Thread(() -> {
            try {
                waitAuth();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }



    private void closeConnection() {
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(in!=null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(out!=null){
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);

    }

    private void readMessage() throws IOException {
        while (true) {
            String s = in.readUTF();
            System.out.println("Receive message: " + s);
            if (Command.isCommand(s)) {
                Command command = Command.getCommand(s);
                String[] params = command.parse(s);

                if (command == Command.END) {
                    controller.toogleBoxesVisibility(false);
                    break;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params));
                    continue;
                }
                if (command == Command.CLIENTS) {
                    controller.updateListClients(params);
                    continue;
                }
            }
            controller.addMessage(s);
        }
    }

    private void waitAuth() throws IOException {
        while (true) {

                final String msg = in.readUTF();
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);
                    if (command == Command.AUTHOK) {
                        final String nick = params[0];
                        controller.toogleBoxesVisibility(true);
                        controller.addMessage("Успешная авторизация под ником " + nick);
                        break;
                    }
                    if (Command.ERROR.equals(command)) {
                        Platform.runLater(() -> controller.showError(params));
                    }
                }
            }
    }

    public void sendMessage(String s) {
        try {
            System.out.println("Send message: " + s);
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
