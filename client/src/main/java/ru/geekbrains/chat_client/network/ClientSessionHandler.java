package ru.geekbrains.chat_client.network;

import ru.geekbrains.chat_common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSessionHandler {
    private MessageProcessor messageProcessor;
    private String serverType;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Thread sessionThread;
    private User sessionOwner;

    public ClientSessionHandler(Socket socket, String serverType, MessageProcessor messageProcessor) {
        try {
            this.messageProcessor = messageProcessor;
            this.serverType = serverType;
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created (" + serverType + "_SERVER).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientSessionHandler(Socket socket, String serverType, MessageProcessor messageProcessor, User sessionOwner) {
        this(socket, serverType, messageProcessor);
        this.sessionOwner = sessionOwner;
    }

    public void handle() {
        messageProcessor.setCurrentSession(this);
        (sessionThread = new Thread(this::readMessage)).start();
    }

    public void close() {
        try {
            socket.close();
            sessionThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        try {
            while(!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String message = inputStream.readUTF();
                messageProcessor.incomingMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Socket closed by server (" + serverType + "_SERVER) at " + System.currentTimeMillis());
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerType() {
        return serverType;
    }

    public User getSessionOwner() {
        return sessionOwner;
    }
}
