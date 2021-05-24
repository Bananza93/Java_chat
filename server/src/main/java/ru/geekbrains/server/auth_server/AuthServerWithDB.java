package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class AuthServerWithDB implements AuthServer {

    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;

    private Socket chatServerSocket;
    private DataInputStream fromChatServer;
    private DataOutputStream toChatServer;
    private boolean isConnectedToChatServer;
    private final Object mon1 = new Object();

    private Connection dbConnection;
    private PreparedStatement statement;
    private final String selectUser = "SELECT * FROM users WHERE login = ? AND password = ?;";
    private final String selectCheckUsername = "SELECT COUNT(*) FROM users WHERE username = ?;";
    private final String selectCheckLogin = "SELECT COUNT(*) FROM users WHERE login = ?;";
    private final String insertNewUser = "INSERT INTO users(username, login, password) VALUES (?, ?, ?);";
    private boolean isConnectedTDB;
    private final Object mon2 = new Object();

    private final Set<AuthServerSessionHandler> activeSessions;


    public AuthServerWithDB() {
        activeSessions = new HashSet<>();
        isConnectedToChatServer = false;
        isConnectedTDB = false;

    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(AUTH_SERVER_PORT)) {
            System.out.println("Auth server started.");
            connectionWithChatServerThread();
            connectToDB();
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = authServerSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new AuthServerSessionHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        for (AuthServerSessionHandler activeSession : activeSessions) {
            activeSession.close();
        }
        try {
            chatServerSocket.close();
            dbConnection.close();
        } catch (IOException | SQLException e) {/*do nothing*/}
        System.out.println("Auth server stopped.");
        System.exit(0);
    }

    public synchronized void addSession(AuthServerSessionHandler session) {
        activeSessions.add(session);
        System.out.println("Session added");
    }

    public synchronized void removeSession(AuthServerSessionHandler session) {
        activeSessions.remove(session);
        System.out.println("Session removed");
    }

    private void connectionWithChatServerThread() {
        Thread t = new Thread(this::connectToChatServer);
        t.setDaemon(true);
        t.start();
    }

    private void connectToDB() {
        while (!isConnectedTDB) {
            synchronized (mon2) {
                try {
                    System.out.println("Trying to connect with database...");
                    dbConnection = DriverManager.getConnection("jdbc:sqlite:server/src/main/resources/java_chat.db");
                    isConnectedTDB = true;
                    System.out.println("Successfully connected to database.");
                } catch (SQLException e) {
                    try {
                        System.out.println("Database not response.");
                        mon2.wait(3000);
                    } catch (InterruptedException interruptedException) {/*do nothing*/}
                }
            }
        }
    }

    private void connectToChatServer() {
        while (!isConnectedToChatServer) {
            synchronized (mon1) {
                try {
                    System.out.println("Trying to connect with chat server...");
                    chatServerSocket = new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT);
                    fromChatServer = new DataInputStream(chatServerSocket.getInputStream());
                    toChatServer = new DataOutputStream(chatServerSocket.getOutputStream());
                    isConnectedToChatServer = true;
                    System.out.println("Successfully connected.");
                } catch (IOException e) {
                    try {
                        System.out.println("Chat server not response.");
                        mon1.wait(3000);
                    } catch (InterruptedException interruptedException) {/*do nothing*/}
                }
            }
        }
        checkConnectionWithChatServer();
    }

    private void checkConnectionWithChatServer() {
        while (isConnectedToChatServer) {
            synchronized (mon1) {
                try {
                    fromChatServer.read();
                    mon1.wait(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    isConnectedToChatServer = false;
                }
            }
        }
        System.out.println("Lost connection with chat server");
        connectToChatServer();
    }

    @Override
    public synchronized User getUserByLoginAndPassword(String login, String password) {
        try {
            statement = dbConnection.prepareStatement(selectUser);
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized boolean isUsernameExists(String username) throws SQLException {
        statement = dbConnection.prepareStatement(selectCheckUsername);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1) != 0;
    }

    @Override
    public synchronized boolean isLoginExists(String login) throws SQLException {
        statement = dbConnection.prepareStatement(selectCheckLogin);
        statement.setString(1, login);
        ResultSet rs = statement.executeQuery();
        rs.next();
        return rs.getInt(1) != 0;
    }

    @Override
    public synchronized boolean createNewUser(String username, String login, String password) throws SQLException {
        statement = dbConnection.prepareStatement(insertNewUser);
        statement.setString(1, username);
        statement.setString(2, login);
        statement.setString(3, password);
        return statement.executeUpdate() == 1;
    }

    @Override
    public synchronized boolean isConnectedToChatServer() {
        return isConnectedToChatServer;
    }
}
