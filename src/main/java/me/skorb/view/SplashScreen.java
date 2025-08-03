package me.skorb.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {
    private Stage splashStage;

    public void show() {
        Platform.runLater(() -> {
            splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED); // No window decorations

            ProgressIndicator progressIndicator = new ProgressIndicator();
            Label loadingLabel = new Label("Loading... Please wait");

            VBox root = new VBox(10, progressIndicator, loadingLabel);
            root.setStyle("-fx-alignment: center; -fx-padding: 20px; -fx-background-color: white;");

            Scene scene = new Scene(root, 300, 200);
            splashStage.setScene(scene);
            splashStage.show();
        });
    }

    public void close() {
        Platform.runLater(() -> splashStage.close());
    }
}
