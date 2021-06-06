package ru.geekbrains.auth_server.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.auth_server.server.AuthServerWithDB;

import java.sql.*;

public class DatabaseManager {
    private static final Logger LOGGER = LogManager.getLogger(AuthServerWithDB.class);
    private Connection dbConnection;
    private PreparedStatement statement;

    public void connectToDB() throws SQLException {
        LOGGER.info("Trying to connect with database...");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:auth_server/src/main/java/ru/geekbrains/auth_server/db/java_chat.db");
        LOGGER.info("Successfully connected to database");
    }

    public void closeConnection() {
        try {
            dbConnection.close();
        } catch (SQLException e) {/*do nothing*/}
    }

    public synchronized ResultSet selectUserByLoginAndPassword(String login, String password) throws SQLException {
        String selectUserQuery = "SELECT * FROM users WHERE login = ? AND password = ?;";
        statement = dbConnection.prepareStatement(selectUserQuery);
        statement.setString(1, login);
        statement.setString(2, password);
        return statement.executeQuery();
    }

    public synchronized boolean isUsernameExists(String username) throws SQLException {
        String selectCheckUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ?;";
        statement = dbConnection.prepareStatement(selectCheckUsernameQuery);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        return rs.next() && rs.getInt(1) == 1;
    }

    public synchronized boolean isLoginExists(String login) throws SQLException {
        String selectCheckLoginQuery = "SELECT COUNT(*) FROM users WHERE login = ?;";
        statement = dbConnection.prepareStatement(selectCheckLoginQuery);
        statement.setString(1, login);
        ResultSet rs = statement.executeQuery();
        return rs.next() && rs.getInt(1) == 1;
    }

    public synchronized boolean insertNewUser(String username, String login, String password) throws SQLException {
        String insertNewUserQuery = "INSERT INTO users(username, login, password) VALUES (?, ?, ?);";
        statement = dbConnection.prepareStatement(insertNewUserQuery);
        statement.setString(1, username);
        statement.setString(2, login);
        statement.setString(3, password);
        return statement.executeUpdate() == 1;
    }

    public synchronized boolean updateUserPassword(String login, String newPassword) throws SQLException {
        String updateUserPasswordQuery = "UPDATE users SET password = ? WHERE login = ?;";
        statement = dbConnection.prepareStatement(updateUserPasswordQuery);
        statement.setString(1, newPassword);
        statement.setString(2, login);
        return statement.executeUpdate() == 1;
    }

    public synchronized boolean updateUserUsername(String login, String newUsername) throws SQLException {
        String updateUserPasswordQuery = "UPDATE users SET username = ? WHERE login = ?;";
        statement = dbConnection.prepareStatement(updateUserPasswordQuery);
        statement.setString(1, newUsername);
        statement.setString(2, login);
        return statement.executeUpdate() == 1;
    }
}
