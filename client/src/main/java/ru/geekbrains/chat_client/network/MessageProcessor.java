package ru.geekbrains.chat_client.network;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import ru.geekbrains.chat_client.ui.ClientController;
import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class MessageProcessor {
    private ClientController controller;
    private ClientSessionHandler currentSession;

    public MessageProcessor(ClientController controller) {
        this.controller = controller;
    }

    public ClientSessionHandler getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(ClientSessionHandler session) {
        this.currentSession = session;
    }

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public synchronized void incomingMessage(String jsonMessage) throws IOException {
        System.out.println("Message received: " + jsonMessage);
        Message message = Message.messageFromJson(jsonMessage);
        switch (message.getMessageType()) {
            case AUTH_FAILURE ->
                Platform.runLater(() -> {
                    controller.authWindowPasswordField.clear();
                    controller.authWindowStateLabel.setText(message.getMessageBody());
                });
            case AUTH_SUCCESS ->
                Platform.runLater(() -> {
                    try {
                        controller.loadChatWindow(message.getToUser());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            case PUBLIC -> {
                if (message.getFromUser().getUsername().equals(currentSession.getSessionOwner().getUsername())) return;
                Platform.runLater(() -> {
                    String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                            + "]\u00A0" + message.getFromUser().getUsername()
                            + ":\u00A0" + message.getMessageBody()
                            + System.lineSeparator();
                    controller.chatArea.appendText(msg);
                });
            }
            case ONLINE_USERS_LIST ->
                Platform.runLater(() -> {
                    Set<User> users = message.getOnlineUsersSet();
                    users.remove(currentSession.getSessionOwner());
                    controller.onlineUsers.setItems(FXCollections.observableArrayList(users));
                    controller.onlineUsers.getItems().add(0, new User("PUBLIC", "", ""));
                    controller.onlineUsers.getSelectionModel().selectFirst();
                });
            case PRIVATE ->
                Platform.runLater(() -> {
                    String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                            + "]\u00A0" + message.getFromUser().getUsername()
                            + "\u00A0->\u00A0ME:\u00A0" + message.getMessageBody()
                            + System.lineSeparator();
                    controller.chatArea.appendText(msg);
                });
            case CREATE_USER_USERNAME_EXISTS ->
                    Platform.runLater(() -> controller.createUserUsernameError.setText(message.getMessageBody()));
            case CREATE_USER_LOGIN_EXISTS ->
                Platform.runLater(() -> controller.createUserLoginError.setText(message.getMessageBody()));
            case CREATE_USER_SUCCESS -> Platform.runLater(() -> controller.createUserBackButton.fire());
        }
    }

    public synchronized void outgoingMessage(String jsonMessage) {
        System.out.println("Message send: " + jsonMessage);
        currentSession.sendMessage(jsonMessage);
    }

    public void sendPublicMessage(String rawMessage) {
        new Thread(() -> {
            Message message = new Message();
            message.setMessageType(MessageType.PUBLIC);
            message.setMessageBody(rawMessage.trim());
            message.setFromUser(currentSession.getSessionOwner());
            message.setMessageDate(new Date());
            outgoingMessage(message.messageToJson());
        }).start();
    }

    public void sendPrivateMessage(String rawMessage, User toUser) {
        new Thread(() -> {
            Message message = new Message();
            message.setMessageType(MessageType.PRIVATE);
            message.setMessageBody(rawMessage.trim());
            message.setFromUser(currentSession.getSessionOwner());
            message.setToUser(toUser);
            message.setMessageDate(new Date());
            outgoingMessage(message.messageToJson());
        }).start();
    }

    public void sendSubscribeRequest(User user) {
        new Thread(() -> {
            Message message = new Message();
            message.setMessageType(MessageType.SUBSCRIBE_REQUEST);
            message.setFromUser(user);
            message.setMessageDate(new Date());
            outgoingMessage(message.messageToJson());
        }).start();
    }

    public void sendAuthRequest(String login, String password) {
        new Thread(() -> {
            Message message = new Message();
            message.setMessageType(MessageType.AUTH_REQUEST);
            message.setMessageBody(login + ":" + password);
            message.setMessageDate(new Date());
            outgoingMessage(message.messageToJson());
        }).start();
    }

    public void sendCreateUserRequest(String username, String login, String password) {
        new Thread(() -> {
            Message message = new Message();
            message.setMessageType(MessageType.CREATE_USER_REQUEST);
            message.setMessageBody(username + ":" + login + ":" + password);
            message.setMessageDate(new Date());
            outgoingMessage(message.messageToJson());
        }).start();
    }
}
