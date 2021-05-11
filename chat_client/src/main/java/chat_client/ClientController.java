package chat_client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientController {
    public TextArea chatArea;
    public ListView onlineUsers;
    public TextArea userMessage;
    public Button sendButton;

    public void Dummy(ActionEvent actionEvent) {
    }

    public void sendMessageBySendButton(ActionEvent actionEvent) {
        sendMessage();
    }

    public void userMessageUtilityKeyHandler(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.isShiftDown()) {
                userMessage.appendText("\n");
            } else {
                sendMessage();
            }
        }
    }

    public void sendMessage() {
        String rawMessage = userMessage.getText();
        if (rawMessage.length() == 0) return;
        SimpleDateFormat pattern = new SimpleDateFormat("dd/MM/yy\u00A0HH:mm:ss");
        String prefix = "[" + pattern.format(new Date()) + "]" + "\u00A0ME:\u00A0";
        chatArea.appendText(prefix + rawMessage + System.lineSeparator());
        userMessage.clear();
    }

    public void clearChat(ActionEvent actionEvent) {
        chatArea.clear();
    }

    public void closeProgram(ActionEvent actionEvent) {
        Platform.exit();
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
}
