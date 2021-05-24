package ru.geekbrains.chat_client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.geekbrains.chat_common.User;

import java.io.IOException;

public class Client extends Application {
    public static Stage authStage;
    public static Stage chatStage;
    public static Scene loginScene;
    public static Scene createUserScene;
    public static Scene changePasswordScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        showAuthStage();
    }

    public static void showAuthStage() throws IOException {
        if (chatStage != null && chatStage.isShowing()) {
            chatStage.close();
            chatStage = null;
        }
        createAuthStage(new Stage());
        authStage.show();
    }

    public static void showChatStage() throws IOException {
        if (authStage != null && authStage.isShowing()) {
            authStage.close();
            authStage = null;
        }
        createChatStage(new Stage());
        chatStage.show();
    }

    public static void showLoginScene() throws IOException {
        authStage.setScene(loginScene);
    }

    public static void showCreateUserScene() throws IOException {
        authStage.setScene(createUserScene);
    }

    public static void showChangePasswordScene() throws IOException {
        authStage.setScene(changePasswordScene);
    }

    private static void createAuthStage(Stage stage) throws IOException {
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        stage.setTitle("POGGERS chat");
        stage.getIcons().add(new Image("poggers_32x32.jpg"));
        stage.setOnCloseRequest(e -> System.exit(0));

        //loginScene
        FXMLLoader loader1 = new FXMLLoader();
        loader1.setLocation(Client.class.getResource("/AuthWindow.fxml"));
        loginScene = new Scene(loader1.load());
        //createUserScene
        FXMLLoader loader2 = new FXMLLoader();
        loader2.setLocation(Client.class.getResource("/CreateUserWindow.fxml"));
        createUserScene = new Scene(loader2.load());
        //changePasswordScene
        FXMLLoader loader3 = new FXMLLoader();
        loader3.setLocation(Client.class.getResource("/ChangePasswordWindow.fxml"));
        changePasswordScene = new Scene(loader3.load());

        authStage = stage;
        showLoginScene();
    }

    private static void createChatStage(Stage stage) throws IOException {
        stage.initStyle(StageStyle.DECORATED);
        stage.setMinWidth(500.0);
        stage.setMinHeight(300.0);
        stage.setTitle("POGGERS chat");
        stage.getIcons().add(new Image("poggers_32x32.jpg"));
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("/ChatWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
        ListView<User> onlineUsers = (ListView<User>) stage.getScene().lookup("#onlineUsers");
        onlineUsers.setCellFactory(stringListView -> new ListCell<>() {
            @Override
            public void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText(null);
                } else {
                    setText(item.getUsername());
                }
            }
        });
        stage.setOnCloseRequest(e -> System.exit(0));
        chatStage = stage;
    }

    static class AboutWindow {

        public static void display() throws IOException {
            Stage window = new Stage();
            window.initOwner(chatStage);
            window.initModality(Modality.WINDOW_MODAL);
            window.initStyle(StageStyle.UNIFIED);
            window.setWidth(250.0);
            window.setHeight(350.0);
            window.setTitle("About POGGERS chat");
            window.getIcons().add(new Image("poggers_32x32.jpg"));
            window.setResizable(false);
            window.setX((chatStage.getX() + (chatStage.getWidth() / 2)) - (window.getWidth() / 2));
            window.setY((chatStage.getY() + (chatStage.getHeight() / 2)) - (window.getHeight() / 2));

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AboutWindow.class.getResource("/AboutWindow.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            window.setScene(scene);
            window.showAndWait();
        }
    }
}
