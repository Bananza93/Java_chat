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
import ru.geekbrains.chat_client.network.ClientSessionHandler;
import ru.geekbrains.chat_client.network.MessageProcessor;

import java.net.URL;
import java.util.ResourceBundle;

public class SubWindowsClientController implements Initializable {
    private static MessageProcessor messageProcessor;
    private static ClientSessionHandler currentSession;

    @FXML
    public Button changeUsernameCancelButton;
    @FXML
    public Label changeUsernamePasswordError;
    @FXML
    public Label changeUsernameUsernameError;
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

    public void submitChangeUsernameRequest(ActionEvent actionEvent) {
    }

    public static ClientSessionHandler getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(ClientSessionHandler sessionHandler) {
        currentSession = sessionHandler;
    }
}
