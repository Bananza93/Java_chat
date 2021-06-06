package ru.geekbrains.chat_client.network;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat_client.utils.MessageHistory;
import ru.geekbrains.chat_client.ui.MainWindowsClientController;
import ru.geekbrains.chat_client.ui.SubWindowsClientController;
import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Set;

public class MessageProcessor {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final MessageProcessor instance = new MessageProcessor();
    private static final User PUBLIC_TECH_USER = new User("PUBLIC", "", "");
    private final Object mon1 = new Object();
    private MainWindowsClientController mainWindowController;
    private SubWindowsClientController subWindowController;

    private MessageProcessor() {
    }

    public static MessageProcessor getInstance() {
        return instance;
    }

    public void setMainWindowController(MainWindowsClientController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public void setSubWindowController(SubWindowsClientController subWindowController) {
        this.subWindowController = subWindowController;
    }

    public synchronized void incomingMessage(String jsonMessage) throws IOException {
        LOGGER.debug("Message received: " + jsonMessage);
        Message message = Message.messageFromJson(jsonMessage);
        switch (message.getMessageType()) {
            case AUTH_FAILURE -> Platform.runLater(() -> {
                mainWindowController.authWindowPasswordField.clear();
                mainWindowController.authWindowStateLabel.setText(message.getMessageBody());
            });
            case AUTH_SUCCESS -> Platform.runLater(() -> {
                try {
                    mainWindowController.showChatWindow();
                    ConnectionManager.setCurrentUser(message.getToUser());
                    for (String msg : MessageHistory.readFromHistory()) mainWindowController.chatArea.appendText(msg + System.lineSeparator());
                    ConnectionManager.getCurrentChatServerSession();
                    sendSubscribeRequest(ConnectionManager.getCurrentUser());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            case PUBLIC -> {
                if (message.getFromUser().getUsername().equals(ConnectionManager.getCurrentUser().getUsername()))
                    return;
                Platform.runLater(() -> {
                    String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                            + "]\u00A0" + message.getFromUser().getUsername()
                            + ":\u00A0" + message.getMessageBody()
                            + System.lineSeparator();
                    mainWindowController.chatArea.appendText(msg);
                    MessageHistory.writeToHistory(msg);
                });
            }
            case PRIVATE -> Platform.runLater(() -> {
                String msg = "[" + new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss").format(message.getMessageDate())
                        + "]\u00A0" + message.getFromUser().getUsername()
                        + "\u00A0->\u00A0ME:\u00A0" + message.getMessageBody()
                        + System.lineSeparator();
                mainWindowController.chatArea.appendText(msg);
                MessageHistory.writeToHistory(msg);
            });
            case ONLINE_USERS_LIST -> Platform.runLater(() -> {
                Set<User> users = message.getOnlineUsersSet();
                users.remove(ConnectionManager.getCurrentUser());
                mainWindowController.onlineUsers.setItems(FXCollections.observableArrayList(users));
                mainWindowController.onlineUsers.getItems().add(0, PUBLIC_TECH_USER);
                mainWindowController.onlineUsers.getSelectionModel().selectFirst();
            });
            case CREATE_USER_USERNAME_EXISTS -> Platform.runLater(() -> mainWindowController.createUserUsernameError.setText(message.getMessageBody()));
            case CREATE_USER_LOGIN_EXISTS -> Platform.runLater(() -> mainWindowController.createUserLoginError.setText(message.getMessageBody()));
            case CREATE_USER_SUCCESS -> Platform.runLater(() -> mainWindowController.createUserBackButton.fire());
            case CHANGE_PASSWORD_LOGIN_FAILURE -> Platform.runLater(() -> mainWindowController.changePasswordLoginErrorLabel.setText(message.getMessageBody()));
            case CHANGE_PASSWORD_LOGIN_EXISTS -> Platform.runLater(() -> mainWindowController.showChangePasswordPasswordView());
            case CHANGE_PASSWORD_SUCCESS -> Platform.runLater(() -> {
                mainWindowController.changePasswordPasswordBackButton.fire();
                mainWindowController.changePasswordLoginBackButton.fire();
            });
            case CHANGE_PASSWORD_FAILURE -> Platform.runLater(() -> mainWindowController.changePasswordCurrentPasswordError.setText(message.getMessageBody()));
            case CHANGE_USERNAME_INCORRECT_PASSWORD -> Platform.runLater(() -> subWindowController.changeUsernamePasswordError.setText(message.getMessageBody()));
            case CHANGE_USERNAME_FAILURE -> Platform.runLater(() -> subWindowController.changeUsernameUsernameError.setText(message.getMessageBody()));
            case CHANGE_USERNAME_SUCCESS -> Platform.runLater(() -> {
                ConnectionManager.setCurrentUser(message.getToUser());
                sendSubscribeRequest(ConnectionManager.getCurrentUser());
                subWindowController.changeUsernameCancelButton.fire();
            });
        }
    }

    public void outgoingMessage(String jsonMessage, ClientSessionHandler session) {
        synchronized (mon1) {
            session.sendMessage(jsonMessage);
        }
    }

    public void sendPublicMessage(String rawMessage) {
        new Thread(() -> {
            Message message = new Message(MessageType.PUBLIC);
            message.setMessageBody(rawMessage.trim());
            message.setFromUser(ConnectionManager.getCurrentUser());
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentChatServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendPrivateMessage(String rawMessage, User toUser) {
        new Thread(() -> {
            Message message = new Message(MessageType.PRIVATE);
            message.setMessageBody(rawMessage.trim());
            message.setFromUser(ConnectionManager.getCurrentUser());
            message.setToUser(toUser);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentChatServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendSubscribeRequest(User user) {
        new Thread(() -> {
            Message message = new Message(MessageType.SUBSCRIBE_REQUEST);
            message.setFromUser(user);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentChatServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendAuthRequest(String login, String password) {
        new Thread(() -> {
            Message message = new Message(MessageType.AUTH_REQUEST);
            message.setMessageBody(login + ":" + password);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentAuthServerSession());
            } catch (IOException e) {
                //mainWindowController.authWindowStateLabel.setText("Auth server unavailable.");
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendCreateUserRequest(String username, String login, String password) {
        new Thread(() -> {
            Message message = new Message(MessageType.CREATE_USER_REQUEST);
            message.setMessageBody(username + ":" + login + ":" + password);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentAuthServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendChangePasswordLoginCheckRequest(String login) {
        new Thread(() -> {
            Message message = new Message(MessageType.CHANGE_PASSWORD_LOGIN_CHECK);
            message.setMessageBody(login);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentAuthServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendChangePasswordRequest(String login, String currPassword, String newPassword) {
        new Thread(() -> {
            Message message = new Message(MessageType.CHANGE_PASSWORD_REQUEST);
            message.setMessageBody(login + ":" + currPassword + ":" + newPassword);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentAuthServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }

    public void sendChangeUsernameRequest(String login, String newUsername, String password) {
        new Thread(() -> {
            Message message = new Message(MessageType.CHANGE_USERNAME_REQUEST);
            message.setMessageBody(login + ":" + newUsername + ":" + password);
            try {
                outgoingMessage(message.messageToJson(), ConnectionManager.getCurrentAuthServerSession());
            } catch (IOException e) {
                LOGGER.debug("EXCEPTION!", e);
            }
        }).start();
    }
}
