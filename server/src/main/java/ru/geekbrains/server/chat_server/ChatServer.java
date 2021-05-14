package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static final int PORT = 11111;
    private Map<User, ChatServerSessionHandler> onlineUsers;

    public ChatServer() {
        onlineUsers = new HashMap<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started");
            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new ChatServerSessionHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribeUser(User user, ChatServerSessionHandler sessionHandler) {
        ChatServerSessionHandler handler = onlineUsers.get(user);
        if (sessionHandler.equals(handler)) return;
        if (handler != null) unsubscribeUser(user);
        onlineUsers.put(user, sessionHandler);
    }

    public synchronized void unsubscribeUser(User user) {
        ChatServerSessionHandler handler = onlineUsers.remove(user);
        if (handler != null) handler.close();
    }

    public synchronized boolean isUserOnline(User user) {
        return onlineUsers.containsKey(user);
    }
}
