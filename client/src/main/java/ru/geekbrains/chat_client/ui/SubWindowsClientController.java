package ru.geekbrains.chat_client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SubWindowsClientController {
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
}
