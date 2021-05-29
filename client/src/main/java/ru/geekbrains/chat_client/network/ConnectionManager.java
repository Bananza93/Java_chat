package ru.geekbrains.chat_client.network;

import ru.geekbrains.chat_client.ui.Client;
import ru.geekbrains.chat_common.User;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class ConnectionManager {
    private static final String AUTH_SERVER_HOST = "localhost";
    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;
    private static final Object mon1 = new Object();
    private static final Object mon2 = new Object();
    private static ClientSessionHandler currentAuthServerSession;
    private static ClientSessionHandler currentChatServerSession;
    private static User currentUser;

    private ConnectionManager() {
    }

    public static ClientSessionHandler getCurrentAuthServerSession() throws IOException {
        synchronized (mon1) {
            if (!isConnectedToAuthServer()) {
                connectToAuthServer();
            }
        }
        return currentAuthServerSession;
    }

    public static ClientSessionHandler getCurrentChatServerSession() throws IOException {
        synchronized (mon2) {
            if (!isConnectedToChatServer()) {
                connectToChatServer();
            }
        }
        return currentChatServerSession;
    }

    private static boolean isConnectedToAuthServer() {
        return currentAuthServerSession != null && !currentAuthServerSession.isClosed();
    }

    private static boolean isConnectedToChatServer() {
        return currentChatServerSession != null && !currentChatServerSession.isClosed();
    }

    private static void connectToAuthServer() throws IOException {
        currentAuthServerSession = new ClientSessionHandler(new Socket(AUTH_SERVER_HOST, AUTH_SERVER_PORT));
        currentAuthServerSession.handle();
        System.out.println("Connected to AUTH_SERVER at " + new Date());
    }

    private static void connectToChatServer() throws IOException {
        currentChatServerSession = new ClientSessionHandler(new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT));
        currentChatServerSession.handle();
        System.out.println("Connected to CHAT_SERVER at " + new Date());
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        ConnectionManager.currentUser = user;
        Client.chatStage.setTitle(Client.chatStage.getTitle() + " [User: " + currentUser.getUsername() + "]");
    }
}
