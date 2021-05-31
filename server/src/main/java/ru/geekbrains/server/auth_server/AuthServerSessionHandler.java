package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;
import ru.geekbrains.chat_common.SessionHandler;
import ru.geekbrains.server.auth_server.db.DatabaseManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
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
    private DatabaseManager dbManager;
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

    public AuthServerSessionHandler(Socket socket, AuthServer authServer, DatabaseManager dbManager) {
        this(socket, authServer);
        this.dbManager = dbManager;
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
        } catch (IOException e) {/*do nothing*/}
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
                System.out.println("Message received: " + rawMessage);
                Message message = Message.messageFromJson(rawMessage);
                switch (message.getMessageType()) {
                    case AUTH_REQUEST -> authenticateUser(message.getMessageBody());
                    case CREATE_USER_REQUEST -> createUser(message.getMessageBody());
                    case CHANGE_PASSWORD_LOGIN_CHECK -> changeUserPasswordCheckLogin(message.getMessageBody());
                    case CHANGE_PASSWORD_REQUEST -> changeUserPassword(message.getMessageBody());
                    case CHANGE_USERNAME_REQUEST -> changeUserUsername(message.getMessageBody());
                    default -> System.out.println("Incorrect message type: " + message.getMessageType());
                }
            }
        } catch (IOException e) {
            System.out.println("Handler closed at " + System.currentTimeMillis());
        } finally {
            close();
        }
    }

    private void changeUserPasswordCheckLogin(String login) {
        Message response = new Message();
        try {
            if (dbManager.isLoginExists(login)) {
                response.setMessageType(MessageType.CHANGE_PASSWORD_LOGIN_EXISTS);
            } else {
                response.setMessageType(MessageType.CHANGE_PASSWORD_LOGIN_FAILURE);
                response.setMessageBody("Login doesn't exists");
            }
        } catch (SQLException e) {
            response.setMessageType(MessageType.CHANGE_PASSWORD_LOGIN_FAILURE);
            response.setMessageBody("Service currently unavailable. Please try again later");
        }
        sendMessage(response);
    }

    private void sendMessage(Message message) {
        try {
            message.setMessageDate(new Date());
            String jsonMessage = message.messageToJson();
            System.out.println("Message send: " + jsonMessage);
            outputStream.writeUTF(jsonMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUser(String messageBody) {
        Message response = new Message();
        String[] userData = messageBody.split(":", 3); //username : login : password
        try {
            boolean isUsernameExists = dbManager.isUsernameExists(userData[0]);
            boolean isLoginExists = dbManager.isLoginExists(userData[1]);
            if (!isUsernameExists && !isLoginExists) {
                if (dbManager.insertNewUser(userData[0], userData[1], userData[2])) {
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
            response.setMessageBody("Service currently unavailable. Please try again later");
            sendMessage(response);
        }
    }

    private void changeUserPassword(String messageBody) {
        Message response = new Message();
        String[] userData = messageBody.split(":", 3); //login : currPassword : newPassword <- возможна ошибка с : в currPassword
        try {
            ResultSet user = dbManager.selectUserByLoginAndPassword(userData[0], userData[1]);
            if (user.next()) {
                if (dbManager.updateUserPassword(userData[0], userData[2])) {
                    response.setMessageType(MessageType.CHANGE_PASSWORD_SUCCESS);
                } else {
                    throw new SQLException();
                }
            } else {
                response.setMessageType(MessageType.CHANGE_PASSWORD_FAILURE);
                response.setMessageBody("Incorrect current password");
            }
        } catch (SQLException e) {
            response.setMessageType(MessageType.CHANGE_PASSWORD_FAILURE);
            response.setMessageBody("Service currently unavailable. Please try again later");
        }
        sendMessage(response);
    }

    private void changeUserUsername(String messageBody) {
        Message response = new Message();
        String[] userData = messageBody.split(":", 3); //login : newUsername : currPassword
        try {
            User user = server.getUserByLoginAndPassword(userData[0], userData[2]);
            if (user != null) {
                if (dbManager.isUsernameExists(userData[1])) {
                    response.setMessageType(MessageType.CHANGE_USERNAME_FAILURE);
                    response.setMessageBody("Username " + userData[1] + " is already in use ");
                } else {
                    dbManager.updateUserUsername(userData[0], userData[1]);
                    user.setUsername(userData[1]);
                    response.setMessageType(MessageType.CHANGE_USERNAME_SUCCESS);
                    response.setToUser(user);
                }
            } else {
                response.setMessageType(MessageType.CHANGE_USERNAME_INCORRECT_PASSWORD);
                response.setMessageBody("Incorrect current password");
            }
        } catch (SQLException e) {
            response.setMessageType(MessageType.CHANGE_USERNAME_FAILURE);
            response.setMessageBody("Service currently unavailable. Please try again later");
        }
        sendMessage(response);
    }

    private void authenticateUser(String messageBody) {
        Message response = new Message();
        if (!server.isConnectedToChatServer()) {
            response.setMessageType(MessageType.AUTH_FAILURE);
            response.setMessageBody("Server under maintenance. Please try again later.");
        } else {
            String[] userData = messageBody.split(":", 2); //login : password
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
