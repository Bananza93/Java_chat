package ru.geekbrains.chat_client.network;

import ru.geekbrains.chat_client.ui.ClientController;
import ru.geekbrains.chat_common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSessionHandler {
    private MessageProcessor messageProcessor;
    private ClientController controller;
    private String serverType;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Thread sessionThread;
    private User sessionOwner;

    public ClientSessionHandler(Socket socket, String serverType, MessageProcessor messageProcessor, ClientController controller) {
        try {
            this.messageProcessor = messageProcessor;
            this.serverType = serverType;
            this.controller = controller;
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created (" + serverType + "_SERVER).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ClientSessionHandler(Socket socket, String serverType, MessageProcessor messageProcessor, ClientController controller, User sessionOwner) {
        this(socket, serverType, messageProcessor, controller);
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
