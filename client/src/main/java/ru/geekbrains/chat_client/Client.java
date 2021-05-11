package ru.geekbrains.chat_client;

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
    private static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/MainWindow.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMinWidth(500.0);
        stage.setMinHeight(300.0);
        stage.setTitle("POGGERS chat");
        stage.getIcons().add(new Image("poggers.jpg"));
        stage.show();
    }

    static class AboutWindow {

        public static void display() throws IOException {
            Stage window = new Stage();
            window.initOwner(mainStage);
            window.initModality(Modality.WINDOW_MODAL);
            window.initStyle(StageStyle.UNIFIED);
            window.setWidth(250.0);
            window.setHeight(350.0);
            window.setTitle("About POGGERS chat");
            window.getIcons().add(new Image("poggers.jpg"));
            window.setResizable(false);
            window.setX((mainStage.getX() + (mainStage.getWidth() / 2)) - (window.getWidth() / 2));
            window.setY((mainStage.getY() + (mainStage.getHeight() / 2)) - (window.getHeight() / 2));

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(AboutWindow.class.getResource("/AboutWindow.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            window.setScene(scene);
            window.showAndWait();
        }
    }
}
