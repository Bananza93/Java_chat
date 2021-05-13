package ru.geekbrains.chat_server;

import ru.geekbrains.chat_common.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SessionHandler {
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;


    public SessionHandler(Socket socket) {
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    String rawMessage = inputStream.readUTF();
                    Message message = Message.messageFromJson(rawMessage);
                    switch (message.getMessageType()) {
                        case PUBLIC -> sendPublicMessage();
                        case PRIVATE -> sendPrivateMessage();
                        case AUTH_REQUEST -> authenticateUser();
                        default -> System.out.println("Incorrect message type: " + message.getMessageType());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String jsonMessage) {
        try {
            outputStream.writeUTF(jsonMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticateUser() {
    }

    private void sendPrivateMessage() {
    }

    private void sendPublicMessage() {
    }
}
