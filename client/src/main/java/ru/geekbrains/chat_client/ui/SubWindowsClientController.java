package ru.geekbrains.chat_client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.geekbrains.chat_client.network.ConnectionManager;
import ru.geekbrains.chat_client.network.MessageProcessor;
import ru.geekbrains.chat_client.utils.NodeUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class SubWindowsClientController implements Initializable {
    private static MessageProcessor messageProcessor;

    @FXML
    public Button changeUsernameCancelButton;
    @FXML
    public Label changeUsernameUsernameError;
    @FXML
    public Label changeUsernamePasswordError;
    @FXML
    public PasswordField changeUsernamePasswordField;
    @FXML
    public TextField changeUsernameUsernameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (messageProcessor == null) {
            messageProcessor = MessageProcessor.getInstance();
        }
        messageProcessor.setSubWindowController(this);
    }

    public void closeAboutWindowByCloseButton(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void closeChangeUsernameByCancelButton(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void submitChangeUsernameRequest() {
        NodeUtils.clearErrorLabels(changeUsernameUsernameError, changeUsernamePasswordError);
        boolean isIncorrectInput = false;
        String newUsername;
        String password;
        if ((newUsername = changeUsernameUsernameField.getText()).isEmpty()) {
            isIncorrectInput = true;
            changeUsernameUsernameError.setText("Enter new username");
        }
        if ((password = changeUsernamePasswordField.getText()).isEmpty()) {
            isIncorrectInput = true;
            changeUsernamePasswordError.setText("Enter your password");
        }
        if (!isIncorrectInput) messageProcessor.sendChangeUsernameRequest(ConnectionManager.getCurrentUser().getLogin(), newUsername, password);
    }

}
