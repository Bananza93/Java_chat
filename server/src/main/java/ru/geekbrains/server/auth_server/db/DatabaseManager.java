package ru.geekbrains.server.auth_server.db;

import java.sql.*;

public class DatabaseManager {

    private Connection dbConnection;
    private PreparedStatement statement;

    public void connectToDB() throws SQLException {
        System.out.println("Trying to connect with database...");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:server/src/main/java/ru/geekbrains/server/auth_server/db/java_chat.db");
        System.out.println("Successfully connected to database.");
    }

    public void closeConnection() {
        try {
            dbConnection.close();
        } catch (SQLException e) {/*do nothing*/}
    }

    public synchronized ResultSet selectUserByLoginAndPassword(String login, String password) throws SQLException {
        String selectUser = "SELECT * FROM users WHERE login = ? AND password = ?;";
        statement = dbConnection.prepareStatement(selectUser);
        statement.setString(1, login);
        statement.setString(2, password);
        return statement.executeQuery();
    }

    public synchronized boolean isUsernameExists(String username) throws SQLException {
        String selectCheckUsername = "SELECT COUNT(*) FROM users WHERE username = ?;";
        statement = dbConnection.prepareStatement(selectCheckUsername);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1) != 0;
    }

    public synchronized boolean isLoginExists(String login) throws SQLException {
        String selectCheckLogin = "SELECT COUNT(*) FROM users WHERE login = ?;";
        statement = dbConnection.prepareStatement(selectCheckLogin);
        statement.setString(1, login);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1) != 0;
    }

    public synchronized boolean createNewUser(String username, String login, String password) throws SQLException {
        String insertNewUser = "INSERT INTO users(username, login, password) VALUES (?, ?, ?);";
        statement = dbConnection.prepareStatement(insertNewUser);
        statement.setString(1, username);
        statement.setString(2, login);
        statement.setString(3, password);
        return statement.executeUpdate() == 1;
    }

}
