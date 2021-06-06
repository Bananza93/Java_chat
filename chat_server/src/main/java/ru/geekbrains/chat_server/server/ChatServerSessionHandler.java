package ru.geekbrains.chat_server.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.chat_common.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ChatServerSessionHandler implements SessionHandler {
    private static final Logger LOGGER = LogManager.getLogger(ChatServer.class);
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
            LOGGER.info("Handler created (IP: " + socket.getInetAddress().getHostAddress() + ")");
        } catch (IOException e) {
            LOGGER.error("EXCEPTION!", e);
        }
    }

    @Override
    public void handle() {
        server.getExecutorService().execute(this::readMessage);
    }

    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {/*do nothing*/}
    }

    private void readMessage() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String rawMessage = inputStream.readUTF();
                LOGGER.debug("Message received: " + rawMessage);
                Message message = Message.messageFromJson(rawMessage);
                switch (message.getMessageType()) {
                    case PUBLIC -> server.sendPublicMessage(rawMessage);
                    case PRIVATE -> server.sendPrivateMessage(rawMessage, message.getToUser());
                    case SUBSCRIBE_REQUEST -> server.subscribeUser(message.getFromUser(), this);
                    default -> LOGGER.warn("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            LOGGER.info("Handler closed (IP: " + socket.getInetAddress().getHostAddress() + ")");
        } finally {
            if (sessionUser != null && server.getUsersHandler(sessionUser).equals(this)) server.unsubscribeUser(sessionUser);
        }
    }

    public void sendMessage(String jsonMessage) {
        try {
            LOGGER.debug("Message send: " + jsonMessage);
            outputStream.writeUTF(jsonMessage);
        } catch (IOException e) {
            LOGGER.error("EXCEPTION!", e);
        }
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
