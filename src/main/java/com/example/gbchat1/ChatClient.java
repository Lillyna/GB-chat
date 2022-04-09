package com.example.gbchat1;

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
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessage();
            } finally {
                closeConnection(socket);
            }
        }).start();
    }

    private void closeConnection(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (true) {
            try {
                String s = in.readUTF();
                controller.addMessage(s);
                if ("/end".equals(s)) {
                    controller.toogleBoxesVisibility(false);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void waitAuth() {
        while (true) {
            try {
                final String msg = in.readUTF();
                if (msg.startsWith("/authok")) {
                    String[] split = msg.split(" ");
                    String nick = split[1];
                    controller.toogleBoxesVisibility(true);
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
