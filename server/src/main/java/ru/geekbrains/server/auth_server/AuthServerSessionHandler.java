package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class AuthServerSessionHandler implements SessionHandler {

    private Socket socket;
    private AuthServer server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public AuthServerSessionHandler(Socket socket, AuthServer authServer) {
        try {
            this.socket = socket;
            this.server = authServer;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle() {
        new Thread(this::readMessage).start();
    }

    private void readMessage() {
        try {
            while(!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String rawMessage = inputStream.readUTF();
                Message message = Message.messageFromJson(rawMessage);
                switch (message.getMessageType()) {
                    case AUTH_REQUEST -> authenticateUser(message.getMessageBody());
                    default -> System.out.println("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void sendMessage(Message message) {
        try {
            message.setMessageDate(new Date());
            outputStream.writeUTF(message.messageToJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void authenticateUser(String messageBody) {
        int splitterIndex = messageBody.indexOf(':');
        User user = server.getUserByLoginAndPassword(messageBody.substring(0, splitterIndex), messageBody.substring(splitterIndex + 1));
        Message response = new Message();
        if (user == null) {
            response.setMessageType(MessageType.AUTH_FAILURE);
            response.setMessageBody("Incorrect login and/or password");
        } else {
            response.setMessageType(MessageType.AUTH_SUCCESS);
            response.setToUser(user);
        }
        sendMessage(response);
    }

}
