package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public synchronized boolean isUserOnline(User user) {
        return onlineUsers.containsKey(user);
    }

    public void sendPrivateMessage() {
    }

    public synchronized void sendPublicMessage(String message) {
        for (ChatServerSessionHandler sessionHandler : onlineUsers.values()) {
            System.out.println("Message sent to " + sessionHandler.getSessionUser());
            sessionHandler.sendMessage(message);
        }
    }

    public synchronized void sendOnlineUsersList() {
        ArrayList<String> list = onlineUsers.keySet().stream().map(User::getUsername).collect(Collectors.toCollection(ArrayList::new));
        Message message = new Message();
        message.setMessageType(MessageType.ONLINE_USERS_LIST);
        message.setMessageUtilList(list);
        message.setMessageDate(new Date());
        String jsonMessage = message.messageToJson();
        for (ChatServerSessionHandler handler : onlineUsers.values()) {
            handler.sendMessage(jsonMessage);
        }
    }
}
