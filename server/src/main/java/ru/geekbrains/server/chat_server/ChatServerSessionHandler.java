package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.chat_common.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ChatServerSessionHandler implements SessionHandler {
    private Thread sessionThread;
    private Socket socket;
    private ChatServer server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private User sessionUser;


    public ChatServerSessionHandler(Socket socket, ChatServer chatServer) {
        try {
            this.socket = socket;
            this.server = chatServer;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle() {
        (sessionThread = new Thread(this::readMessage)).start();
    }

    @Override
    public void close() {
        try {
            System.out.println("Close session invoked");
            socket.close();
            sessionThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String rawMessage = inputStream.readUTF();
                System.out.println("Message received: " + rawMessage);
                Message message = Message.messageFromJson(rawMessage);
                switch (message.getMessageType()) {
                    case PUBLIC -> server.sendPublicMessage(rawMessage);
                    case PRIVATE -> server.sendPrivateMessage(rawMessage, message.getToUser());
                    case SUBSCRIBE_REQUEST -> server.subscribeUser(message.getFromUser(), this);
                    default -> System.out.println("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            System.out.println("Handler closed at " + System.currentTimeMillis());
        } finally {
            server.unsubscribeUser(sessionUser);
        }
    }

    public void sendMessage(String jsonMessage) {
        try {
            outputStream.writeUTF(jsonMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getSessionUser() {
        return sessionUser;
    }

    public void setSessionUser(User sessionUser) {
        this.sessionUser = sessionUser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatServerSessionHandler that = (ChatServerSessionHandler) o;
        return socket.equals(that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(socket);
    }
}
