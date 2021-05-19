package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer implements Server {
    private static final int PORT = 11111;
    private final Map<User, ChatServerSessionHandler> onlineUsers;

    public ChatServer() {
        onlineUsers = new HashMap<>();
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
        for (ChatServerSessionHandler serverSessionHandler : onlineUsers.values()) {
            serverSessionHandler.close();
        }
        System.out.println("Chat server stopped.");
        System.exit(0);
    }

    public synchronized void subscribeUser(User user, ChatServerSessionHandler sessionHandler) {
        ChatServerSessionHandler handler = onlineUsers.get(user);
        if (sessionHandler.equals(handler)) return;
        if (handler != null) unsubscribeUser(user);
        onlineUsers.put(user, sessionHandler);
        sessionHandler.setSessionUser(user);
        System.out.println("User " + user.getUsername() + " subscribed.");
        sendOnlineUsersList();
    }

    public synchronized void unsubscribeUser(User user) {
        ChatServerSessionHandler handler = onlineUsers.remove(user);
        if (handler != null) handler.close();
        System.out.println("User " + user.getUsername() + " unsubscribed.");
        sendOnlineUsersList();
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
        Message message = new Message();
        message.setMessageType(MessageType.ONLINE_USERS_LIST);
        message.setOnlineUsersSet(set);
        message.setMessageDate(new Date());
        String jsonMessage = message.messageToJson();
        for (ChatServerSessionHandler handler : onlineUsers.values()) {
            handler.sendMessage(jsonMessage);
        }
    }
}
