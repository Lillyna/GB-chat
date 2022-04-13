package com.example.gbchat1.server;

import com.example.gbchat1.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    final private Socket socket;
    final private ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    AuthServiceImpl authService;

    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer chatServer) {

       try{
           this.socket = socket;
           this.server = chatServer;
           this.in = new DataInputStream(socket.getInputStream());
           this.out = new DataOutputStream(socket.getOutputStream());
           this.authService = new AuthServiceImpl();

           new Thread(() -> {
               try{
                   authenticate();
                   readMessage();
               } finally {
                   closeConnection();
               }

           }).start();

       } catch (IOException e){
           throw new RuntimeException("Ошибка подключение к клиенту", e);
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

    private void readMessage(){
        while (true){
            String msg;
            try {
                msg = in.readUTF();
                System.out.println("Получено сообщение: " + msg);
                if (Command.isCommand(msg) && Command.getCommand(msg) == Command.END) {
                    break;
                }
                String msgToSend = this.nick + ":\n" + msg;
                if (msg.startsWith("/w") & msg.split(" ").length > 2) {
                    String[] msgArr = msg.split(" ", 3);
                    server.sendMessage(this.nick + ":\n" + msgArr[2], msgArr[1]);
                } else {
                    server.broadcast(msgToSend);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void authenticate() {
        while (true) {
            try {
                String msg = in.readUTF(); // /auth login1 pass1
                if (Command.isCommand(msg)) {
                    Command command = Command.getCommand(msg);
                    String[] params = command.parse(msg);

                if (command == Command.AUTH) {
                    final String[] s = msg.split(" ");
                    String login = params[0];
                    String pass = params[1];
                    String nick = authService.getNickByLoginAndPassword(login, pass);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(Command.ERROR, "Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage(Command.AUTHOK, nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        server.subscribe(this);
                        break;
                    }
                }}
            } catch (IOException e) {
                e.printStackTrace();
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
