package ru.geekbrains.chat_client.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class Client extends Application {
    public static Stage authStage;
    public static Stage chatStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        createAuthStage(stage);
        createChatStage(new Stage());
        showAuthStage();
    }

    public static void showAuthStage() {
        if (chatStage.isShowing()) chatStage.close();
        authStage.show();
    }

    public static void showChatStage() {
        if (authStage.isShowing()) authStage.close();
        chatStage.show();
    }

    private void createAuthStage(Stage stage) throws IOException {
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(false);
        stage.setTitle("POGGERS chat");
        stage.getIcons().add(new Image("poggers.jpg"));
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("/AuthWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
        authStage = stage;
    }

    private void createChatStage(Stage stage) throws IOException {
        stage.initStyle(StageStyle.DECORATED);
        stage.setMinWidth(500.0);
        stage.setMinHeight(300.0);
        stage.setTitle("POGGERS chat");
        stage.getIcons().add(new Image("poggers.jpg"));
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("/ChatWindow.fxml"));
        stage.setScene(new Scene(loader.load()));
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
            window.getIcons().add(new Image("poggers.jpg"));
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
