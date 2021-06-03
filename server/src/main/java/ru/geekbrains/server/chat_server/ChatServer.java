package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer implements Server {
    private static final int PORT = 11111;
    private final Map<User, ChatServerSessionHandler> onlineUsers;
    private final ExecutorService executorService;

    public ChatServer() {
        onlineUsers = new HashMap<>();
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started.");
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new ChatServerSessionHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
        System.out.println("Chat server stopped.");
        System.exit(0);
    }

    public synchronized void subscribeUser(User user, ChatServerSessionHandler sessionHandler) {
        ChatServerSessionHandler handler = onlineUsers.get(user);
        if (handler != null) {
            unsubscribeUser(user, !handler.equals(sessionHandler));
        }
        onlineUsers.put(user, sessionHandler);
        sessionHandler.setSessionUser(user);
        System.out.println("User " + user.getUsername() + " subscribed.");
        sendOnlineUsersList();
    }

    public synchronized void unsubscribeUser(User user, boolean closeExistSession) {
        ChatServerSessionHandler handler = onlineUsers.remove(user);
        if (handler != null && closeExistSession) handler.close();
        System.out.println("User " + user.getUsername() + " unsubscribed.");
        sendOnlineUsersList();
    }

    public synchronized void unsubscribeUser(User user) {
        unsubscribeUser(user, true);
    }

    public synchronized void sendPrivateMessage(String message, User toUser) {
        onlineUsers.get(toUser).sendMessage(message);
    }

    public synchronized void sendPublicMessage(String message) {
        for (ChatServerSessionHandler sessionHandler : onlineUsers.values()) {
            sessionHandler.sendMessage(message);
        }
    }

    public synchronized void sendOnlineUsersList() {
        HashSet<User> set = new HashSet<>(onlineUsers.keySet());
        Message message = new Message(MessageType.ONLINE_USERS_LIST);
        message.setOnlineUsersSet(set);
        String jsonMessage = message.messageToJson();
        for (ChatServerSessionHandler handler : onlineUsers.values()) {
            handler.sendMessage(jsonMessage);
        }
    }

    public synchronized ChatServerSessionHandler getUsersHandler(User user) {
        return onlineUsers.get(user);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
