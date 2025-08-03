package me.skorb.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.skorb.Main;
import me.skorb.view.frames.MainFrame;
//import org.springframework.boot.SpringApplication;
//import org.springframework.context.ConfigurableApplicationContext;

public class SplashScreenApp extends Application {
    private static Stage splashStage;

    @Override
    public void start(Stage primaryStage) {
        splashStage = primaryStage;
        splashStage.initStyle(StageStyle.UNDECORATED); // No window decorations

        ProgressIndicator progressIndicator = new ProgressIndicator();

        VBox root = new VBox(10, progressIndicator);
        root.setStyle("-fx-alignment: center; -fx-padding: 20px; -fx-background-color: white;");

        Scene scene = new Scene(root, 300, 200);
        splashStage.setScene(scene);
        splashStage.show();

        // Start Spring Boot in a background thread
        new Thread(() -> {
//            ConfigurableApplicationContext springContext = SpringApplication.run(Main.class);
//            Main.setSpringContext(springContext);

            // Once Spring Boot is ready, close splash screen and start MainFrame
            Platform.runLater(() -> {
                splashStage.close();

                // Start MainFrame manually instead of using Application.launch()
//                MainFrame mainFrame = springContext.getBean(MainFrame.class);
                try {
//                    mainFrame.start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    public static void main(String[] args) {
        launch(args); // Start JavaFX and show the splash screen
    }
}
