package me.skorb.view.tabs;

import static me.skorb.view.ViewUtils.*;
import static me.skorb.view.ViewUtils.getValueOrDefaultNull;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.skorb.entity.Customer;
import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.event.EventBus;
import me.skorb.event.OpenCustomersTabEvent;
import me.skorb.service.CustomerService;
import me.skorb.service.MakeService;
import me.skorb.service.ModelService;
import me.skorb.service.VehicleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NewCustomerTab {

    private static final Logger logger = LogManager.getLogger(NewCustomerTab.class);

    private final CustomerService customerService = new CustomerService();
    private final VehicleService vehicleService = new VehicleService();
    private final MakeService makeService = new MakeService();
    private final ModelService modelService = new ModelService();

    private Tab newCustomerTab;

    private VBox mainLayout;

    private Label firstNameLabel;
    private TextField firstNameField;
    private Label firstNameError;
    private TextField lastNameField;
    private TextField addressField;
    private TextField cityField;
    private ComboBox<Customer.State> stateCombo;
    private TextField postalCodeField;
    private Label phoneLabel;
    private TextField phoneField;
    private Label phoneError;
    private TextField emailField;
    private Label emailError;
    private Label vinLabel;
    private TextField vinField;
    private Label vinError;
    private Label licensePlateLabel;
    private TextField licensePlateField;
    private Label makeLabel;
    private ComboBox<String> makeCombo;
    private Label modelLabel;
    private ComboBox<String> modelCombo;
    private Label yearLabel;
    private ComboBox<String> yearCombo;
    private Label yearError;
    private Button saveCustomerButton;

    public NewCustomerTab() {}

    public Tab createNewCustomerTab() {
        newCustomerTab = new Tab("New Customer");

        initNewCustomerTabContentView(newCustomerTab);

        return newCustomerTab;
    }

    private void initNewCustomerTabContentView(Tab newCustomerTab) {

        // --- Form Layout (GridPane) ---
        GridPane formGrid = new GridPane();
        formGrid.setPadding(new Insets(20, 20, 20, 20));
        formGrid.setHgap(50);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // --- Customer Information Fields ---
        firstNameLabel = new Label("First Name*:");
        firstNameField = new TextField();
        firstNameError = new Label();
        firstNameError.setStyle("-fx-text-fill: red;");

        lastNameField = new TextField();

        addressField = new TextField();
        cityField = new TextField();

        stateCombo = new ComboBox<>();
        stateCombo.setItems(FXCollections.observableArrayList(Customer.State.values()));

        postalCodeField = new TextField();

        phoneLabel = new Label("Phone*:");
        phoneField = new TextField();
        phoneField.setPromptText("(555) 111-3333");
        phoneError = new Label();
        phoneError.setStyle("-fx-text-fill: red;");

/*
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("[^0-9]", ""); // Keep only digits

            // Limit input to 10 digits max
            if (digitsOnly.length() > 10) {
                digitsOnly = digitsOnly.substring(0, 10);
            }

            // Format the phone number for display
            StringBuilder formatted = new StringBuilder();
            int length = digitsOnly.length();

            if (length > 0) {
                formatted.append("(").append(digitsOnly.substring(0, Math.min(3, length)));
            }
            if (length > 3) {
                formatted.append(") ").append(digitsOnly.substring(3, Math.min(6, length)));
            }
            if (length > 6) {
                formatted.append("-").append(digitsOnly.substring(6, Math.min(10, length)));
            }

            // Prevent infinite loop: Only update if text actually changed
            String formattedText = formatted.toString();
            if (!newValue.equals(formattedText)) {
                int caretPosition = phoneField.getCaretPosition(); // Save caret position
                phoneField.setText(formattedText);

                // Adjust caret position if needed
                if (caretPosition > formattedText.length()) {
                    caretPosition = formattedText.length();
                }
                phoneField.positionCaret(caretPosition);
            }

            // Store actual number separately for backend use (digits only)
            phoneField.setUserData(digitsOnly); // You can retrieve it later with phoneField.getUserData()
        });
*/

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

        emailField = new TextField();
        emailError = new Label();
        emailError.setStyle("-fx-text-fill: red;");

        // --- Vehicle Information Fields ---
        vinLabel = new Label("VIN:");
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

        vinError = new Label();
        vinError.setStyle("-fx-text-fill: red;");

        licensePlateLabel = new Label("License Plate:");
        licensePlateField = new TextField();

        makeLabel = new Label("Make:");
        makeCombo = new ComboBox<>();

        modelLabel = new Label("Model:");
        modelCombo = new ComboBox<>();

        populateMakeCombo(makeCombo, modelCombo);

        yearLabel = new Label("Year:");
        yearCombo = new ComboBox<>();
        populateYearCombo(yearCombo);
        yearError = new Label();
        yearError.setStyle("-fx-text-fill: red;");

        initSaveCustomerButton();

        // Customer Information
        formGrid.add(firstNameLabel, 0, 0);
        formGrid.add(firstNameField, 1, 0);

        formGrid.add(new Label("Last Name*:"), 0, 2);
        formGrid.add(lastNameField, 1, 2);

        formGrid.add(new Label("Address:"), 0, 3);
        formGrid.add(addressField, 1, 3);

        formGrid.add(new Label("City:"), 0, 4);
        formGrid.add(cityField, 1, 4);

        formGrid.add(new Label("State:"), 0, 5);
        formGrid.add(stateCombo, 1, 5);

        formGrid.add(new Label("Postal Code:"), 0, 6);
        formGrid.add(postalCodeField, 1, 6);

        formGrid.add(phoneLabel, 0, 7);
        formGrid.add(phoneField, 1, 7);
        formGrid.add(phoneError, 1, 8);

        formGrid.add(new Label("Email:"), 0, 9);
        formGrid.add(emailField, 1, 9);
        formGrid.add(emailError, 1, 10);

        // Vehicle Information
        formGrid.add(vinLabel, 2, 0);
        formGrid.add(vinField, 3, 0);
        formGrid.add(vinError, 3, 1);

        formGrid.add(licensePlateLabel, 2, 2);
        formGrid.add(licensePlateField, 3, 2);

        formGrid.add(makeLabel, 2, 3);
        formGrid.add(makeCombo, 3, 3);

        formGrid.add(modelLabel, 2, 4);
        formGrid.add(modelCombo, 3, 4);

        formGrid.add(yearLabel, 2, 5);
        formGrid.add(yearCombo, 3, 5);

        // --- Button Section ---
        HBox buttonBox = new HBox(saveCustomerButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // --- Assemble UI ---
        mainLayout = new VBox(10, formGrid, buttonBox);

        newCustomerTab.setContent(mainLayout);
    }

    private void initSaveCustomerButton() {
        saveCustomerButton = new Button("Save Customer");
        saveCustomerButton.setCursor(Cursor.HAND);
        saveCustomerButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        saveCustomerButton.setOnAction(event -> {
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
                showToastPopup(errors, ToastType.ERROR, mainLayout);
                event.consume();
                return;
            }

            Customer newCustomer = new Customer();
            // At this point we are sure required fields are not empty and they are valid
            newCustomer.setFirstName(firstNameField.getText());
            newCustomer.setLastName(lastNameField.getText());
            newCustomer.setPhone(phoneField.getText());

            newCustomer.setAddress(getValueOrDefaultNull(addressField));
            newCustomer.setCity(getValueOrDefaultNull(cityField));
            newCustomer.setState(stateCombo.getValue());
            newCustomer.setPostalCode(getValueOrDefaultNull(postalCodeField));
            // At this point we are sure if email field isn't empty, it contains valid email
            newCustomer.setEmail(getValueOrDefaultNull(emailField));

            Customer savedCustomer = customerService.saveCustomer(newCustomer);

            logger.info("New customer successfully saved to the database: " + savedCustomer);

//            Vehicle newVehicle = new Vehicle();
//
//            newVehicle.setCustomerId(savedCustomer.getId());
//
//            Make selectedMake = (makeCombo.getValue() == null) ? null : makeService.getMakeByName(makeCombo.getValue());
//            newVehicle.setMake(selectedMake);
//
//            Model selectedModel = (modelCombo.getValue() == null) ? null : modelService.getModelByNameAndMake(modelCombo.getValue(), selectedMake);
//            newVehicle.setModel(selectedModel);
//
//            Integer year = (yearCombo.getValue() == null) ? null : Integer.parseInt(yearCombo.getValue());
//            newVehicle.setYear(year);
//
//            newVehicle.setVin(vinField.getText());
//
//            String licensePlate = (licensePlateField.getText() == null || licensePlateField.getText().isEmpty()) ? null : licensePlateField.getText();
//            newVehicle.setLicensePlate(licensePlate);
//
//            Vehicle savedVehicle = vehicleService.saveVehicle(newVehicle);
//
//            logger.info("Vehicle successfully saved to the database: " + savedVehicle);

            // Close this tab dynamically
            TabPane tabPane = newCustomerTab.getTabPane();
            if (tabPane != null) {
                Platform.runLater(() -> tabPane.getTabs().remove(newCustomerTab));
            }

            // Open the Customers List tab
            EventBus.fireEvent(new OpenCustomersTabEvent());
        });
    }

    private void populateMakeCombo(ComboBox<String> makeCombo, ComboBox<String> modelCombo) {
        List<Make> makes = makeService.getAllMakesWithModels();
        ObservableList<String> makeNames = FXCollections.observableArrayList();
        makes.forEach(make -> makeNames.add(make.getName()));

        makeCombo.setItems(makeNames);

        // Load models dynamically when a make is selected
        makeCombo.setOnAction(event -> {
            String selectedMakeName = makeCombo.getValue();
            if (selectedMakeName != null) {
                Make selectedMake = makes.stream()
                        .filter(make -> make.getName().equals(selectedMakeName))
                        .findFirst()
                        .orElse(null);

                if (selectedMake != null) {
                    populateModelCombo(modelCombo, selectedMake);
                }
            }
        });
    }

    private void populateModelCombo(ComboBox<String> modelCombo, Make selectedMake) {
        List<Model> models = selectedMake.getModels();
        ObservableList<String> modelNames = FXCollections.observableArrayList();
        models.forEach(model -> modelNames.add(model.getName()));

        modelCombo.setItems(modelNames);
    }

    private void populateYearCombo(ComboBox<String> yearCombo) {
        ObservableList<String> years = FXCollections.observableArrayList();
        int currentYear = java.time.Year.now().getValue();

        for (int i = currentYear; i >= 1980; i--) { // Populate with years from 1980 to the current year
            years.add(String.valueOf(i));
        }

        yearCombo.setItems(years);
    }

}
