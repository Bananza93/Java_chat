package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.auth_server.db.DatabaseManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthServerWithDB implements AuthServer {

    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;

    private Socket chatServerSocket;
    private DataInputStream fromChatServer;
    private boolean isConnectedToChatServer;
    private final Object mon1 = new Object();

    private final DatabaseManager dbManager;

    private final Set<AuthServerSessionHandler> activeSessions;
    private final ExecutorService executorService;

    public AuthServerWithDB() {
        activeSessions = new HashSet<>();
        isConnectedToChatServer = false;
        dbManager = new DatabaseManager();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(AUTH_SERVER_PORT)) {
            System.out.println("Auth server started.");
            chatServerConnectionThread();
            dbManager.connectToDB();
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = authServerSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new AuthServerSessionHandler(socket, this, dbManager).handle();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
        try {
            chatServerSocket.close();
            dbManager.closeConnection();
        } catch (IOException e) {/*do nothing*/}
        System.out.println("Auth server stopped.");
        System.exit(0);
    }

    private void chatServerConnectionThread() {
        Thread t = new Thread(this::connectToChatServer);
        t.setDaemon(true);
        t.start();
    }

    private void connectToChatServer() {
        while (!isConnectedToChatServer) {
            synchronized (mon1) {
                try {
                    System.out.println("Trying to connect with chat server...");
                    chatServerSocket = new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT);
                    fromChatServer = new DataInputStream(chatServerSocket.getInputStream());
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
    public synchronized boolean isConnectedToChatServer() {
        return isConnectedToChatServer;
    }

    public synchronized void addSession(AuthServerSessionHandler session) {
        activeSessions.add(session);
        System.out.println("Session added");
    }

    public synchronized void removeSession(AuthServerSessionHandler session) {
        activeSessions.remove(session);
        System.out.println("Session removed");
    }

    @Override
    public synchronized User getUserByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = dbManager.selectUserByLoginAndPassword(login, password);
            if (rs.next()) return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
