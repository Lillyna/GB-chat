package com.example.gbchat1.server;

import com.example.gbchat1.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler {
    final private Socket socket;
    final private ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private boolean isConnected = false;
    AuthService authService;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer chatServer, AuthService authService) {

        try {
            this.nick = "";
            this.socket = socket;
            this.server = chatServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;

            Thread readThread = new Thread(() -> {

                while (true) {
                    try {
                        authenticate();
                        readMessage();
                    } finally {
                        closeConnection();
                    }
                }
            });
            readThread.start();
            new Thread(() ->
            {

                try {
                    Thread.sleep(120_00);
                    if (!isConnected) {
                        closeConnection();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка подключения к клиенту", e);
        }

    }

    public void sendMessage(String message) {
        try {
            System.out.println("Отправляю сообщение " + message);
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        try {
            while (true) {
                final String msg = in.readUTF();
                System.out.println("Получено сообщение: " + msg);
                if (Command.isCommand(msg)) {
                    final Command command = Command.getCommand(msg);
                    final String[] params = command.parse(msg);
                    if (command == Command.END) {
                        break;
                    }
                    if (command == Command.PRIVATE_MESSAGE) {
                        server.sendMessageToClient(this, params[0], params[1]);
                        continue;
                    }
                }
                server.broadcast(nick + ": " + msg);
            }


        } catch (IOException e) {
                e.printStackTrace();
            }


    }

    private void authenticate() {
        while (true) {
            try {
                if (!socket.isClosed()) {
                    String msg = in.readUTF(); // /auth login1 pass1

                    if (Command.isCommand(msg)) {

                        Command command = Command.getCommand(msg);
                        String[] params = command.parse(msg);

                        if (command == Command.AUTH) {

                            String login = params[0];
                            String pass = params[1];
                            String nick = authService.getNickByLoginAndPassword(login, pass);
                            if (nick != null) {
                                if (server.isNickBusy(nick)) {
                                    sendMessage(Command.ERROR, "Пользователь уже авторизован");
                                    continue;
                                }
                                isConnected = true;
                                sendMessage(Command.AUTHOK, nick);
                                this.nick = nick;
                                server.broadcast("Пользователь " + nick + " вошел в чат");
                                server.subscribe(this);
                                break;
                            } else {
                                sendMessage(Command.ERROR, "Неверные логин и пароль");
                            }
                        }
                    }
                }
            } catch (SocketException se) {
                System.out.println("Сокет закрыт");
                se.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }

    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private void closeConnection() {
        sendMessage(Command.END);
        try{
            if(in != null){
                in.close();
            }
        } catch (IOException e){
            throw new RuntimeException("Ошибка отключения", e );
        }
        try{
            if(out != null){
                out.close();
            }
        } catch (IOException e){
            throw new RuntimeException("Ошибка отключения", e );
        }
        try{
            if(socket != null){
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e){
            throw new RuntimeException("Ошибка отключения", e );
        }
    }
}
