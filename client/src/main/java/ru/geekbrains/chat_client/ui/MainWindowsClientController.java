package ru.geekbrains.chat_client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import ru.geekbrains.chat_client.network.MessageProcessor;
import ru.geekbrains.chat_common.User;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class MainWindowsClientController implements Initializable {
    private static MessageProcessor messageProcessor;

    //AuthWindow vars
    //Login view vars
    @FXML
    public Button authWindowLoginButton;
    @FXML
    public Button authWindowExitButton;
    @FXML
    public Button authWindowSignupButton;
    @FXML
    public Button authWindowChangePasswordButton;
    @FXML
    public Label authWindowStateLabel;
    @FXML
    public PasswordField authWindowPasswordField;
    @FXML
    public TextField authWindowLoginField;
    @FXML
    public VBox loginView;
    //CreateUser view vars
    @FXML
    public Button createUserClearButton;
    @FXML
    public Button createUserBackButton;
    @FXML
    public Label createUserUsernameError;
    @FXML
    public Label createUserLoginError;
    @FXML
    public Label createUserPasswordError;
    @FXML
    public PasswordField createUserPasswordField;
    @FXML
    public PasswordField createUserConfirmPasswordField;
    @FXML
    public TextField createUserUsernameField;
    @FXML
    public TextField createUserLoginField;
    @FXML
    public VBox createUserView;
    //ChangePassword login view vars
    @FXML
    public Button changePasswordLoginBackButton;
    @FXML
    public Label changePasswordLoginErrorLabel;
    @FXML
    public TextField changePasswordLoginTextField;
    @FXML
    public VBox changePasswordLoginView;
    //ChangePassword password view vars
    @FXML
    public Button changePasswordPasswordClearButton;
    @FXML
    public Button changePasswordPasswordBackButton;
    @FXML
    public Label changePasswordCurrentPasswordError;
    @FXML
    public Label changePasswordNewPasswordError;
    @FXML
    public PasswordField changePasswordCurrentPasswordField;
    @FXML
    public PasswordField changePasswordNewPasswordField;
    @FXML
    public PasswordField changePasswordConfirmNewPasswordField;
    @FXML
    public VBox changePasswordPasswordView;

    //ChatWindow vars
    @FXML
    public TextArea chatArea;
    @FXML
    public ListView<User> onlineUsers;
    @FXML
    public TextArea userMessage;
    @FXML
    public Button sendButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (messageProcessor == null) {
            messageProcessor = MessageProcessor.getInstance();
        }
        messageProcessor.setMainWindowController(this);
    }

    public void Dummy() {
    }

    public void showLoginView(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source instanceof Button) {
            switch (((Button) source).getId()) {
                case "createUserBackButton" -> {
                    createUserClearButton.fire();
                    createUserView.setVisible(false);
                }
                case "changePasswordLoginBackButton" -> {
                    clearErrorLabels(changePasswordLoginErrorLabel);
                    clearTextInputs(changePasswordLoginTextField);
                    changePasswordLoginView.setVisible(false);
                }
            }
        }
        loginView.setVisible(true);
    }

    public void showCreateUserView() {
        clearTextInputs(authWindowPasswordField);
        loginView.setVisible(false);
        createUserView.setVisible(true);
    }

    public void showChangePasswordLoginView(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source instanceof Button) {
            switch (((Button) source).getId()) {
                case "changePasswordPasswordBackButton" -> {
                    changePasswordPasswordClearButton.fire();
                    changePasswordPasswordView.setVisible(false);
                }
                case "authWindowChangePasswordButton" -> {
                    clearTextInputs(authWindowPasswordField);
                    loginView.setVisible(false);
                }
            }
        }
        changePasswordLoginView.setVisible(true);
    }

    public void showChangePasswordPasswordView() {
        changePasswordLoginView.setVisible(false);
        changePasswordPasswordView.setVisible(true);
    }

    public void showChatWindow() throws IOException {
        Client.showChatStage();
    }

    public void openAboutWindow() throws IOException {
        Client.AboutWindow.display();
    }

    public void openChangeUsernameWindow() throws IOException {
        Client.ChangeUsernameWindow.display();
    }

    public void sendAuthRequest() {
        String login = authWindowLoginField.getText();
        String password = authWindowPasswordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            authWindowStateLabel.setText("Please enter login and password.");
            return;
        }
        clearErrorLabels(authWindowStateLabel);
        messageProcessor.sendAuthRequest(login, password);
    }

    public void submitCreateUserRequest() {
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
        if (!isIncorrectInput) messageProcessor.sendCreateUserRequest(username, login, password);
    }

    public void changePasswordCheckIfLoginExists() {
        clearErrorLabels(changePasswordLoginErrorLabel);
        String login;
        if ((login = changePasswordLoginTextField.getText()).isEmpty()) {
            changePasswordLoginErrorLabel.setText("Enter your login");
            return;
        }
        messageProcessor.sendChangePasswordLoginCheckRequest(login);
    }

    public void submitChangePasswordRequest() {
        clearErrorLabels(changePasswordCurrentPasswordError, changePasswordNewPasswordError);
        boolean isIncorrectInput = false;
        String login = changePasswordLoginTextField.getText();
        String currPassword;
        String newPassword;
        if ((currPassword = changePasswordCurrentPasswordField.getText()).isEmpty()) {
            isIncorrectInput = true;
            changePasswordCurrentPasswordError.setText("Enter your current password");
        }
        if ((newPassword = changePasswordNewPasswordField.getText()).isEmpty()) {
            isIncorrectInput = true;
            changePasswordNewPasswordError.setText("Enter your new password");
        } else if (!newPassword.equals(changePasswordConfirmNewPasswordField.getText())) {
            isIncorrectInput = true;
            changePasswordNewPasswordError.setText("Passwords doesn't match!");
        } else if (currPassword.equals(newPassword)) {
            isIncorrectInput = true;
            changePasswordCurrentPasswordError.setText("Current and new passwords must be different!");
            changePasswordNewPasswordError.setText("Current and new passwords must be different!");
        }
        if (!isIncorrectInput) messageProcessor.sendChangePasswordRequest(login, currPassword, newPassword);
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

    public void sendMessageBySendButton() {
        sendMessage();
    }

    public void clearChat() {
        chatArea.clear();
    }

    public void closeProgram() {
        System.exit(0);
    }

    public void toGitHubPage() throws URISyntaxException, IOException {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI("https://github.com/Bananza93/Java_chat"));
    }

    public void loginByEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            authWindowLoginButton.fire();
        }
    }

    public void focusToLoginField() {
        authWindowLoginField.requestFocus();
    }

    public void focusToPasswordField() {
        authWindowPasswordField.requestFocus();
    }

    public void createUserClearFormsAction() {

        clearTextInputs(createUserUsernameField, createUserLoginField, createUserPasswordField, createUserConfirmPasswordField);
        clearErrorLabels(createUserUsernameError, createUserLoginError, createUserPasswordError);
    }

    public void changePasswordPasswordClearForms() {
        clearTextInputs(changePasswordCurrentPasswordField, changePasswordNewPasswordField, changePasswordConfirmNewPasswordField);
        clearErrorLabels(changePasswordCurrentPasswordError, changePasswordNewPasswordError);
    }

    private void clearErrorLabels(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }

    private void clearTextInputs(TextInputControl... textInputElement) {
        for (TextInputControl textInputControl : textInputElement) {
            textInputControl.clear();
        }
    }
}
