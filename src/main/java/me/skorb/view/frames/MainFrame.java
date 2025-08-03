package me.skorb.view.frames;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import me.skorb.view.screens.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainFrame extends Application {

    private static final Logger logger = LogManager.getLogger(MainFrame.class);

    private Pane dashboardView;
    private Pane customersView;
    private Pane ordersView;
    private Pane appointmentsView;
    private Pane servicesView;

    private BorderPane root;
    private StackPane centerContent;

    @Override
    public void start(Stage primaryStage) {
        dashboardView = new DashboardView();
        customersView = new CustomersView();
        ordersView = new OrdersView();
        appointmentsView = new AppointmentsView();
        servicesView = new ServicesView();

        // Создаем основной layout
        root = new BorderPane();

        // Создаем центральную область, где будет отображаться текущий экран
        centerContent = new StackPane();
        centerContent.setPadding(new Insets(15));
        centerContent.setStyle("-fx-background-color: #E5F0FC;");
        root.setCenter(centerContent);

        // Создаем навигационную панель
        VBox navBar = new VBox(10);
        navBar.setPadding(new Insets(15));
        navBar.setPrefWidth(150);
        navBar.setStyle("-fx-background-color: #2D5A90;");
        navBar.setAlignment(Pos.TOP_CENTER);

        // Кнопки навигации
        Button dashboardBtn = createNavButton("Dashboard", () -> showScreen(dashboardView));
        Button appointmentsBtn = createNavButton("Appointments", () -> showScreen(appointmentsView));
        Button customersBtn = createNavButton("Customers", () -> showScreen(customersView));
        Button ordersBtn = createNavButton("Orders", () -> showScreen(ordersView));
        Button servicesBtn = createNavButton("Services", () -> showScreen(servicesView));
        Button partsBtn = createNavButton("Parts", () -> showScreen(new Text("Parts Screen")));

        navBar.getChildren().addAll(
                dashboardBtn, appointmentsBtn, customersBtn,
                ordersBtn, servicesBtn, partsBtn
        );

        root.setLeft(navBar);

        // Show Dashboard screen by default
        showScreen(dashboardView);

        setIcon(primaryStage);

        Scene scene = new Scene(root, 1360, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Car Service Management System");
        // Handle window close event
        primaryStage.setOnCloseRequest(event -> {
            shutdownApplication();
        });
        primaryStage.show();
    }

    private void setIcon(Stage primaryStage) {
        try {
            Image icon = new Image("file:img/icon.png");
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            logger.warn("Can't load icon file");
        }
    }

    private Button createNavButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
                "-fx-background-color: white; " +
                        "-fx-text-fill: #2D5A90; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;"
        );
        button.setOnAction(e -> action.run());
        button.setCursor(Cursor.HAND);
        return button;
    }

    private void showScreen(Node content) {
        centerContent.getChildren().setAll(content);
    }

    private void shutdownApplication() {
        logger.info("Shutting down the application...");

        Platform.exit();
        System.exit(0);
    }

}
