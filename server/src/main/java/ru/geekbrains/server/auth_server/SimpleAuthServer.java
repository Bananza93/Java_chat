package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.chat_server.ChatServerSessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class SimpleAuthServer implements AuthServer {

    private static final int PORT = 22222;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private List<User> users;
    private Socket chatServerSocket;
    private boolean isConnectedToChatServer = false;

    public SimpleAuthServer() {
        users = List.of(new User("user1", "log1", "pass"),
                new User("user2", "log2", "pass"),
                new User("user3", "log3", "pass")
        );
    }

    @Override
    public void start() {
        try (ServerSocket authServerSocket = new ServerSocket(PORT)) {
            System.out.println("Auth server started");
            connectToChatServer();
            while (true) {

                socket = authServerSocket.accept();
                String rawMessage = in.readUTF();
                //System.out.println("FROM SERVER: " + rawMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void authorizeUser(String jsonMessage, ChatServerSessionHandler userSession) {

    }

    private void connectToChatServer() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    chatServerSocket = new Socket("localhost", 11111);
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
}
