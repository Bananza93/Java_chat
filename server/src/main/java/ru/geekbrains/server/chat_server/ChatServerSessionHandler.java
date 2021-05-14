package ru.geekbrains.server.chat_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Objects;

public class ChatServerSessionHandler implements SessionHandler {
    private Socket socket;
    private ChatServer server;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;


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
        new Thread(this::readMessage).start();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String rawMessage = inputStream.readUTF();
                Message message = Message.messageFromJson(rawMessage);
                switch (message.getMessageType()) {
                    case PUBLIC -> sendPublicMessage();
                    case PRIVATE -> sendPrivateMessage();
                    case SUBSCRIBE_REQUEST -> server.subscribeUser(message.getFromUser(), this);
                    case UNSUBSCRIBE_REQUEST -> server.unsubscribeUser(message.getFromUser());
                    default -> System.out.println("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private void sendPrivateMessage() {
    }

    private void sendPublicMessage() {
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
        return Objects.hash(socket);
    }
}
