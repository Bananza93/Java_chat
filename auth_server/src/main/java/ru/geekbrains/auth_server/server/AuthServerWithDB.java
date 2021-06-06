package ru.geekbrains.auth_server.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.auth_server.db.DatabaseManager;
import ru.geekbrains.auth_server.utils.AuthServer;
import ru.geekbrains.chat_common.User;

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
    private static final Logger LOGGER = LogManager.getLogger(AuthServerWithDB.class);
    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;
    private final Object mon1 = new Object();

    private Socket chatServerSocket;
    private DataInputStream fromChatServer;
    private boolean isConnectedToChatServer;
    private final DatabaseManager dbManager;
    private final ExecutorService executorService;
    private final Set<AuthServerSessionHandler> activeSessions;

    public AuthServerWithDB() {
        activeSessions = new HashSet<>();
        isConnectedToChatServer = false;
        dbManager = new DatabaseManager();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(AUTH_SERVER_PORT)) {
            LOGGER.info("Auth server started");
            chatServerConnectionThread();
            dbManager.connectToDB();
            while (true) {
                Socket socket = authServerSocket.accept();
                LOGGER.info("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new AuthServerSessionHandler(socket, this, dbManager).handle();
            }
        } catch (IOException | SQLException e) {
            LOGGER.error("EXCEPTION!", e);
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
        LOGGER.warn("Auth server stopped.");
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
                    LOGGER.info("Trying to connect with chat server...");
                    chatServerSocket = new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT);
                    fromChatServer = new DataInputStream(chatServerSocket.getInputStream());
                    isConnectedToChatServer = true;
                    LOGGER.info("Successfully connected to chat server.");
                } catch (IOException e) {
                    try {
                        LOGGER.warn("Chat server not response.");
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
                    LOGGER.error("EXCEPTION!", e);
                } catch (IOException e) {
                    isConnectedToChatServer = false;
                }
            }
        }
        LOGGER.warn("Lost connection with chat server");
        connectToChatServer();
    }

    @Override
    public synchronized boolean isConnectedToChatServer() {
        return isConnectedToChatServer;
    }

    public synchronized void addSession(AuthServerSessionHandler session) {
        activeSessions.add(session);
        LOGGER.info("Session added");
    }

    public synchronized void removeSession(AuthServerSessionHandler session) {
        activeSessions.remove(session);
        LOGGER.info("Session removed");
    }

    @Override
    public synchronized User getUserByLoginAndPassword(String login, String password) {
        try {
            ResultSet rs = dbManager.selectUserByLoginAndPassword(login, password);
            if (rs.next()) return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION!", e);
        }
        return null;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
