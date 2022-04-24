package com.example.gbchat1.db;

public class UserData {
    final private String login;
    final private String password;
    final private String nick;

    public UserData(String login, String password, String nick) {
        this.login = login;
        this.password = password;
        this.nick = nick;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNick() {
        return nick;
    }
}
