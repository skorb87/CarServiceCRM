package me.skorb.view.screens;

import static me.skorb.view.ViewUtils.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import me.skorb.entity.Service;
import me.skorb.service.ServiceService;
import me.skorb.view.ViewUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ServicesView extends GridPane {

    private final ServiceService serviceService = new ServiceService();

    private Label categoriesTableLabel;
    private TableView<String> categoriesTable;

    private Label serviceTableLabel;
    private TableView<Service> servicesTable;

    private GridPane categoryDetailsPane;
    private GridPane serviceDetailsPane;

    private Label categoryNameLabel;
    private TextField categoryNameField;

    private Label serviceNameLabel;
    private TextField serviceNameField;
    private Label serviceCategoryLabel;
    private ComboBox<String> serviceCategoryCombo;
    private Label serviceDescriptionLabel;
    private TextField serviceDescriptionField;
    private Label servicePriceLabel;
    private TextField servicePriceField;

    private Button addCategoryButton;
    private Button updateCategoryButton;
    private Button deleteCategoryButton;

    private Button addServiceButton;
    private Button updateServiceButton;
    private Button deleteServiceButton;

    public ServicesView() {
        initCategoriesTable();
        initCategoryDetailsView();
        initServicesTable();
        initServiceDetailsView();
        initLayout();
    }

    private void initCategoriesTable() {
        categoriesTableLabel = new Label("Service Categories:");

        categoriesTable = new TableView<>();
        VBox.setVgrow(categoriesTable, Priority.ALWAYS);
        categoriesTable.setPadding(new Insets(10, 10, 10, 10));
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));

        categoriesTable.getColumns().addAll(nameColumn);

        // Ensure all categories table's columns have equal width
        categoriesTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / categoriesTable.getColumns().size();
            categoriesTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        refreshCategoriesTable();

        // Update category fields when the category is selected
        categoriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCategory) -> {
            if (selectedCategory != null) {
                categoryNameField.setDisable(false);
                updateCategoryButton.setDisable(false);
                deleteCategoryButton.setDisable(false);

                categoryNameField.setText(selectedCategory);
            } else {
                categoryNameField.setText("");

                categoryNameField.setDisable(true);
                updateCategoryButton.setDisable(true);
                deleteCategoryButton.setDisable(true);
            }
        });
    }

    private void initCategoryDetailsView() {
        categoryNameLabel = new Label("Category Name:");
        categoryNameField = new TextField();
        categoryNameField.setDisable(true);

        initAddCategoryButton();
        initUpdateCategoryButton();
        initDeleteCategoryButton();

        categoryDetailsPane = new GridPane();
        categoryDetailsPane.setPadding(new Insets(20, 50, 20, 50));
        categoryDetailsPane.setHgap(10);
        categoryDetailsPane.setVgap(10);
        VBox.setVgrow(categoryDetailsPane, Priority.ALWAYS);
    }

    private void initAddCategoryButton() {
        addCategoryButton = new Button("Add New Service Category");
        addCategoryButton.setCursor(Cursor.HAND);
        addCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        addCategoryButton.setOnAction(addServiceButtonActionEvent -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Add New Service Category");

            ButtonType saveButtonType = new ButtonType("Add Category", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Label newCategoryNameLabel = new Label("Service Category Name*:");
            TextField newCategoryNameField = new TextField();
            Label newCategoryNameErrorLabel = new Label();
            newCategoryNameErrorLabel.setStyle("-fx-text-fill: red;");

            GridPane addCategoryMainPane = new GridPane();
            addCategoryMainPane.setHgap(10);
            addCategoryMainPane.setVgap(10);
            addCategoryMainPane.setPadding(new Insets(20, 150, 10, 10));

            addCategoryMainPane.add(newCategoryNameLabel, 0, 0);
            addCategoryMainPane.add(newCategoryNameField, 1, 0);
            addCategoryMainPane.add(newCategoryNameErrorLabel, 1, 1);

            dialog.getDialogPane().setContent(addCategoryMainPane);

            Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
            saveButton.addEventFilter(ActionEvent.ACTION, saveButtonActionEvent -> {
                boolean isNewCategoryNameValid = verifyRequiredFieldIsNotEmpty(newCategoryNameField);
                if (!isNewCategoryNameValid) {
                    showToastPopup("Service Category Name is required", ToastType.ERROR, serviceDetailsPane);
                    saveButtonActionEvent.consume();
                    return;
                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    return newCategoryNameField.getText();
                }
                return null;
            });

            dialog.showAndWait().ifPresent(newCategory -> {
                serviceService.addCategory(newCategory);
                categoriesTable.getItems().add(newCategory); // Update UI
                categoriesTable.refresh();

                // Store current selected item, refresh the list of items and restore previous selection
                String selectedItem = serviceCategoryCombo.getSelectionModel().getSelectedItem();
                serviceCategoryCombo.getItems().clear();
                serviceCategoryCombo.getItems().addAll(serviceService.getAllServiceCategories());
                serviceCategoryCombo.getSelectionModel().select(selectedItem);
            });
        });
    }

    private void initUpdateCategoryButton() {
        updateCategoryButton = new Button("Update Service Category");
        updateCategoryButton.setCursor(Cursor.HAND);
        updateCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateCategoryButton.setOnAction(addServiceButtonActionEvent -> {
            String selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

            if (selectedCategory == null) {
                showToastPopup("Please select a Service Category for updating", ToastType.WARNING, serviceDetailsPane);
                addServiceButtonActionEvent.consume();
                return;
            }

            boolean isCategoryNameValid = verifyRequiredFieldIsNotEmpty(categoryNameField);
            if (!isCategoryNameValid) {
                showToastPopup("Service Category Name is required", ToastType.ERROR, serviceDetailsPane);
                addServiceButtonActionEvent.consume();
                return;
            }

            int selectedCategoryId = serviceService.getCategoryIdByCategoryName(selectedCategory);
            String newName = categoryNameField.getText();
            serviceService.updateCategory(selectedCategoryId, newName);

            refreshCategoriesTable();

            ViewUtils.showAlert("Service Category Updated", "Service Category has been successfully updated.");
        });

        updateCategoryButton.setDisable(true);
    }

    private void initDeleteCategoryButton() {
        deleteCategoryButton = new Button("Delete Service Category");
        deleteCategoryButton.setCursor(Cursor.HAND);
        deleteCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteCategoryButton.setOnAction(addServiceButtonActionEvent -> {
            String selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

            if (selectedCategory == null) {
                ViewUtils.showAlert("No Service Category Selected", "Please select a Service Category before deleting.");
                return;
            }

            String confirmationAlertMessage;
            boolean isSelectedCategoryUsed = serviceService.isCategoryUsed(selectedCategory);
            if (isSelectedCategoryUsed) {
                confirmationAlertMessage = String.format("Category %s still has services.\n" +
                        "If you delete this category - it will delete all related services.\n" +
                        "This action cannot be undone.", selectedCategory);
            } else {
                confirmationAlertMessage = "This action cannot be undone.";
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText(String.format("Are you sure you want to delete %s Category?", selectedCategory));
            confirmationAlert.setContentText(confirmationAlertMessage);

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the Service Category
                serviceService.deleteCategory(selectedCategory);

                // Refresh Service Categories table
                refreshCategoriesTable();

                ViewUtils.showAlert("Service Category Deleted", "Service Category has been successfully deleted.");
            }
        });

        deleteCategoryButton.setDisable(true);
    }

    private void initServicesTable() {
        serviceTableLabel = new Label("Services:");

        servicesTable = new TableView<>();
        VBox.setVgrow(servicesTable, Priority.ALWAYS);
        servicesTable.setPadding(new Insets(10, 10, 10, 10));
        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Service, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Service, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Service, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        servicesTable.getColumns().addAll(nameColumn, categoryColumn, priceColumn);

        // Ensure all service table's columns have equal width
        servicesTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / servicesTable.getColumns().size();
            servicesTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        // Load services data from the database and refresh the view
        refreshServicesTable();

        // Update order fields when the order is selected
        servicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedService) -> {
            if (selectedService != null) {
                serviceNameField.setDisable(false);
                serviceCategoryCombo.setDisable(false);
                serviceDescriptionField.setDisable(false);
                servicePriceField.setDisable(false);
                addServiceButton.setDisable(false);
                updateServiceButton.setDisable(false);
                deleteServiceButton.setDisable(false);

                serviceNameField.setText(selectedService.getName());
                serviceCategoryCombo.setValue(selectedService.getCategoryName());
                serviceDescriptionField.setText(selectedService.getDescription());
                servicePriceField.setText(selectedService.getPrice().toString());
            } else {
                serviceNameField.setText("");
                serviceCategoryCombo.setValue(null);
                serviceDescriptionField.setText("");
                servicePriceField.setText("");

                serviceNameField.setDisable(true);
                serviceCategoryCombo.setDisable(true);
                serviceDescriptionField.setDisable(true);
                servicePriceField.setDisable(true);
                addServiceButton.setDisable(true);
                updateServiceButton.setDisable(true);
                deleteServiceButton.setDisable(true);
            }
        });
    }

    private void initServiceDetailsView() {
        serviceNameLabel = new Label("Service Name:");
        serviceNameField = new TextField();
        serviceNameField.setDisable(true);

        serviceCategoryLabel = new Label("Service Category:");
        serviceCategoryCombo = new ComboBox<>();
        serviceCategoryCombo.getItems().addAll(serviceService.getAllServiceCategories());
        serviceCategoryCombo.setDisable(true);

        serviceDescriptionLabel = new Label("Service Description:");
        serviceDescriptionField = new TextField();
        serviceDescriptionField.setDisable(true);

        servicePriceLabel = new Label("Service Price:");
        servicePriceField = new TextField();
        servicePriceField.setDisable(true);

        initAddServiceButton();
        initUpdateServiceButton();
        initDeleteServiceButton();

        serviceDetailsPane = new GridPane();
        serviceDetailsPane.setPadding(new Insets(20, 50, 20, 50));
        serviceDetailsPane.setHgap(10);
        serviceDetailsPane.setVgap(10);
        VBox.setVgrow(serviceDetailsPane, Priority.ALWAYS);
    }

    private void initAddServiceButton() {
        addServiceButton = new Button("Add New Service");
        addServiceButton.setCursor(Cursor.HAND);
        addServiceButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        addServiceButton.setOnAction(addServiceButtonActionEvent -> {
            Dialog<Service> dialog = new Dialog<>();
            dialog.setTitle("Add New Service");

            ButtonType saveButtonType = new ButtonType("Add Service", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Label newServiceNameLabel = new Label("Service Name*:");
            TextField newServiceNameField = new TextField();
            Label newServiceNameErrorLabel = new Label();
            newServiceNameErrorLabel.setStyle("-fx-text-fill: red;");

            Label newServiceCategoryLabel = new Label("Service Category*:");
            ComboBox<String> newServiceCategoryCombo = new ComboBox<>();
            newServiceCategoryCombo.getItems().addAll(serviceService.getAllServiceCategories());
            Label newServiceCategoryErrorLabel = new Label();
            newServiceCategoryErrorLabel.setStyle("-fx-text-fill: red;");

            Label newServiceDescriptionLabel = new Label("Service Description:");
            TextField newServiceDescriptionField = new TextField();

            Label newServicePriceLabel = new Label("Service Price*:");
            TextField newServicePriceField = new TextField();
            Label newServicePriceErrorLabel = new Label();
            newServicePriceErrorLabel.setStyle("-fx-text-fill: red;");

            GridPane addServiceMainPane = new GridPane();
            addServiceMainPane.setHgap(10);
            addServiceMainPane.setVgap(10);
            addServiceMainPane.setPadding(new Insets(20, 150, 10, 10));

            addServiceMainPane.add(newServiceNameLabel, 0, 0);
            addServiceMainPane.add(newServiceNameField, 1, 0);
            addServiceMainPane.add(newServiceNameErrorLabel, 1, 1);
            addServiceMainPane.add(newServiceCategoryLabel, 0, 2);
            addServiceMainPane.add(newServiceCategoryCombo, 1, 2);
            addServiceMainPane.add(newServiceCategoryErrorLabel, 1, 3);
            addServiceMainPane.add(newServiceDescriptionLabel, 0, 4);
            addServiceMainPane.add(newServiceDescriptionField, 1, 4);
            addServiceMainPane.add(newServicePriceLabel, 0, 5);
            addServiceMainPane.add(newServicePriceField, 1, 5);
            addServiceMainPane.add(newServicePriceErrorLabel, 1, 6);

            dialog.getDialogPane().setContent(addServiceMainPane);

            Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
            saveButton.addEventFilter(ActionEvent.ACTION, saveButtonActionEvent -> {
//                boolean isNewServiceNameValid = TabUtils.verifyRequiredFieldIsNotEmpty(newServiceNameField, newServiceNameErrorLabel, "Service Name is a required field");
//                boolean isNewServicePriceValid = TabUtils.verifyRequiredFieldIsNotEmpty(newServicePriceField, newServicePriceErrorLabel, "Service Price is a required field");
//                boolean isNewServiceCategoryValid = TabUtils.isRequiredComboBoxChosen(newServiceCategoryCombo, newServiceCategoryErrorLabel, "Service Category is a required field");
//                if (!isNewServiceNameValid || !isNewServicePriceValid || !isNewServiceCategoryValid) {
//                    saveButtonActionEvent.consume();
//                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String newServiceName = newServiceNameField.getText();
                    String newServiceCategoryName = newServiceCategoryCombo.getValue();
                    String newServiceDescription = newServiceDescriptionField.getText();

                    String newServicePriceText = newServicePriceField.getText();
                    BigDecimal newServicePrice;
                    try {
                        if (newServicePriceText.isEmpty()) {
                            newServicePriceErrorLabel.setText("Price cannot be empty");
                            return null; // Prevent closing the dialog
                        }
                        newServicePrice = new BigDecimal(newServicePriceText);
                        if (newServicePrice.compareTo(BigDecimal.ZERO) < 0) {
                            newServicePriceErrorLabel.setText("Price cannot be negative");
                            return null; // Prevent closing the dialog
                        }

                        newServicePriceErrorLabel.setText("");
                    } catch (NumberFormatException e) {
                        // Show error message and prevent closing dialog
                        newServicePriceErrorLabel.setText("Invalid price: " + e.getMessage());
                        return null;
                    }

                    Service newService = new Service();
                    newService.setName(newServiceName);
                    newService.setCategoryName(newServiceCategoryName);
                    newService.setCategoryId(serviceService.getCategoryIdByCategoryName(newServiceCategoryName));
                    newService.setDescription(newServiceDescription);
                    newService.setPrice(newServicePrice);

                    return newService;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(newService -> {
                serviceService.addService(newService);
                servicesTable.getItems().add(newService); // Update UI
                servicesTable.refresh();
            });
        });
    }

    private void initUpdateServiceButton() {
        updateServiceButton = new Button("Update Service");
        updateServiceButton.setCursor(Cursor.HAND);
        updateServiceButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateServiceButton.setOnAction(event -> {
            Service selectedService = servicesTable.getSelectionModel().getSelectedItem();

            if (selectedService == null) {
                ViewUtils.showAlert("No Service Selected", "Please select a Service before updating.");
                return;
            }

            boolean isServiceNameValid = verifyRequiredFieldIsNotEmpty(serviceNameField);
            boolean isServiceCategoryChosen = isRequiredComboBoxChosen(serviceCategoryCombo, serviceCategoryLabel);
            boolean isServicePriceValid = verifyRequiredFieldIsNotEmpty(servicePriceField)
                    && isPriceValid(servicePriceField, servicePriceLabel);

            if (!isServiceNameValid) {
                ViewUtils.showAlert("Service Was Not Updated", "Service Name is a required field.");
                return;
            }

            if (!isServiceCategoryChosen) {
                ViewUtils.showAlert("Service Was Not Updated", "Service Category is a required field.");
                return;
            }

            if (!isServicePriceValid) {
                ViewUtils.showAlert("Service Was Not Updated", "Please provide a valid price");
                return;
            }

            selectedService.setName(serviceNameField.getText());
            int serviceCategoryId = serviceService.getCategoryIdByCategoryName(serviceCategoryCombo.getValue());
            selectedService.setCategoryId(serviceCategoryId);
            selectedService.setCategoryName(serviceCategoryCombo.getValue());
            selectedService.setDescription(serviceDescriptionField.getText());
            selectedService.setPrice(new BigDecimal(servicePriceField.getText()));

            serviceService.updateService(selectedService);

            // Refresh services table
            refreshServicesTable();

            ViewUtils.showAlert("Service Updated", "Service details have been successfully updated.");
        });

        updateServiceButton.setDisable(true);
    }

    private void initDeleteServiceButton() {
        deleteServiceButton = new Button("Delete Service");
        deleteServiceButton.setCursor(Cursor.HAND);
        deleteServiceButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteServiceButton.setOnAction(event -> {
            Service selectedService = servicesTable.getSelectionModel().getSelectedItem();

            if (selectedService == null) {
                ViewUtils.showAlert("No Service Selected", "Please select a Service before deleting.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this Service?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the Service
                serviceService.deleteService(selectedService);

                refreshServicesTable();

                ViewUtils.showAlert("Service Deleted", "Service has been successfully deleted.");
            }
        });

        deleteServiceButton.setDisable(true);
    }

    private void refreshCategoriesTable() {
        // Load service categories data from the database and refresh the view
        List<String> serviceCategories = serviceService.getAllServiceCategories();
        ObservableList<String> categoriesList = FXCollections.observableArrayList(serviceCategories);
        categoriesTable.setItems(categoriesList);
        categoriesTable.refresh();
    }

    private void refreshServicesTable() {
        // Load services data from the database and refresh the view
        List<Service> services = serviceService.getAllServices();
        ObservableList<Service> serviceList = FXCollections.observableArrayList(services);
        servicesTable.setItems(serviceList);
        servicesTable.refresh();
    }

    private void initLayout() {
        // --- Top Left Column: Categories Table With Buttons ---
        HBox categoriesButtonBox = new HBox(addCategoryButton, deleteCategoryButton);
        categoriesButtonBox.setAlignment(Pos.CENTER);
        categoriesButtonBox.setPadding(new Insets(10, 10, 50, 10));
        categoriesButtonBox.setSpacing(20);

        VBox categoriesTableColumn = new VBox(10);
        categoriesTableColumn.setPadding(new Insets(20));
        categoriesTableColumn.getChildren().addAll(categoriesTable, categoriesButtonBox);
        HBox.setHgrow(categoriesTableColumn, Priority.ALWAYS);

        // --- Top Right Column: Category Details ---
        VBox categoryDetailsColumn = new VBox(10);
        categoryDetailsColumn.setPadding(new Insets(20));
        categoryDetailsColumn.getChildren().addAll(
                categoryNameLabel,
                categoryNameField,
                updateCategoryButton);
        HBox.setHgrow(categoryDetailsColumn, Priority.ALWAYS);

        // --- Main Top Layout: Arrange columns in an HBox ---
        HBox categoriesContent = new HBox(30, categoriesTableColumn, categoryDetailsColumn);
        categoriesContent.setPadding(new Insets(20));
        categoriesContent.setAlignment(Pos.TOP_CENTER);

        categoriesTableColumn.prefWidthProperty().bind(categoriesContent.widthProperty().multiply(0.7));
        categoryDetailsColumn.prefWidthProperty().bind(categoriesContent.widthProperty().multiply(0.3));

        // --- Bottom Left Column: Services Table ---
        HBox servicesButtonBox = new HBox(addServiceButton, deleteServiceButton);
        servicesButtonBox.setAlignment(Pos.CENTER);
        servicesButtonBox.setPadding(new Insets(10, 10, 50, 10));
        servicesButtonBox.setSpacing(20);

        VBox servicesTableColumn = new VBox(10);
        servicesTableColumn.setPadding(new Insets(20));
        servicesTableColumn.getChildren().addAll(servicesTable, servicesButtonBox);
        HBox.setHgrow(servicesTableColumn, Priority.ALWAYS);

        // --- Bottom Right Column: Service Details ---
        VBox serviceDetailsColumn = new VBox(10);
        serviceDetailsColumn.setPadding(new Insets(20));
        serviceDetailsColumn.getChildren().addAll(
                serviceNameLabel,
                serviceNameField,
                serviceCategoryLabel,
                serviceCategoryCombo,
                serviceDescriptionLabel,
                serviceDescriptionField,
                servicePriceLabel,
                servicePriceField,
                updateServiceButton);
        HBox.setHgrow(serviceDetailsColumn, Priority.ALWAYS);

        // --- Main Top Layout: Arrange columns in an HBox ---
        HBox servicesContent = new HBox(30, servicesTableColumn, serviceDetailsColumn);
        servicesContent.setPadding(new Insets(20));
        servicesContent.setAlignment(Pos.TOP_CENTER);

        servicesTableColumn.prefWidthProperty().bind(servicesContent.widthProperty().multiply(0.7));
        serviceDetailsColumn.prefWidthProperty().bind(servicesContent.widthProperty().multiply(0.3));

        // mainPane is a GridPane to control height distribution between top part and bottom part
        setVgap(10);
        add(categoriesContent, 0, 0);
        add(servicesContent, 0, 1);

        // Make rows to take corresponding heights
        RowConstraints categoriesTablesRowConstraint = new RowConstraints();
        categoriesTablesRowConstraint.setPercentHeight(50);

        RowConstraints servicesTableRowConstraint = new RowConstraints();
        servicesTableRowConstraint.setPercentHeight(50);
        servicesTableRowConstraint.setVgrow(Priority.ALWAYS); // Ensure it expands when window resizes

        getRowConstraints().addAll(categoriesTablesRowConstraint, servicesTableRowConstraint);

        GridPane.setVgrow(categoriesContent, Priority.ALWAYS);
        GridPane.setVgrow(servicesContent, Priority.ALWAYS);
        GridPane.setHgrow(categoriesContent, Priority.ALWAYS);
        GridPane.setHgrow(servicesContent, Priority.ALWAYS);

        // Ensure mainPane itself stretches across the full tab width
        setPrefWidth(Double.MAX_VALUE);
    }

}
