package me.skorb.view.tabs;

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
import me.skorb.entity.Part;
import me.skorb.service.PartService;
import me.skorb.view.ViewUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartsTab {

    private final PartService partService = new PartService();

    private Tab partsTab;

    private Label categoriesTableLabel;
    private TableView<String> categoriesTable;

    private Label partsTableLabel;
    private TableView<Part> partsTable;

    private GridPane categoryDetailsPane;
    private GridPane partDetailsPane;

    private Label categoryNameLabel;
    private TextField categoryNameField;

    private Label partNameLabel;
    private TextField partNameField;
    private Label partCategoryLabel;
    private ComboBox<String> partCategoryCombo;
    private Label partDescriptionLabel;
    private TextField partDescriptionField;
    private Label partPriceLabel;
    private TextField partPriceField;

    private Button addCategoryButton;
    private Button updateCategoryButton;
    private Button deleteCategoryButton;

    private Button addPartButton;
    private Button updatePartButton;
    private Button deletePartButton;

    public Tab createPartsTab() {
        partsTab = new Tab("Parts");

        initPartsTabContentView();

        return partsTab;
    }

    private void initPartsTabContentView() {
        initCategoriesTable();
        initCategoryDetailsView();
        initServicesTable();
        initServiceDetailsView();
        initLayout();
    }

    private void initCategoriesTable() {
        categoriesTableLabel = new Label("Parts Categories:");

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
        addCategoryButton = new Button("Add New Part Category");
        addCategoryButton.setCursor(Cursor.HAND);
        addCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        addCategoryButton.setOnAction(addServiceButtonActionEvent -> {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Add New Part Category");

            ButtonType saveButtonType = new ButtonType("Add Category", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Label newCategoryNameLabel = new Label("Part Category Name*:");
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
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean isNewCategoryNameValid = verifyRequiredFieldIsNotEmpty(newCategoryNameField);
                if (!isNewCategoryNameValid) {
                    showToastPopup("Service Category Name is required", ToastType.ERROR, categoryDetailsPane);
                    event.consume();
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
                partService.addCategory(newCategory);
                categoriesTable.getItems().add(newCategory); // Update UI
                categoriesTable.refresh();

                // Store current selected item, refresh the list of items and restore previous selection
                String selectedItem = partCategoryCombo.getSelectionModel().getSelectedItem();
                partCategoryCombo.getItems().clear();
                partCategoryCombo.getItems().addAll(partService.getAllPartCategories());
                partCategoryCombo.getSelectionModel().select(selectedItem);
            });
        });
    }

    private void initUpdateCategoryButton() {
        updateCategoryButton = new Button("Update Part Category");
        updateCategoryButton.setCursor(Cursor.HAND);
        updateCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateCategoryButton.setOnAction(event -> {
            String selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

            if (selectedCategory == null) {
                ViewUtils.showAlert("No Part Category Selected", "Please select a Part Category before deleting.");
                return;
            }

            boolean isCategoryNameValid = verifyRequiredFieldIsNotEmpty(categoryNameField);
            if (!isCategoryNameValid) {
                showToastPopup("Part category name is required", ToastType.ERROR, partDetailsPane);
                event.consume();
                return;
            }

            int selectedCategoryId = partService.getCategoryIdByCategoryName(selectedCategory);
            String newName = categoryNameField.getText();
            partService.updateCategory(selectedCategoryId, newName);

            refreshCategoriesTable();

            showToastPopup("Part Category has been successfully updated", ToastType.SUCCESS, partDetailsPane);
        });

        updateCategoryButton.setDisable(true);
    }

    private void initDeleteCategoryButton() {
        deleteCategoryButton = new Button("Delete Part Category");
        deleteCategoryButton.setCursor(Cursor.HAND);
        deleteCategoryButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteCategoryButton.setOnAction(addServiceButtonActionEvent -> {
            String selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();

            if (selectedCategory == null) {
                ViewUtils.showAlert("No Part Category Selected", "Please select a Part Category before deleting.");
                return;
            }

            String confirmationAlertMessage;
            boolean isSelectedCategoryUsed = partService.isCategoryUsed(selectedCategory);
            if (isSelectedCategoryUsed) {
                confirmationAlertMessage = String.format("Category %s still has parts.\n" +
                        "If you delete this category - it will delete all related parts.\n" +
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
                // If the user confirms, delete the Part Category
                partService.deleteCategory(selectedCategory);

                // Refresh Part Categories table
                refreshCategoriesTable();

                ViewUtils.showAlert("Part Category Deleted", "Part Category has been successfully deleted.");
            }
        });

        deleteCategoryButton.setDisable(true);
    }

    private void initServicesTable() {
        partsTableLabel = new Label("Parts:");

        partsTable = new TableView<>();
        VBox.setVgrow(partsTable, Priority.ALWAYS);
        partsTable.setPadding(new Insets(10, 10, 10, 10));
        partsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Part, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Part, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Part, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        partsTable.getColumns().addAll(nameColumn, categoryColumn, priceColumn);

        // Ensure all service table's columns have equal width
        partsTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / partsTable.getColumns().size();
            partsTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        // Load services data from the database and refresh the view
        refreshServicesTable();

        // Update order fields when the order is selected
        partsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedService) -> {
            if (selectedService != null) {
                partNameField.setDisable(false);
                partCategoryCombo.setDisable(false);
                partDescriptionField.setDisable(false);
                partPriceField.setDisable(false);
                addPartButton.setDisable(false);
                updatePartButton.setDisable(false);
                deletePartButton.setDisable(false);

                partNameField.setText(selectedService.getName());
                partCategoryCombo.setValue(selectedService.getCategoryName());
                partDescriptionField.setText(selectedService.getDescription());
                partPriceField.setText(selectedService.getPrice().toString());
            } else {
                partNameField.setText("");
                partCategoryCombo.setValue(null);
                partDescriptionField.setText("");
                partPriceField.setText("");

                partNameField.setDisable(true);
                partCategoryCombo.setDisable(true);
                partDescriptionField.setDisable(true);
                partPriceField.setDisable(true);
                addPartButton.setDisable(true);
                updatePartButton.setDisable(true);
                deletePartButton.setDisable(true);
            }
        });
    }

    private void initServiceDetailsView() {
        partNameLabel = new Label("Part Name*:");
        partNameField = new TextField();
        partNameField.setDisable(true);

        partCategoryLabel = new Label("Part Category*:");
        partCategoryCombo = new ComboBox<>();
        partCategoryCombo.getItems().addAll(partService.getAllPartCategories());
        partCategoryCombo.setDisable(true);

        partDescriptionLabel = new Label("Part Description:");
        partDescriptionField = new TextField();
        partDescriptionField.setDisable(true);

        partPriceLabel = new Label("Part Price*:");
        partPriceField = new TextField();
        partPriceField.setDisable(true);

        initAddPartButton();
        initUpdatePartButton();
        initDeleteServiceButton();

        partDetailsPane = new GridPane();
        partDetailsPane.setPadding(new Insets(20, 50, 20, 50));
        partDetailsPane.setHgap(10);
        partDetailsPane.setVgap(10);
        VBox.setVgrow(partDetailsPane, Priority.ALWAYS);
    }

    private void initAddPartButton() {
        addPartButton = new Button("Add New Part");
        addPartButton.setCursor(Cursor.HAND);
        addPartButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        addPartButton.setOnAction(event -> {
            Dialog<Part> dialog = new Dialog<>();
            dialog.setTitle("Add New Part");

            ButtonType saveButtonType = new ButtonType("Add Part", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            Label newPartNameLabel = new Label("Part Name*:");
            TextField newPartNameField = new TextField();
            Label newPartNameErrorLabel = new Label();
            newPartNameErrorLabel.setStyle("-fx-text-fill: red;");

            Label newPartCategoryLabel = new Label("Part Category*:");
            ComboBox<String> newPartCategoryCombo = new ComboBox<>();
            newPartCategoryCombo.getItems().addAll(partService.getAllPartCategories());
            Label newPartCategoryErrorLabel = new Label();
            newPartCategoryErrorLabel.setStyle("-fx-text-fill: red;");

            Label newPartDescriptionLabel = new Label("Part Description:");
            TextField newPartDescriptionField = new TextField();

            Label newPartPriceLabel = new Label("Part Price*:");
            TextField newPartPriceField = new TextField();
            Label newPartPriceErrorLabel = new Label();
            newPartPriceErrorLabel.setStyle("-fx-text-fill: red;");

            GridPane addPartMainPane = new GridPane();
            addPartMainPane.setHgap(10);
            addPartMainPane.setVgap(10);
            addPartMainPane.setPadding(new Insets(20, 20, 20, 20));

            addPartMainPane.add(newPartNameLabel, 0, 0);
            addPartMainPane.add(newPartNameField, 1, 0);
            addPartMainPane.add(newPartNameErrorLabel, 1, 1);
            addPartMainPane.add(newPartCategoryLabel, 0, 2);
            addPartMainPane.add(newPartCategoryCombo, 1, 2);
            addPartMainPane.add(newPartCategoryErrorLabel, 1, 3);
            addPartMainPane.add(newPartDescriptionLabel, 0, 4);
            addPartMainPane.add(newPartDescriptionField, 1, 4);
            addPartMainPane.add(newPartPriceLabel, 0, 5);
            addPartMainPane.add(newPartPriceField, 1, 5);
            addPartMainPane.add(newPartPriceErrorLabel, 1, 6);

            dialog.getDialogPane().setContent(addPartMainPane);

            Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
            saveButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
            saveButton.addEventFilter(ActionEvent.ACTION, saveButtonActionEvent -> {
//                boolean isNewServiceNameValid = TabUtils.verifyRequiredFieldIsNotEmpty(newPartNameField, newPartNameErrorLabel, "Part Name is a required field");
//                boolean isNewServicePriceValid = TabUtils.verifyRequiredFieldIsNotEmpty(newPartPriceField, newPartPriceErrorLabel, "Part Price is a required field");
//                boolean isNewServiceCategoryValid = TabUtils.isRequiredComboBoxChosen(newPartCategoryCombo, newPartCategoryErrorLabel, "Part Category is a required field");
//                if (!isNewServiceNameValid || !isNewServicePriceValid || !isNewServiceCategoryValid) {
//                    saveButtonActionEvent.consume();
//                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String newPartName = newPartNameField.getText();
                    String newPartCategoryName = newPartCategoryCombo.getValue();
                    String newPartDescription = newPartDescriptionField.getText();

                    String newPartPriceText = newPartPriceField.getText();
                    BigDecimal newPartPrice;
                    try {
                        if (newPartPriceText.isEmpty()) {
                            newPartPriceErrorLabel.setText("Price cannot be empty");
                            return null; // Prevent closing the dialog
                        }
                        newPartPrice = new BigDecimal(newPartPriceText);
                        if (newPartPrice.compareTo(BigDecimal.ZERO) < 0) {
                            newPartPriceErrorLabel.setText("Price cannot be negative");
                            return null; // Prevent closing the dialog
                        }

                        newPartPriceErrorLabel.setText("");
                    } catch (NumberFormatException e) {
                        // Show error message and prevent closing dialog
                        newPartPriceErrorLabel.setText("Invalid price: " + e.getMessage());
                        return null;
                    }

                    Part newPart = new Part();
                    newPart.setName(newPartName);
                    newPart.setCategoryName(newPartCategoryName);
                    newPart.setCategoryId(partService.getCategoryIdByCategoryName(newPartCategoryName));
                    newPart.setDescription(newPartDescription);
                    newPart.setPrice(newPartPrice);

                    return newPart;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(newPart -> {
                partService.addPart(newPart);
                partsTable.getItems().add(newPart); // Update UI
                partsTable.refresh();
            });
        });
    }

    private void initUpdatePartButton() {
        updatePartButton = new Button("Update Part");
        updatePartButton.setCursor(Cursor.HAND);
        updatePartButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updatePartButton.setOnAction(event -> {
            Part selectedPart = partsTable.getSelectionModel().getSelectedItem();

            if (selectedPart == null) {
                showToastPopup("Please select a Part before updating", ToastType.WARNING, partDetailsPane);
                event.consume();
                return;
            }

            boolean isPartNameValid = verifyRequiredFieldIsNotEmpty(partNameField);
            boolean isPartPriceValid = ViewUtils.verifyRequiredFieldIsNotEmpty(partPriceField)
                    && ViewUtils.isPriceValid(partPriceField, partPriceLabel);

            List<String> errors = new ArrayList<>();
            if (!isPartNameValid) {
                errors.add("Part Name is a required field.");
            }
            if (partCategoryCombo.getValue() == null) {
                errors.add("Part Category is a required field.");
            }
            if (!isPartPriceValid) {
                errors.add("Please provide a valid price");
            }
            if (!errors.isEmpty()) {
                showToastPopup(errors, ToastType.ERROR, partDetailsPane);
                event.consume();
                return;
            }

            selectedPart.setName(partNameField.getText());
            int partCategoryId = partService.getCategoryIdByCategoryName(partCategoryCombo.getValue());
            selectedPart.setCategoryId(partCategoryId);
            selectedPart.setCategoryName(partCategoryCombo.getValue());
            selectedPart.setDescription(partDescriptionField.getText());
            selectedPart.setPrice(new BigDecimal(partPriceField.getText()));

            partService.updatePart(selectedPart);

            // Refresh services table
            refreshServicesTable();

            ViewUtils.showAlert("Part Updated", "Part details have been successfully updated.");
        });

        updatePartButton.setDisable(true);
    }

    private void initDeleteServiceButton() {
        deletePartButton = new Button("Delete Part");
        deletePartButton.setCursor(Cursor.HAND);
        deletePartButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deletePartButton.setOnAction(event -> {
            Part selectedPart = partsTable.getSelectionModel().getSelectedItem();

            if (selectedPart == null) {
                ViewUtils.showAlert("No Part Selected", "Please select a Part before deleting.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this Part?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the Part
                partService.deletePart(selectedPart);

                refreshServicesTable();

                ViewUtils.showAlert("Part Deleted", "Part has been successfully deleted.");
            }
        });

        deletePartButton.setDisable(true);
    }

    private void refreshCategoriesTable() {
        // Load part categories data from the database and refresh the view
        List<String> partCategories = partService.getAllPartCategories();
        ObservableList<String> categoriesList = FXCollections.observableArrayList(partCategories);
        categoriesTable.setItems(categoriesList);
        categoriesTable.refresh();
    }

    private void refreshServicesTable() {
        // Load parts data from the database and refresh the view
        List<Part> parts = partService.getAllParts();
        ObservableList<Part> partList = FXCollections.observableArrayList(parts);
        partsTable.setItems(partList);
        partsTable.refresh();
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
        HBox partsButtonBox = new HBox(addPartButton, deletePartButton);
        partsButtonBox.setAlignment(Pos.CENTER);
        partsButtonBox.setPadding(new Insets(10, 10, 50, 10));
        partsButtonBox.setSpacing(20);

        VBox partsTableColumn = new VBox(10);
        partsTableColumn.setPadding(new Insets(20));
        partsTableColumn.getChildren().addAll(partsTable, partsButtonBox);
        HBox.setHgrow(partsTableColumn, Priority.ALWAYS);

        // --- Bottom Right Column: Service Details ---
        VBox partDetailsColumn = new VBox(10);
        partDetailsColumn.setPadding(new Insets(20));
        partDetailsColumn.getChildren().addAll(
                partNameLabel,
                partNameField,
                partCategoryLabel,
                partCategoryCombo,
                partDescriptionLabel,
                partDescriptionField,
                partPriceLabel,
                partPriceField,
                updatePartButton);
        HBox.setHgrow(partDetailsColumn, Priority.ALWAYS);

        // --- Main Top Layout: Arrange columns in an HBox ---
        HBox partsContent = new HBox(30, partsTableColumn, partDetailsColumn);
        partsContent.setPadding(new Insets(20));
        partsContent.setAlignment(Pos.TOP_CENTER);

        partsTableColumn.prefWidthProperty().bind(partsContent.widthProperty().multiply(0.7));
        partDetailsColumn.prefWidthProperty().bind(partsContent.widthProperty().multiply(0.3));

        // mainPane is a GridPane to control height distribution between top part and bottom part
        GridPane mainPane = new GridPane();
        mainPane.setVgap(10);
        mainPane.add(categoriesContent, 0, 0);
        mainPane.add(partsContent, 0, 1);

        // Make rows to take corresponding heights
        RowConstraints categoriesTablesRowConstraint = new RowConstraints();
        categoriesTablesRowConstraint.setPercentHeight(50);

        RowConstraints servicesTableRowConstraint = new RowConstraints();
        servicesTableRowConstraint.setPercentHeight(50);
        servicesTableRowConstraint.setVgrow(Priority.ALWAYS); // Ensure it expands when window resizes

        mainPane.getRowConstraints().addAll(categoriesTablesRowConstraint, servicesTableRowConstraint);

        GridPane.setVgrow(categoriesContent, Priority.ALWAYS);
        GridPane.setVgrow(partsContent, Priority.ALWAYS);
        GridPane.setHgrow(categoriesContent, Priority.ALWAYS);
        GridPane.setHgrow(partsContent, Priority.ALWAYS);

        // Ensure mainPane itself stretches across the full tab width
        mainPane.setPrefWidth(Double.MAX_VALUE);

        partsTab.setContent(mainPane);
    }

}
