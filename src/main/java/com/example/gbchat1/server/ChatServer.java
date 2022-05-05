package com.example.gbchat1.server;

import com.example.gbchat1.Command;
import com.example.gbchat1.db.DbConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatServer {
    private static final int MAX_CLIENTS = 10;

    private final Map<String, ClientHandler> clients;
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_CLIENTS);

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8181);
             AuthService authService = new AuthServiceImpl()) {
            while (true) {
                System.out.println("Wait client connection...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService, executorService);
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        StringBuilder nicks = new StringBuilder();
        for (ClientHandler value : clients.values()) {
            nicks.append(value.getNick()).append(" ");
        }
        broadcast(Command.CLIENTS, nicks.toString().trim());
    }

    private void broadcast(Command command, String nicks) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, nicks);
        }
    }

    public void broadcast(String msg) {
        clients.values().forEach(client -> client.sendMessage(msg));
    }

    public void sendMessageToClient(ClientHandler sender, String to, String message) {
        final ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.sendMessage("от " + sender.getNick() + ": " + message);
            sender.sendMessage("участнику " + to + ": " + message);
        } else {
            sender.sendMessage(Command.ERROR, "Участника с ником " + to + " нет в чате!");
        }
    }

    public String changeNick(String oldNick, String newNick) throws SQLException {
        DbConnection dbConnection = new DbConnection(oldNick, newNick);
        return newNick;
    }
}
