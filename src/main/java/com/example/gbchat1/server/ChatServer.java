package com.example.gbchat1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final AuthService authService;
    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
        this.authService = new AuthServiceImpl();
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public void run() {
        try (
                ServerSocket serverSocket = new ServerSocket(8189)
        ) {
            while (true) {
                System.out.println("Ожидаем подключения клиента");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this);
                System.out.println("Клиент подключился");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сервера", e);
        }

    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }

    }



    public void subscribe(ClientHandler client) {
        clients.add(client);

    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);

    }
}
