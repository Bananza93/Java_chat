package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class SimpleAuthServer implements AuthServer {

    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;

    private Socket chatServerSocket;
    private DataOutputStream toChatServer;
    private boolean isConnectedToChatServer;

    private List<User> tempUserDatabase;


    public SimpleAuthServer() {
        tempUserDatabase = List.of(new User("user1", "log1", "pass"),
                new User("user2", "log2", "pass"),
                new User("user3", "log3", "pass")
        );
        isConnectedToChatServer = false;
    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(AUTH_SERVER_PORT)) {
            System.out.println("Auth server started");
            connectToChatServer();
            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = authServerSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new AuthServerSessionHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }



    private void connectToChatServer() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    chatServerSocket = new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT);
                    toChatServer = new DataOutputStream(chatServerSocket.getOutputStream());
                    isConnectedToChatServer = true;
                    checkConnectionWithChatServer();
                    break;
                } catch (IOException e) {
                    try {
                        Thread.currentThread().wait(3000);
                    } catch (InterruptedException interruptedException) {/*do nothing*/}
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void checkConnectionWithChatServer() {
        Thread t = new Thread(() -> {
            while (!chatServerSocket.isClosed()) {
                try {
                    Thread.currentThread().wait(3000);
                } catch (InterruptedException e) {/*do nothing*/}
            }
            connectToChatServer();
        });
        t.setDaemon(true);
        t.start();
    }

    @Override
    public User getUserByLoginAndPassword(String login, String password) {
        for (User user : tempUserDatabase) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    public Socket getChatServerSocket() {
        return chatServerSocket;
    }

    public DataOutputStream getToChatServer() {
        return toChatServer;
    }

    public boolean isConnectedToChatServer() {
        return isConnectedToChatServer;
    }
}
