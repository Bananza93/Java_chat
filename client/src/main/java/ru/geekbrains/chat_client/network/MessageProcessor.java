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
    private final ClientController controller;
    private ClientSessionHandler currentSession;

    public MessageProcessor(ClientController controller) {
        this.controller = controller;
    }

    public void setCurrentSession(ClientSessionHandler session) {
        this.currentSession = session;
    }

    public synchronized void incomingMessage(String jsonMessage) throws IOException {
        System.out.println("Message received: " + jsonMessage);
        Message message = Message.messageFromJson(jsonMessage);
        if (message.getMessageType().equals(MessageType.AUTH_FAILURE)) {
            Platform.runLater(() -> {
                controller.authWindowPasswordField.clear();
                controller.authWindowStateLabel.setText(message.getMessageBody());
            });
        } else if (message.getMessageType().equals(MessageType.AUTH_SUCCESS)) {
            Platform.runLater(() -> {
                try {
                    controller.loadChatWindow(message.getToUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (message.getMessageType().equals(MessageType.PUBLIC)) {
            if (message.getFromUser().getUsername().equals(currentSession.getSessionOwner().getUsername())) return;
            Platform.runLater(() -> {
                String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                        + "]\u00A0" + message.getFromUser().getUsername()
                        + ":\u00A0" + message.getMessageBody()
                        + System.lineSeparator();
                controller.chatArea.appendText(msg);
            });
        } else if (message.getMessageType().equals(MessageType.ONLINE_USERS_LIST)) {
            Platform.runLater(() -> {
                Set<User> users = message.getOnlineUsersSet();
                users.remove(currentSession.getSessionOwner());
                controller.onlineUsers.setItems(FXCollections.observableArrayList(users));
                controller.onlineUsers.getItems().add(0, new User("PUBLIC", "", ""));
                controller.onlineUsers.getSelectionModel().selectFirst();
            });
        } else if (message.getMessageType().equals(MessageType.PRIVATE)) {
            Platform.runLater(() -> {
                String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                        + "]\u00A0" + message.getFromUser().getUsername()
                        + "\u00A0->\u00A0ME:\u00A0" + message.getMessageBody()
                        + System.lineSeparator();
                controller.chatArea.appendText(msg);
            });
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
}
