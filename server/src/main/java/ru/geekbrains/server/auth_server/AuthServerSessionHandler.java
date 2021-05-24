package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.chat_common.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AuthServerSessionHandler implements SessionHandler {
    private Thread sessionThread;
    private Timer timeoutTimer;
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
        server.addSession(this);
        createTimeoutTask();
        (sessionThread = new Thread(this::readMessage)).start();
    }

    @Override
    public void close() {
        try {
            if (!socket.isClosed() || !sessionThread.isInterrupted()) {
                timeoutTimer.cancel();
                server.removeSession(this);
                socket.close();
                sessionThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTimeoutTask() {
        (timeoutTimer = new Timer(true)).schedule(new TimerTask() {
            @Override
            public void run() {
                close();
            }
        }, 120_000);
    }

    private void readMessage() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String rawMessage = inputStream.readUTF();
                Message message = Message.messageFromJson(rawMessage);
                if (message.getMessageType() == MessageType.AUTH_REQUEST) {
                    authenticateUser(message.getMessageBody());
                } else if (message.getMessageType() == MessageType.CREATE_USER_REQUEST) {
                    createUser(message.getMessageBody());
                } else if (message.getMessageType() == MessageType.CHANGE_PASSWORD_REQUEST) {
                    changeUserPassword(message.getMessageBody());
                } else if (message.getMessageType() == MessageType.CHANGE_USERNAME_REQUEST) {
                    changeUserUsername(message.getMessageBody());
                } else {
                    System.out.println("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            System.out.println("Handler closed at " + System.currentTimeMillis());
        } finally {
            close();
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

    private void createUser(String messageBody) {
        Message response = new Message();
        String[] userData = messageBody.split(":", 3); //username : login : password
        boolean isUsernameExists;
        boolean isLoginExists;
        try {
            isUsernameExists = server.isUsernameExists(userData[0]);
            isLoginExists = server.isLoginExists(userData[1]);
            if (!isUsernameExists && !isLoginExists) {
                if (server.createNewUser(userData[0], userData[1], userData[2])) {
                    response.setMessageType(MessageType.CREATE_USER_SUCCESS);
                    response.setMessageBody("User " + userData[0] + " (login: " + userData[1] + ") successfully created!");
                    sendMessage(response);
                } else {
                    throw new SQLException();
                }
            } else {
                if (isUsernameExists) {
                    response.setMessageType(MessageType.CREATE_USER_USERNAME_EXISTS);
                    response.setMessageBody("Username " + userData[0] + " is already in use");
                    sendMessage(response);
                }
                if (isLoginExists) {
                    response.setMessageType(MessageType.CREATE_USER_LOGIN_EXISTS);
                    response.setMessageBody("Login " + userData[1] + " is already in use");
                    sendMessage(response);
                }
            }
        } catch (SQLException e) {
            response.setMessageType(MessageType.CREATE_USER_FAILURE);
            response.setMessageBody("Unexpected error. Please try again later.");
            sendMessage(response);
        }
    }

    private void changeUserPassword(String messageBody) {
    }

    private void changeUserUsername(String messageBody) {
    }

    private void authenticateUser(String messageBody) {
        Message response = new Message();
        if (!server.isConnectedToChatServer()) {
            response.setMessageType(MessageType.AUTH_FAILURE);
            response.setMessageBody("Server under maintenance. Please try again later.");
        } else {
            String[] userData = messageBody.split(":", 2);
            User user = server.getUserByLoginAndPassword(userData[0], userData[1]);
            if (user == null) {
                response.setMessageType(MessageType.AUTH_FAILURE);
                response.setMessageBody("Incorrect login and/or password");
            } else {
                response.setMessageType(MessageType.AUTH_SUCCESS);
                response.setToUser(user);
            }
        }
        sendMessage(response);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthServerSessionHandler that = (AuthServerSessionHandler) o;
        return socket.equals(that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(socket);
    }
}
