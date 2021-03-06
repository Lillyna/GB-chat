package com.example.gbchat1.server;

import com.example.gbchat1.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    final private Socket socket;
    final private ChatServer server;
    private String nick;
    private final DataInputStream in;
    private final DataOutputStream out;
    private boolean isConnected = false;
    AuthService authService;
    ExecutorService executorService;
    private static final Logger log = LogManager.getLogger(ClientHandler.class);

    public String getNick() {
        return nick;
    }

    public ClientHandler(Socket socket, ChatServer chatServer, AuthService authService, ExecutorService executorService) {

        try {
            this.nick = "";
            this.socket = socket;
            this.server = chatServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = authService;
            this.executorService = executorService;
            executorService.submit(() -> {

                while (true) {
                    try {
                        authenticate();
                        readMessage();
                    } finally {
                        closeConnection();
                    }
                }
            });
            executorService.submit(() ->
            {

                try {
                    Thread.sleep(120_000);
                    if (!isConnected) {
                        closeConnection();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
        } catch (IOException e) {
            log.error("???????????? ?????????????????????? ?? ?????????????? {}",()->e);
            throw new RuntimeException("???????????? ?????????????????????? ?? ??????????????", e);
        }

    }

    public void sendMessage(String message) {
        try {
            log.trace("?????????????????? ?????????????????? {}", ()-> message);
            out.writeUTF(message);
            if (!Command.isCommand(message)) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("history_" +
                        nick + ".txt", true))) {
                    writer.append("\n" + message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        try {
            while (true) {
                final String msg = in.readUTF();
                log.trace("???????????????? ??????????????????: {}", ()-> msg);
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
                    if (command == Command.CHANGE_NICK) {
                        log.info("NICK: {}", ()->  this.getNick());
                        this.nick = server.changeNick(this.getNick(), params[0]);
                        authService.updateUsers();

                        continue;
                    }
                }
                server.broadcast(nick + ": " + msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
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
                                    sendMessage(Command.ERROR, "???????????????????????? ?????? ??????????????????????");
                                    continue;
                                }
                                isConnected = true;
                                sendMessage(Command.AUTHOK, nick);
                                this.nick = nick;
                                server.broadcast("???????????????????????? " + nick + " ?????????? ?? ??????");
                                server.subscribe(this);
                                break;
                            } else {
                                sendMessage(Command.ERROR, "???????????????? ?????????? ?? ????????????");
                            }
                        }
                    }
                }
            } catch (SocketException se) {
                log.warn("?????????? ????????????");
                System.out.println("?????????? ????????????");
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
        executorService.shutdown();
        sendMessage(Command.END);
        try{
            if(in != null){
                in.close();
            }
        } catch (IOException e){
            log.error("???????????? ???????????????????? {}", () -> e);
            throw new RuntimeException("???????????? ????????????????????", e );
        }
        try{
            if(out != null){
                out.close();
            }
        } catch (IOException e){
            log.error("???????????? ???????????????????? {}", () -> e);
            throw new RuntimeException("???????????? ????????????????????", e );
        }
        try{
            if(socket != null){
                server.unsubscribe(this);
                socket.close();
            }
        } catch (IOException e){
            log.error("???????????? ???????????????????? {}", () -> e);
            throw new RuntimeException("???????????? ????????????????????", e );
        }
    }
}
