package com.example.gbchat1.server;

import com.example.gbchat1.db.DbConnection;
import com.example.gbchat1.db.UserData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class AuthServiceImpl implements AuthService {
    private ArrayList<UserData> users = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl() {
        DbConnection dbConnection = new DbConnection(users);
    }

    @Override
    public void updateUsers(){
        log.debug("Обновление пользователей: {}", ()-> users);
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
        log.info("Сервис аутентификации остановлен");

    }

    @Override
    public void run() {
        log.info("Сервис аутентификации запущен");
    }
}
