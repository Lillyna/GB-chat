package com.example.gbchat1.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
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
                if("/end".equals(msg)){
                    break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void authenticate() {
        while (true){
            try{
                String msg = in.readUTF(); // /auth login1 pass1
                if(msg.startsWith("/auth")){
                    final String[] s = msg.split(" ");
                    String login = s[1];
                    String pass = s[2];
                    String nick = authService.getNickByLoginAndPassword(login, pass);
                    if(nick!=null){
                        if(server.isNickBusy(nick)){
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok "+ nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " вошел в чат");
                        server.subscribe(this);
                        break;
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");
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
