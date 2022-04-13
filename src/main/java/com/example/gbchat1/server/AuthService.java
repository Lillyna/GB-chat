package com.example.gbchat1.server;

import java.io.Closeable;

public interface AuthService extends Closeable {
    String getNickByLoginAndPassword(String login, String password);

    void run();

    void start();

    void close();
}
