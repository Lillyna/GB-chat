package com.example.gbchat1.server;

import com.example.gbchat1.db.DbConnection;
import com.example.gbchat1.db.UserData;

import java.sql.*;
import java.util.ArrayList;

public class AuthServiceImpl implements AuthService {
    private ArrayList<UserData> users = new ArrayList<>();

    public AuthServiceImpl() {
        DbConnection dbConnection = new DbConnection(users);
    }

    @Override
    public void updateUsers(){
        users.clear();
        DbConnection dbConnection = new DbConnection(users);
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user.getNick();
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
