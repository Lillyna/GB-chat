package com.example.gbchat1.server;

import java.util.ArrayList;

public class AuthServiceImpl implements AuthService {
    private ArrayList<UserData> users;

    private class UserData {
        final private String login;
        final private String password;
        final private String nick;

        public UserData(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    public AuthServiceImpl() {
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("login" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nick;
            }
        }
        return null;
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");

    }

    @Override
    public void run() {
        System.out.println("AuthService run");
    }
}
