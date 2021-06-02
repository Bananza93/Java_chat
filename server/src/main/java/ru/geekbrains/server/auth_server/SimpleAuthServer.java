package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class SimpleAuthServer implements AuthServer {

    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;
    private final Object mon1 = new Object();

    private Socket chatServerSocket;
    private DataInputStream fromChatServer;
    private DataOutputStream toChatServer;
    private boolean isConnectedToChatServer;

    private final List<User> tempUserDatabase;
    private final Set<AuthServerSessionHandler> activeSessions;


    public SimpleAuthServer() {
        tempUserDatabase = List.of(new User("user1", "log1", "pass"),
                new User("user2", "log2", "pass"),
                new User("user3", "log3", "pass")
        );
        activeSessions = new HashSet<>();
        isConnectedToChatServer = false;
    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(AUTH_SERVER_PORT)) {
            System.out.println("Auth server started.");
            connectionWithChatServerThread();
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
        System.out.println("Auth server stopped.");
        System.exit(0);
    }

    public synchronized void addSession(AuthServerSessionHandler session) {
        System.out.println("Session added");
        activeSessions.add(session);
    }

    public synchronized void removeSession(AuthServerSessionHandler session) {
        System.out.println("Session removed");
        activeSessions.remove(session);
    }

    private void connectionWithChatServerThread() {
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
        for (User user : tempUserDatabase) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean isConnectedToChatServer() {
        return isConnectedToChatServer;
    }

    @Override
    public ExecutorService getExecutorService() {
        return null;
    }
}
