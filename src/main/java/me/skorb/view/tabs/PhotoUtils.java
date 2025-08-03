package me.skorb.view.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.skorb.entity.Order;
import me.skorb.service.OrderService;
import me.skorb.view.ViewUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class PhotoUtils {

    private static final OrderService ORDER_SERVICE = new OrderService();

    public static void showOrderPhotosDialog(Order order) {
        List<String> photoPaths = ORDER_SERVICE.getPhotoPaths(order.getId());

        Stage photoStage = new Stage();
        photoStage.initModality(Modality.APPLICATION_MODAL);
        photoStage.setTitle("Order Photos");

        FlowPane photoPane = new FlowPane();
        photoPane.setPadding(new Insets(10));
        photoPane.setHgap(10);
        photoPane.setVgap(10);
        photoPane.setAlignment(Pos.CENTER);

        // Initial load of photos
        updatePhotoPane(photoPane, order, photoPaths);

        // Attach Photos Button
        Button attachPhotosButton = new Button("Attach Photos");
        attachPhotosButton.setCursor(Cursor.HAND);
        attachPhotosButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        attachPhotosButton.setOnAction(event -> {
            PhotoUtils.attachPhotosToOrder(order.getId());

            // Refresh photo list dynamically
            List<String> updatedPhotoPaths = ORDER_SERVICE.getPhotoPaths(order.getId());
            updatePhotoPane(photoPane, order, updatedPhotoPaths);
        });

        HBox buttonBox = new HBox(attachPhotosButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 10, 50, 10));
        buttonBox.setSpacing(20);

        VBox layout = new VBox(10, photoPane, buttonBox);
        layout.setAlignment(Pos.CENTER);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));

        Scene scene = new Scene(scrollPane, 800, 600);
        photoStage.setScene(scene);
        photoStage.show();
    }

    /**
     * Updates the FlowPane with images and delete buttons dynamically.
     */
    private static void updatePhotoPane(FlowPane photoPane, Order order, List<String> photoPaths) {
        photoPane.getChildren().clear(); // Clear existing images

        for (String path : photoPaths) {
            StackPane photoContainer = new StackPane();

            try {
                // Load image safely
                Image image = new Image(new File(path).toURI().toString(), 150, 100, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setCursor(Cursor.HAND);

                // Click event for full-size preview
                imageView.setOnMouseClicked(e -> showFullSizeImage(path));

                // "Delete" Button
                Button deleteButton = new Button("X");
                deleteButton.setPrefSize(18, 16);
                deleteButton.setCursor(Cursor.HAND);
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 2px;");
                deleteButton.setOnAction(event -> {
                    ORDER_SERVICE.deletePhotoPath(path);
                    updatePhotoPane(photoPane, order, ORDER_SERVICE.getPhotoPaths(order.getId())); // Refresh view
                });

                StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT);
                StackPane.setMargin(deleteButton, new Insets(5, 5, 0, 0));

                photoContainer.getChildren().addAll(imageView, deleteButton);
                photoPane.getChildren().add(photoContainer);
            } catch (Exception e) {
                System.err.println("Error loading image: " + path);
            }
        }
    }

    public static void showFullSizeImage(String imagePath) {
        Stage imageStage = new Stage();
        imageStage.initModality(Modality.APPLICATION_MODAL);
        imageStage.setTitle("Photo Preview");

        ImageView fullImageView = new ImageView(new Image(new File(imagePath).toURI().toString()));
        fullImageView.setPreserveRatio(true);
        fullImageView.setFitWidth(800); // Adjust as needed
        fullImageView.setFitHeight(600);

        ScrollPane scrollPane = new ScrollPane(fullImageView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 850, 650);
        imageStage.setScene(scene);
        imageStage.showAndWait();
    }

    public static void attachPhotosToOrder(int orderId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles == null || selectedFiles.isEmpty()) return;

        String orderPhotosFolderName = "order_photos/" + orderId + "/";
        File orderPhotosFolder = new File(orderPhotosFolderName);

        boolean isPhotosFolderCreated = false;
        if (!orderPhotosFolder.exists()) {
            isPhotosFolderCreated = orderPhotosFolder.mkdirs();
        }

        if (isPhotosFolderCreated || orderPhotosFolder.exists()) {
            for (File selectedFile : selectedFiles) {
                try {
                    // Create unique file name (orderID + original name)
                    File destFile = new File(orderPhotosFolder, orderId + "_" + System.currentTimeMillis() + "_" + selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Save path to database
                    ORDER_SERVICE.savePhotoPath(orderId, destFile.getAbsolutePath());

                } catch (IOException e) {
                    ViewUtils.showAlert("Error", "Could not save photo: " + e.getMessage());
                }
            }

            ViewUtils.showAlert("Photos Attached", "Photos successfully attached to order.");
        } else {
            ViewUtils.showAlert("Error", "Could not create folder for Order's photos");
        }
    }

    public static void attachPhotosToOrder(int orderId, List<File> photoFiles) {
        String orderPhotosFolderName = "order_photos/" + orderId + "/";
        File orderPhotosFolder = new File(orderPhotosFolderName);

        boolean isPhotosFolderCreated = false;
        if (!orderPhotosFolder.exists()) {
            isPhotosFolderCreated = orderPhotosFolder.mkdirs();
        }

        if (isPhotosFolderCreated || orderPhotosFolder.exists()) {
            for (File selectedFile : photoFiles) {
                try {
                    // Create unique file name (orderID + original name)
                    File destFile = new File(orderPhotosFolder, orderId + "_" + System.currentTimeMillis() + "_" + selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Save path to database
                    ORDER_SERVICE.savePhotoPath(orderId, destFile.getAbsolutePath());

                } catch (IOException e) {
                    ViewUtils.showAlert("Error", "Could not save photo: " + e.getMessage());
                }
            }
//            TabUtils.showAlert("Photos Attached", "Photos successfully attached to order.");
        } else {
            ViewUtils.showAlert("Error", "Could not create folder for Order's photos");
        }
    }

}
