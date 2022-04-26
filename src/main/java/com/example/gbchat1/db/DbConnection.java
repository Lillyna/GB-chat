package com.example.gbchat1.db;

import java.sql.*;
import java.util.ArrayList;

public class DbConnection {

    ArrayList<UserData> users;
    private Connection connection;

    public DbConnection(ArrayList<UserData> users) {
        try {
            connect();
            selectAllUsers(connection, users);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public DbConnection(String oldNick, String newNick) {
        try {
            connect();
            updateNick(connection, oldNick, newNick);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }


    public void connect() throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:chatdb.db");
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void selectAllUsers(Connection connection, ArrayList<UserData> users) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT * FROM users")) {
            final ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String login = rs.getString("login");
                String password = rs.getString("password");
                String nick = rs.getString("nick");
                users.add(new UserData(login, password, nick));
            }
        }
    }


    public void updateNick(Connection connection, String oldNick, String newNick) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?")) {
            statement.setString(1, newNick);
            statement.setString(2, oldNick);
            statement.executeUpdate();
        }
    }
}
