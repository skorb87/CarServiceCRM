package me.skorb.view.tabs;

import static me.skorb.view.ViewUtils.*;

import javafx.application.Platform;
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
import javafx.stage.Modality;
import me.skorb.entity.Customer;
import me.skorb.entity.Vehicle;
import me.skorb.service.CustomerService;
import me.skorb.service.MakeService;
import me.skorb.service.ModelService;
import me.skorb.service.VehicleService;
import me.skorb.view.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CustomersTab {

    private final CustomerService customerService = new CustomerService();
    private final VehicleService vehicleService = new VehicleService();

    private final MakeService makeService = new MakeService();
    private final ModelService modelService = new ModelService();

    private Tab customersTab;

    private HBox topPane;
    private TableView<Customer> customerTable;
    private GridPane detailsPane;

    private Button newCustomerButton;
    private Button findCustomerButton;

    private Label firstNameLabel;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField addressField;
    private TextField cityField;
    private ComboBox<Customer.State> stateCombo;
    private TextField postalCodeField;
    private Label phoneLabel;
    private TextField phoneField;
    private TextField emailField;

    private Label vinLabel;
    private TextField vinField;
    private TextField licensePlateField;
    private Label makeLabel;
    private ComboBox<String> makeCombo;
    private Label modelLabel;
    private ComboBox<String> modelCombo;
    private Label yearLabel;
    private ComboBox<String> yearCombo;

    private Label vehicleTableLabel;
    private TableView<Vehicle> vehicleTable;

    private Button updateCustomerButton;
    private Button deleteCustomerButton;
    private Button addVehicleButton;
    private Button updateVehicleButton;
    private Button deleteVehicleButton;

    public Tab createCustomersTab() {
        customersTab = new Tab("Customers");

        initCustomersTabContentView();

        return customersTab;
    }

    private void initCustomersTabContentView() {
        initTopPane();
        initCustomerTable();
        initDetailsPane();
        initUpdateCustomerButton();
        initDeleteCustomerButton();
        initVehicleListTable();
        initUpdateVehicleButton();
        initDeleteVehicleButton();
        initAddVehicleButton();
        initLayout();
    }

    private void initTopPane() {
        newCustomerButton = new Button("+ New Customer");
        newCustomerButton.setCursor(Cursor.HAND);
        newCustomerButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold;");
        newCustomerButton.setOnAction(e -> showNewCustomerDialog(customerTable));

        findCustomerButton = new Button("Find Customer");
        findCustomerButton.setCursor(Cursor.HAND);
        findCustomerButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        findCustomerButton.setOnAction(event -> {
            Dialog<Customer> dialog = new Dialog<>();
            dialog.setTitle("Find Customer");
            dialog.initModality(Modality.APPLICATION_MODAL);

            ButtonType findButtonType = new ButtonType("Find", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = ButtonType.CANCEL;
            dialog.getDialogPane().getButtonTypes().addAll(findButtonType, cancelButtonType);

            // --- Radio Buttons ---
            ToggleGroup searchModeGroup = new ToggleGroup();
            RadioButton nameRadio = new RadioButton("Search by Name");
            RadioButton phoneRadio = new RadioButton("Search by Phone");
            nameRadio.setToggleGroup(searchModeGroup);
            phoneRadio.setToggleGroup(searchModeGroup);
            nameRadio.setSelected(true); // Default mode

            HBox radioBox = new HBox(20, nameRadio, phoneRadio);
            radioBox.setAlignment(Pos.CENTER_LEFT);

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("First Name");
            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Last Name");
            TextField phoneField = new TextField();
            phoneField.setPromptText("Phone");
            phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                String digitsOnly = newValue.replaceAll("[^0-9]", ""); // Remove non-digit characters

                // Limit input to 10 digits max
                if (digitsOnly.length() > 10) {
                    digitsOnly = digitsOnly.substring(0, 10);
                }

                // Prevent infinite loop: Only update if text actually changed
                if (!newValue.equals(digitsOnly)) {
                    int caretPosition = phoneField.getCaretPosition(); // Save caret position
                    phoneField.setText(digitsOnly);

                    // Adjust caret position if needed
                    if (caretPosition > digitsOnly.length()) {
                        caretPosition = digitsOnly.length();
                    }
                    phoneField.positionCaret(caretPosition);
                }
            });

            GridPane form = new GridPane();
            form.setPadding(new Insets(20));
            form.setHgap(10);
            form.setVgap(10);

            form.add(radioBox, 0, 0, 2, 1);
            form.add(firstNameField, 0, 1);
            form.add(lastNameField, 1, 1);
            form.add(phoneField, 0, 2);

            dialog.getDialogPane().setContent(form);

            // --- Disable/Enable fields depending on radio selection ---
            searchModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
                boolean isNameMode = newToggle == nameRadio;
                firstNameField.setDisable(!isNameMode);
                lastNameField.setDisable(!isNameMode);
                phoneField.setDisable(isNameMode);
            });

            // Initialize state (name search enabled by default)
            firstNameField.setDisable(false);
            lastNameField.setDisable(false);
            phoneField.setDisable(true);

            Button findButton = (Button) dialog.getDialogPane().lookupButton(findButtonType);
            findButton.setCursor(Cursor.HAND);
            findButton.setStyle(
                    "-fx-background-color: #2c7be5; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-background-radius: 6;"
            );

            Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
            cancelButton.setCursor(Cursor.HAND);
            cancelButton.setStyle("-fx-background-radius: 6;");

            // User input validation
            findButton.addEventFilter(ActionEvent.ACTION, findButtonEvent -> {
                if (nameRadio.isSelected() &&
                        (firstNameField.getText().trim().isEmpty() && lastNameField.getText().trim().isEmpty())) {
                    showToastPopup("Enter first or last name", ToastType.ERROR, dialog.getDialogPane());
                    findButtonEvent.consume();
                } else if (phoneRadio.isSelected() && phoneField.getText().trim().isEmpty()) {
                    showToastPopup("Enter phone number", ToastType.ERROR, dialog.getDialogPane());
                    findButtonEvent.consume();
                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == findButtonType) {
                    if (nameRadio.isSelected()) {
                        List<Customer> customers = customerService.findByName(
                                firstNameField.getText().trim(),
                                lastNameField.getText().trim()
                        );
                        return customers.isEmpty() ? null : customers.get(0);
                    } else {
                        return customerService.findByPhone(phoneField.getText().trim()).orElse(null);
                    }
                }
                return null;
            });

            Optional<Customer> result = dialog.showAndWait();
            result.ifPresent(foundCustomer -> {
                Platform.runLater(() -> {
                    customerTable.getSelectionModel().select(foundCustomer);
                    customerTable.scrollTo(foundCustomer);
                });
            });
        });

        topPane = new HBox(15);
        topPane.setPadding(new Insets(15, 50, 10, 50));
        topPane.getChildren().addAll(newCustomerButton, findCustomerButton);
    }

    private void initDetailsPane() {
        // --- Customer Information Fields ---
        firstNameLabel = new Label("First Name*:");
        firstNameField = new TextField();
        firstNameField.setDisable(true);

        lastNameField = new TextField();
        lastNameField.setDisable(true);

        addressField = new TextField();
        addressField.setDisable(true);

        cityField = new TextField();
        cityField.setDisable(true);

        stateCombo = new ComboBox<>();
        stateCombo.setItems(FXCollections.observableArrayList(Customer.State.values()));
        stateCombo.setDisable(true);

        postalCodeField = new TextField();
        postalCodeField.setDisable(true);

        phoneLabel = new Label("Phone*:");
        phoneField = new TextField();
//        phoneField.setPromptText("(555) 111-3333");
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("[^0-9]", ""); // Remove non-digit characters

            // Limit input to 10 digits max
            if (digitsOnly.length() > 10) {
                digitsOnly = digitsOnly.substring(0, 10);
            }

            // Prevent infinite loop: Only update if text actually changed
            if (!newValue.equals(digitsOnly)) {
                int caretPosition = phoneField.getCaretPosition(); // Save caret position
                phoneField.setText(digitsOnly);

                // Adjust caret position if needed
                if (caretPosition > digitsOnly.length()) {
                    caretPosition = digitsOnly.length();
                }
                phoneField.positionCaret(caretPosition);
            }
        });
        phoneField.setDisable(true);

        emailField = new TextField();
        emailField.setDisable(true);

        vinLabel = new Label("VIN Number*:");
        vinField = new TextField();
        vinField.textProperty().addListener((observable, oldValue, newValue) -> {
            String allowedCharsOnly = newValue.toUpperCase().replaceAll("[^A-HJ-NPR-Z0-9]", ""); // Remove not allowed characters

            // Limit input to 17 digits max
            if (allowedCharsOnly.length() > 17) {
                allowedCharsOnly = allowedCharsOnly.substring(0, 17);
            }

            // Prevent infinite loop: Only update if text actually changed
            if (!newValue.equals(allowedCharsOnly)) {
                int caretPosition = vinField.getCaretPosition(); // Save caret position
                vinField.setText(allowedCharsOnly);

                // Adjust caret position if needed
                if (caretPosition > allowedCharsOnly.length()) {
                    caretPosition = allowedCharsOnly.length();
                }
                vinField.positionCaret(caretPosition);
            }
        });
        vinField.setDisable(true);

        licensePlateField = new TextField();
        licensePlateField.setDisable(true);

        makeLabel = new Label("Make:");
        makeCombo = new ComboBox<>();
        makeCombo.setDisable(true);

        modelLabel = new Label("Model:");
        modelCombo = new ComboBox<>();
        populateMakeAndModelCombos(makeCombo, modelCombo);
        modelCombo.setDisable(true);

        yearLabel = new Label("Year:");
        yearCombo = new ComboBox<>();
        populateYearCombo(yearCombo);
        yearCombo.setDisable(true);

        detailsPane = new GridPane();
        detailsPane.setPadding(new Insets(20, 50, 20, 50));
        detailsPane.setHgap(10);
        detailsPane.setVgap(10);

        ColumnConstraints customerInfoLabelsColumnConstraint = new ColumnConstraints();
        customerInfoLabelsColumnConstraint.setPercentWidth(10);
        ColumnConstraints customerInfoFieldsColumnConstraint = new ColumnConstraints();
        customerInfoFieldsColumnConstraint.setPercentWidth(15);

        ColumnConstraints spacer1 = new ColumnConstraints();
        spacer1.setPercentWidth(10);

        ColumnConstraints vehicleListColumnConstraint = new ColumnConstraints();
        vehicleListColumnConstraint.setPercentWidth(30);

        ColumnConstraints spacer2 = new ColumnConstraints();
        spacer2.setPercentWidth(10);

        ColumnConstraints vehicleInfoLabelsColumnConstraint = new ColumnConstraints();
        vehicleInfoLabelsColumnConstraint.setPercentWidth(10);
        ColumnConstraints vehicleInfoFieldsColumnConstraint = new ColumnConstraints();
        vehicleInfoFieldsColumnConstraint.setPercentWidth(15);


        detailsPane.getColumnConstraints().addAll(
                customerInfoLabelsColumnConstraint,
                customerInfoFieldsColumnConstraint,
                spacer1,
                vehicleListColumnConstraint,
                spacer2,
                vehicleInfoLabelsColumnConstraint,
                vehicleInfoFieldsColumnConstraint);

    }

    private void initUpdateCustomerButton() {
        updateCustomerButton = new Button("Update Customer");
        updateCustomerButton.setCursor(Cursor.HAND);
        updateCustomerButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateCustomerButton.setOnAction(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

            if (selectedCustomer == null) {
                showToastPopup("Please select a Customer before updating", ToastType.WARNING, customerTable);
                return;
            }

//            if (!isCustomerModified(selectedCustomer)) {
//                TabUtils.showAlert("No Changes Detected", "There are no changes to update.");
//                return;
//            }

            List<String> errors = new ArrayList<>();
            if (!isFirstNameValid(firstNameField)) {
                errors.add("Please provide valid first name");
            }
            if (!isLastNameValid(firstNameField)) {
                errors.add("Please provide valid last name");
            }
            if (!isPhoneValid(phoneField)) {
                errors.add("Please provide valid phone number");
            }
            if (isPhoneAlreadyTaken(phoneField)) {
                errors.add("Phone number is already taken");
            }
            if ((getValueOrDefaultNull(emailField) != null) && !isEmailValid(emailField)) {
                errors.add("Please provide valid email");
            }
            if (!errors.isEmpty()) {
                showToastPopup(errors, ToastType.ERROR, detailsPane);
                event.consume();
                return;
            }

            // Updating customer
            // At this point we are sure required fields are not empty and they are valid
            selectedCustomer.setFirstName(firstNameField.getText());
            selectedCustomer.setLastName(lastNameField.getText());
            selectedCustomer.setPhone(phoneField.getText());
            selectedCustomer.setAddress(getValueOrDefaultNull(addressField));
            selectedCustomer.setCity(getValueOrDefaultNull(cityField));
            selectedCustomer.setState(stateCombo.getValue());
            selectedCustomer.setPostalCode(getValueOrDefaultNull(postalCodeField));
            // At this point we are sure if email field isn't empty, it contains valid email
            selectedCustomer.setEmail(emailField.getText());

            // Save to database
            customerService.updateCustomer(selectedCustomer);

            refreshCustomerTable();

            showToastPopup("Customer details have been successfully updated", ToastType.SUCCESS, detailsPane);
        });
    }

    private void initDeleteCustomerButton() {
        deleteCustomerButton = new Button("Delete Customer");
        deleteCustomerButton.setCursor(Cursor.HAND);
        deleteCustomerButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteCustomerButton.setOnAction(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();

            if (selectedCustomer == null) {
                ViewUtils.showAlert("No Customer Selected", "Please select a customer before deleting.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this customer?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the customer
                customerService.deleteCustomer(selectedCustomer);

                // Refresh customer table
                refreshCustomerTable();

                ViewUtils.showAlert("Customer Deleted", "Customer has been successfully deleted.");
            }
        });
    }

    private void initVehicleListTable() {
        vehicleTableLabel = new Label("Customer Vehicles:");
        vehicleTable = new TableView<>();
        GridPane.setVgrow(vehicleTable, Priority.ALWAYS);
        vehicleTable.setDisable(true);

        TableColumn<Vehicle, String> vinColumn = new TableColumn<>("VIN");
        vinColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));

        TableColumn<Vehicle, String> makeColumn = new TableColumn<>("Make");
        makeColumn.setCellValueFactory(cellData -> {
            String make = (cellData.getValue().getMake() == null) ? null : cellData.getValue().getMake().getName();
            return new SimpleStringProperty(make);
        });

        TableColumn<Vehicle, String> modelColumn = new TableColumn<>("Model");
        modelColumn.setCellValueFactory(cellData -> {
            String model = (cellData.getValue().getModel() == null) ? null : cellData.getValue().getModel().getName();
            return new SimpleStringProperty(model);
        });

        TableColumn<Vehicle, String> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));

        vehicleTable.getColumns().addAll(vinColumn, makeColumn, modelColumn, yearColumn);

        // Ensure all vehicle table's columns have equal width
        vehicleTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / vehicleTable.getColumns().size();
            vehicleTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedVehicle) -> {
            if (selectedVehicle != null) {
                vinField.setDisable(false);
                licensePlateField.setDisable(false);
                makeCombo.setDisable(false);
                modelCombo.setDisable(false);
                yearCombo.setDisable(false);

                vinField.setText(selectedVehicle.getVin());
                licensePlateField.setText(selectedVehicle.getLicensePlate());

                if (selectedVehicle.getMake() != null) {
                    makeCombo.setValue(selectedVehicle.getMake().getName());
                } else {
                    makeCombo.setValue(null);
                }

                if (selectedVehicle.getModel() != null) {
                    modelCombo.setValue(selectedVehicle.getModel().getName());
                } else {
                    modelCombo.setValue(null);
                }

                if (selectedVehicle.getYear() != null) {
                    yearCombo.setValue(String.valueOf(selectedVehicle.getYear()));
                } else {
                    yearCombo.setValue(null);
                }

                updateVehicleButton.setDisable(false);

            } else {
                vinField.setText("");
                licensePlateField.setText("");
                makeCombo.setValue(null);
                modelCombo.setValue(null);
                yearCombo.setValue(null);

                vinField.setDisable(true);
                licensePlateField.setDisable(true);
                makeCombo.setDisable(true);
                modelCombo.setDisable(true);
                yearCombo.setDisable(true);

                updateVehicleButton.setDisable(true);
            }
        });
    }

    private void initAddVehicleButton() {
        addVehicleButton = new Button("Add Vehicle");
        addVehicleButton.setCursor(Cursor.HAND);
        addVehicleButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        addVehicleButton.setOnAction(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                ViewUtils.showToastPopup("Please select a customer before adding new vehicle", ToastType.WARNING, customerTable);
                return;
            }
            showAddVehicleDialogFor(selectedCustomer, vehicleTable);
        });
    }

    private void initUpdateVehicleButton() {
        updateVehicleButton = new Button("Update Vehicle");
        updateVehicleButton.setCursor(Cursor.HAND);
        updateVehicleButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateVehicleButton.setOnAction(event -> {
            Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();

            if (selectedVehicle == null) {
                ViewUtils.showAlert("No Vehicle Selected", "Please select a vehicle before updating.");
                return;
            }

            if (!isVehicleModified(selectedVehicle)) {
                ViewUtils.showAlert("No Changes Detected", "There are no changes to update.");
                return;
            }

            if (vinField.getText() == null) {
                ViewUtils.showAlert("Required field is empty", "VIN - is a required field.");
                return;
            }

            selectedVehicle.setVin(vinField.getText());

            if (makeCombo.getValue() != null) {
                selectedVehicle.setMake(makeService.getMakeByName(makeCombo.getValue()));
                selectedVehicle.setModel((modelCombo.getValue() != null) ? modelService.getModelByNameAndMake(modelCombo.getValue(), selectedVehicle.getMake()) : null);
            } else {
                selectedVehicle.setMake(null);
                selectedVehicle.setModel(null);
            }

            selectedVehicle.setLicensePlate((licensePlateField.getText() == null || licensePlateField.getText().isEmpty()) ? null : licensePlateField.getText());
            selectedVehicle.setYear((yearCombo.getValue() != null) ? Integer.parseInt(yearCombo.getValue()) : null);

            vehicleService.updateVehicle(selectedVehicle);

            // Refresh vehicle list for selected customer
            refreshVehicleListViewForCustomer(selectedVehicle.getCustomerId());

            ViewUtils.showAlert("Vehicle Updated", "Vehicle details have been successfully updated.");
        });
    }

    private void initDeleteVehicleButton() {
        deleteVehicleButton = new Button("Delete Vehicle");
        deleteVehicleButton.setCursor(Cursor.HAND);
        deleteVehicleButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteVehicleButton.setOnAction(event -> {
            Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();

            if (selectedVehicle == null) {
                ViewUtils.showAlert("No Vehicle Selected", "Please select a vehicle before deleting.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this vehicle?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the vehicle
                vehicleService.deleteVehicle(selectedVehicle);

                // Refresh vehicle list for selected customer
                refreshVehicleListViewForCustomer(selectedVehicle.getCustomerId());

                ViewUtils.showAlert("Vehicle Deleted", "Vehicle has been successfully deleted.");
            }
        });
    }

    private void initLayout() {
        HBox customerDetailsButtonBox = new HBox(updateCustomerButton, deleteCustomerButton);
        customerDetailsButtonBox.setAlignment(Pos.CENTER);
        customerDetailsButtonBox.setPadding(new Insets(10, 10, 10, 10));
        customerDetailsButtonBox.setSpacing(20);

        HBox vehicleListButtonBox = new HBox(addVehicleButton, deleteVehicleButton);
        vehicleListButtonBox.setAlignment(Pos.CENTER);
        vehicleListButtonBox.setPadding(new Insets(10, 10, 10, 10));
        vehicleListButtonBox.setSpacing(20);

        // --- Adding Elements to the Grid ---
        detailsPane.add(firstNameLabel, 0, 0);
        detailsPane.add(firstNameField, 1, 0);

        detailsPane.add(new Label("Last Name:"), 0, 1);
        detailsPane.add(lastNameField, 1, 1);

        detailsPane.add(new Label("Address:"), 0, 2);
        detailsPane.add(addressField, 1, 2);

        detailsPane.add(new Label("City:"), 0, 3);
        detailsPane.add(cityField, 1, 3);

        detailsPane.add(new Label("State:"), 0, 4);
        detailsPane.add(stateCombo, 1, 4);

        detailsPane.add(new Label("Postal Code:"), 0, 5);
        detailsPane.add(postalCodeField, 1, 5);

        detailsPane.add(phoneLabel, 0, 6);
        detailsPane.add(phoneField, 1, 6);

        detailsPane.add(new Label("Email:"), 0, 7);
        detailsPane.add(emailField, 1, 7);

        detailsPane.add(customerDetailsButtonBox, 0, 9, 2, 1);

        detailsPane.add(vehicleTableLabel, 3, 0);
        detailsPane.add(vehicleTable, 3, 1, 2, 5); // Span across 5 rows

        detailsPane.add(vehicleListButtonBox, 3, 7, 2, 1);

        detailsPane.add(vinLabel, 5, 0);
        detailsPane.add(vinField, 6, 0);

        detailsPane.add(new Label("License Plate:"), 5, 1);
        detailsPane.add(licensePlateField, 6, 1);

        detailsPane.add(makeLabel, 5, 2);
        detailsPane.add(makeCombo, 6, 2);

        detailsPane.add(modelLabel, 5, 3);
        detailsPane.add(modelCombo, 6, 3);

        detailsPane.add(yearLabel, 5, 4);
        detailsPane.add(yearCombo, 6, 4);

        detailsPane.add(updateVehicleButton, 6, 5);

        // mainPane is a GridPane to control height distribution between customerTable and detailsPane
        GridPane mainPane = new GridPane();
        mainPane.setVgap(5);
        mainPane.add(topPane, 0, 0);
        mainPane.add(customerTable, 0, 1);
        mainPane.add(detailsPane, 0, 2);

        // Make rows to take corresponding heights
        RowConstraints findCustomerRowConstraint = new RowConstraints();
        findCustomerRowConstraint.setPercentHeight(5); // customerTable 10% of available height
        RowConstraints customerTableRowConstraint = new RowConstraints();
        customerTableRowConstraint.setPercentHeight(17); // customerTable 25% of available height
        RowConstraints detailsPaneRowConstraint = new RowConstraints();
        detailsPaneRowConstraint.setPercentHeight(78); // detailsPane takes 65% of available height

        mainPane.getRowConstraints().addAll(findCustomerRowConstraint, customerTableRowConstraint, detailsPaneRowConstraint);
        GridPane.setVgrow(topPane, Priority.ALWAYS);
        GridPane.setVgrow(customerTable, Priority.ALWAYS);
        GridPane.setVgrow(detailsPane, Priority.ALWAYS);
        GridPane.setHgrow(topPane, Priority.ALWAYS);
        GridPane.setHgrow(customerTable, Priority.ALWAYS);
        GridPane.setHgrow(detailsPane, Priority.ALWAYS);

        // Ensure mainPane itself stretches across the full tab width
        mainPane.setPrefWidth(Double.MAX_VALUE);

        customersTab.setContent(mainPane);
    }

    private boolean isVehicleModified(Vehicle vehicle) {
        if (vehicle.getModel() == null) {
            if (modelCombo != null) return true;
        } else if (!Objects.equals(vehicle.getMake().getName(), makeCombo.getValue())) {
            return true;
        }

        if (vehicle.getModel() == null) {
            if (modelCombo.getValue() != null) return true;
        } else if (!Objects.equals(vehicle.getModel().getName(), modelCombo.getValue())) {
            return true;
        }

        return !Objects.equals(String.valueOf(vehicle.getYear()), yearCombo.getValue())
                || !Objects.equals(vehicle.getVin(), vinField.getText())
                || !Objects.equals(vehicle.getLicensePlate(), licensePlateField.getText());
    }

    private void initCustomerTable() {
        customerTable = new TableView<>();
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        customerTable.setPadding(new Insets(10));
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Customer, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Customer, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Customer, String> cityColumn = new TableColumn<>("City");
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Customer, String> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(cellData -> {
            String state = null;
            if (cellData.getValue().getState() != null) {
                state = cellData.getValue().getState().name().replace('_', ' ');
            }
            return new SimpleStringProperty(state);
        });

        TableColumn<Customer, String> postalCodeColumn = new TableColumn<>("Postal Code");
        postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        customerTable.getColumns().addAll(firstNameColumn, lastNameColumn, addressColumn, cityColumn,
                stateColumn, postalCodeColumn, phoneColumn, emailColumn);

        // Ensure all customer table's columns have equal width
        customerTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / customerTable.getColumns().size();
            customerTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        // Update details when a customer is selected
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCustomer) -> {
            if (selectedCustomer != null) {
                firstNameField.setDisable(false);
                lastNameField.setDisable(false);
                addressField.setDisable(false);
                cityField.setDisable(false);
                postalCodeField.setDisable(false);
                stateCombo.setDisable(false);
                phoneField.setDisable(false);
                emailField.setDisable(false);
                vehicleTable.setDisable(false);

                firstNameField.setText(selectedCustomer.getFirstName());
                lastNameField.setText(selectedCustomer.getLastName());
                addressField.setText(selectedCustomer.getAddress());
                cityField.setText(selectedCustomer.getCity());
                postalCodeField.setText(selectedCustomer.getPostalCode());

                Customer.State state = selectedCustomer.getState();
                stateCombo.setValue(state);

                phoneField.setText(selectedCustomer.getPhone());
                emailField.setText(selectedCustomer.getEmail());

                // Load vehicles for selected customer
                List<Vehicle> vehicleList = customerService.getVehiclesByCustomerId(selectedCustomer.getId());
                vehicleTable.setItems(FXCollections.observableArrayList(vehicleList));
            } else {
                firstNameField.setText("");
                lastNameField.setText("");
                addressField.setText("");
                cityField.setText("");
                postalCodeField.setText("");
                stateCombo.setValue(null);
                phoneField.setText("");
                emailField.setText("");

                vehicleTable.getItems().clear();

                firstNameField.setDisable(true);
                lastNameField.setDisable(true);
                addressField.setDisable(true);
                cityField.setDisable(true);
                postalCodeField.setDisable(true);
                stateCombo.setDisable(true);
                phoneField.setDisable(true);
                emailField.setDisable(true);
                vehicleTable.setDisable(true);
            }
        });

        // Load customer data
        List<Customer> customers = customerService.getAllCustomers();
        ObservableList<Customer> customerList = FXCollections.observableArrayList(customers);
        customerTable.setItems(customerList);
    }

    private void refreshCustomerTable() {
        List<Customer> customers = customerService.getAllCustomers();
        ObservableList<Customer> customerList = FXCollections.observableArrayList(customers);
        customerTable.setItems(customerList);
    }

    private void refreshVehicleListViewForCustomer(int customerId) {
        List<Vehicle> vehicles = customerService.getVehiclesByCustomerId(customerId);
        ObservableList<Vehicle> vehicleList = FXCollections.observableArrayList(vehicles);
        vehicleTable.setItems(vehicleList);
    }

    // TODO: The logic doesn't work properly. Need to debug it when updating the customer's info
    private boolean isCustomerModified(Customer customer) {
        return !Objects.equals(customer.getFirstName(), firstNameField.getText()) ||
                !Objects.equals(customer.getLastName(), lastNameField.getText()) ||
                !Objects.equals(customer.getPhone(), phoneField.getText()) ||
                !Objects.equals(customer.getEmail(), emailField.getText()) ||
                !Objects.equals(customer.getAddress(), addressField.getText()) ||
                !Objects.equals(customer.getCity(), cityField.getText()) ||
                !Objects.equals(customer.getState(), stateCombo.getValue()) ||
                !Objects.equals(customer.getPostalCode(), postalCodeField.getText());
    }

}
