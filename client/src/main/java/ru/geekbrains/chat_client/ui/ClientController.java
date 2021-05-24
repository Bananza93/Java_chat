package ru.geekbrains.chat_client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ru.geekbrains.chat_client.network.ClientSessionHandler;
import ru.geekbrains.chat_client.network.MessageProcessor;
import ru.geekbrains.chat_common.Message;
import ru.geekbrains.chat_common.MessageType;
import ru.geekbrains.chat_common.User;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class ClientController implements Initializable {
    private static final String AUTH_SERVER_HOST = "localhost";
    private static final int AUTH_SERVER_PORT = 22222;
    private static final String CHAT_SERVER_HOST = "localhost";
    private static final int CHAT_SERVER_PORT = 11111;
    private static MessageProcessor messageProcessor;
    private static ClientSessionHandler currentSession;

    //AuthWindow vars
    //Login scene vars
    @FXML
    public TextField authWindowLoginField;
    @FXML
    public PasswordField authWindowPasswordField;
    @FXML
    public Button authWindowLoginButton;
    @FXML
    public Button authWindowExitButton;
    @FXML
    public Label authWindowStateLabel;
    @FXML
    public Button authWindowSignupButton;
    @FXML
    public Button authWindowChangePasswordButton;
    //CreateUser scene vars
    @FXML
    public TextField createUserUsernameField;
    @FXML
    public TextField createUserLoginField;
    @FXML
    public PasswordField createUserPasswordField;
    @FXML
    public PasswordField createUserConfirmPasswordField;
    @FXML
    public Label createUserUsernameError;
    @FXML
    public Label createUserLoginError;
    @FXML
    public Label createUserPasswordError;

    //ChatWindow vars
    @FXML
    public TextArea chatArea;
    @FXML
    public ListView<User> onlineUsers;
    @FXML
    public TextArea userMessage;
    @FXML
    public Button sendButton;


    public void Dummy(ActionEvent actionEvent) {
    }

    public void sendAuthRequest(ActionEvent actionEvent) {
        String login = authWindowLoginField.getText();
        String password = authWindowPasswordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            authWindowStateLabel.setText("Please enter login and password.");
            return;
        }
        authWindowStateLabel.setText("");
        if (connectToAuthServer()) messageProcessor.sendAuthRequest(login, password);
    }

    public void sendMessageBySendButton(ActionEvent actionEvent) {
        sendMessage();
    }

    public void userMessageUtilityKeyHandler(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.isShiftDown()) {
                userMessage.appendText("\n");
            } else {
                userMessage.setText(userMessage.getText().substring(0, userMessage.getText().length() - 1));
                sendMessage();
            }
        }
    }

    public void sendMessage() {
        String rawMessage = userMessage.getText();
        if (rawMessage.length() == 0) return;
        SimpleDateFormat pattern = new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss");
        String prefix;
        User toUser = onlineUsers.getSelectionModel().getSelectedItem();
        if (toUser.getUsername().equals("PUBLIC")) {
            messageProcessor.sendPublicMessage(rawMessage);
            prefix = "[" + pattern.format(new Date()) + "]" + "\u00A0ME:\u00A0";
        } else {
            messageProcessor.sendPrivateMessage(rawMessage, toUser);
            prefix = "[" + pattern.format(new Date()) + "]" + "\u00A0ME\u00A0->\u00A0" + toUser.getUsername() + ":\u00A0";
        }
        chatArea.appendText(prefix + rawMessage + System.lineSeparator());
        userMessage.clear();
    }

    public void clearChat(ActionEvent actionEvent) {
        chatArea.clear();
    }

    public void closeProgram(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void toGitHubPage(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI("https://github.com/Bananza93/Java_chat"));
    }

    public void openAboutWindow(ActionEvent actionEvent) throws IOException {
        Client.AboutWindow.display();
    }

    public void closeAboutWindowByCloseButton(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (messageProcessor == null) {
            messageProcessor = new MessageProcessor(this);
        } else {
            messageProcessor.setController(this);
        }
    }


    public void loadChatWindow(User owner) throws IOException {
        connectToChatServer(owner);
        Client.showChatStage();
        Client.chatStage.setTitle(Client.chatStage.getTitle() + " [User: " + owner.getUsername() + "]");
        messageProcessor.sendSubscribeRequest(owner);
    }

    private boolean connectToAuthServer() {
        try {
            if (currentSession != null) {
                if (!currentSession.getServerType().equals("AUTH")) {
                    currentSession.close();
                } else {
                    return true;
                }
            }
            currentSession = new ClientSessionHandler(new Socket(AUTH_SERVER_HOST, AUTH_SERVER_PORT), "AUTH", messageProcessor);
            currentSession.handle();
            return true;
        } catch (IOException e) {
            authWindowStateLabel.setText("Auth server unavailable.");
        }
        return false;
    }

    private boolean connectToChatServer(User owner) {
        try {
            if (currentSession != null) {
                if (!currentSession.getServerType().equals("CHAT")) {
                    currentSession.close();
                } else {
                    return true;
                }
            }
            currentSession = new ClientSessionHandler(new Socket(CHAT_SERVER_HOST, CHAT_SERVER_PORT), "CHAT", messageProcessor, owner);
            currentSession.handle();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loginByEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            authWindowLoginButton.fire();
        }
    }

    public void focusToLoginField(MouseEvent mouseEvent) {
        authWindowLoginField.requestFocus();
    }

    public void focusToPasswordField(MouseEvent mouseEvent) {
        authWindowPasswordField.requestFocus();
    }

    public void openLoginScene(ActionEvent actionEvent) throws IOException {
        Client.showLoginScene();
    }

    public void openCreateUserScene(ActionEvent actionEvent) throws IOException {
        Client.showCreateUserScene();
    }

    public void openChangePasswordScene(ActionEvent actionEvent) throws IOException {
        Client.showChangePasswordScene();
    }

    public void createUserSubmitAction(ActionEvent actionEvent) {
        clearErrorLabels(createUserUsernameError, createUserLoginError, createUserPasswordError);
        boolean isIncorrectInput = false;
        String username;
        String login;
        String password;
        if ((username = createUserUsernameField.getText()).isEmpty()) {
            isIncorrectInput = true;
            createUserUsernameError.setText("Enter your username");
        }
        if ((login = createUserLoginField.getText()).isEmpty()) {
            isIncorrectInput = true;
            createUserLoginError.setText("Enter your login");
        }
        if ((password = createUserPasswordField.getText()).isEmpty()) {
            isIncorrectInput = true;
            createUserPasswordError.setText("Enter your password");
        } else if (!password.equals(createUserConfirmPasswordField.getText())) {
            isIncorrectInput = true;
            createUserPasswordError.setText("Passwords doesn't match!");
        }
        if (!isIncorrectInput && connectToAuthServer()) messageProcessor.sendCreateUserRequest(username, login, password);
    }

    private void clearErrorLabels(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }
}
