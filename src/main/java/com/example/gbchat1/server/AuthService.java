package com.example.gbchat1.server;

import java.io.Closeable;
import java.io.IOException;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

    void updateUsers();

    void run();

    void close() throws IOException;
}
