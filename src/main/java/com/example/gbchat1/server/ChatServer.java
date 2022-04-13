package com.example.gbchat1.server;

import com.example.gbchat1.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ChatServer {

    private final AuthService authService;
    private final HashMap<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
        this.authService = new AuthServiceImpl();
    }

    public boolean isNickBusy(String nick) {

            if (clients.containsKey(nick)) {
                return true;

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
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }

    }

    public void sendMessage(String message, String nick) {
        for (ClientHandler client : clients.values()) {
            if (message.startsWith(client.getNick()) || nick.equals(client.getNick())) {
                client.sendMessage(message);
            }
        }

    }

    public void subscribe(ClientHandler client) {
        //clients.add(client);
        clients.put(client.getNick(), client);
        broadcastClientList();

    }

    private void broadcastClientList() {
        StringBuilder nicks = new StringBuilder();
        for (ClientHandler value : clients.values()){
            nicks.append(value.getNick()).append(" ");
        }
//        String nicks = clients.values().stream()
//                .map(client -> client.getNick())
//                .collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broadcast(Command clients, String nicks) {
        for (ClientHandler client : clients.values()){
            client.sendMessage();
        }
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientList();

    }
    public void sendMessage (ClientHandler sender, String to, String message){
        ClientHandler receiver = clients.get(to);
        if(sender != null){
            sender.sendMessage("От " + sender.getNick() + ": " + message;
            sender.sendMessage("участнику " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
        }

    }
}
