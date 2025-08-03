package me.skorb.view;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.util.Duration;
import me.skorb.entity.Customer;
import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.entity.Vehicle;
import me.skorb.service.CustomerService;
import me.skorb.service.MakeService;
import me.skorb.service.ModelService;
import me.skorb.service.VehicleService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViewUtils {

    private static final CustomerService CUSTOMER_SERVICE = new CustomerService();
    private static final VehicleService VEHICLE_SERVICE = new VehicleService();
    private static final MakeService MAKE_SERVICE = new MakeService();
    private static final ModelService MODEL_SERVICE = new ModelService();

    public static boolean isRequiredComboBoxChosen(ComboBox<String> comboBox, Label label) {
        if (comboBox.getValue() == null) {
            label.setStyle("-fx-text-fill: red;");
            return false;
        } else {
            label.setStyle("-fx-text-fill: black;");
            return true;
        }
    }

    public static boolean isFirstNameValid(TextField firstNameField) {
        return verifyRequiredFieldIsNotEmpty(firstNameField);
    }

    public static boolean isLastNameValid(TextField lastNameField) {
        return verifyRequiredFieldIsNotEmpty(lastNameField);
    }

    public static boolean isPhoneValid(TextField phoneField) {
        String text = phoneField.getText();
        return verifyRequiredFieldIsNotEmpty(phoneField)
                && text.matches("^\\d{10}$");
    }

    public static boolean verifyRequiredFieldIsNotEmpty(TextInputControl textField) {
        return textField.getText() != null && !textField.getText().trim().isEmpty();
    }

    public static boolean isVINValid(TextField vinField) {
        String vin = vinField.getText();
        if (vin == null || vin.trim().isEmpty()) return false;
        return vin.matches("^[A-HJ-NPR-Z0-9]{17}$");
    }

    public static boolean verifyVINIsValid(TextField vinField, Label vinErrorLabel, String errorMessage) {
        String vin = vinField.getText();
        if (!vin.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
            vinErrorLabel.setText(errorMessage);
            return false;
        } else {
            vinErrorLabel.setText("");
            return true;
        }
    }

    public static boolean isVinAlreadyTaken(TextField vinField) {
        return VEHICLE_SERVICE.isVinAlreadyTaken(vinField.getText());
    }

    public static boolean isEmailValid(TextField emailField) {
        String email = emailField.getText();
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isPhoneAlreadyTaken(TextField phoneField) {
        String phone = phoneField.getText();
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        return CUSTOMER_SERVICE.existsByPhone(digitsOnly);
    }

    public static boolean isPriceValid(TextField priceField, Label priceLabel) {
        String priceText = priceField.getText().trim();

        try {
            BigDecimal price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                priceLabel.setStyle("-fx-text-fill: red;");
                return false;
            }

            priceLabel.setStyle("-fx-text-fill: black;"); // Clear error message if input is valid

            return true;
        } catch (NumberFormatException e) {
            priceLabel.setStyle("-fx-text-fill: red;");
            return false;
        }
    }

    // We want to make sure that we aren't saving empty Strings to DB.
    // We are saving NULL values instead.
    public static String getValueOrDefaultNull(TextField textField) {
        String value = textField.getText();
        return (value != null && !value.trim().isEmpty()) ? value : null;
    }

    @Deprecated
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showNewCustomerDialog(TableView<Customer> customerTable) {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Add New Customer");
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType saveButtonType = new ButtonType("Add Customer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        Label firstNameLabel = new Label("First Name*:");
        firstNameLabel.setMinWidth(100);
        TextField firstNameField = new TextField();
        firstNameField.setMinWidth(220);
        Label lastNameLabel = new Label("Last Name*:");
        lastNameLabel.setMinWidth(100);
        TextField lastNameField = new TextField();
        lastNameField.setMinWidth(220);
        Label addressLabel = new Label("Address:");
        addressLabel.setMinWidth(100);
        TextField addressField = new TextField();
        addressField.setMinWidth(220);
        Label cityLabel = new Label("City:");
        cityLabel.setMinWidth(100);
        TextField cityField = new TextField();
        cityField.setMinWidth(220);
        Label stateLabel = new Label("State:");
        stateLabel.setMinWidth(100);
        ComboBox<Customer.State> stateComboBox = new ComboBox<>();
        stateComboBox.setMinWidth(220);
        stateComboBox.setItems(FXCollections.observableArrayList(Customer.State.values()));
        Label postalCodeLabel = new Label("ZIP:");
        postalCodeLabel.setMinWidth(100);
        TextField postalCodeField = new TextField();
        postalCodeField.setMinWidth(220);
        Label phoneLabel = new Label("Phone:");
        phoneLabel.setMinWidth(100);
        TextField phoneField = new TextField();
        phoneField.setMinWidth(220);
        Label emailLabel = new Label("Email:");
        emailLabel.setMinWidth(100);
        TextField emailField = new TextField();
        emailField.setMinWidth(220);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        form.add(firstNameLabel, 0, 0);
        form.add(firstNameField, 1, 0);

        form.add(lastNameLabel, 0, 1);
        form.add(lastNameField, 1, 1);

        form.add(addressLabel, 0, 2);
        form.add(addressField, 1, 2);

        form.add(cityLabel, 0, 3);
        form.add(cityField, 1, 3);

        form.add(stateLabel, 0, 4);
        form.add(stateComboBox, 1, 4);

        form.add(postalCodeLabel, 0, 5);
        form.add(postalCodeField, 1, 5);

        form.add(phoneLabel, 0, 6);
        form.add(phoneField, 1, 6);

        form.add(emailLabel, 0, 7);
        form.add(emailField, 1, 7);

        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setCursor(Cursor.HAND);
        saveButton.setStyle(
                "-fx-background-color: #2c7be5; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;"
        );

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setCursor(Cursor.HAND);
        cancelButton.setStyle("-fx-background-radius: 6;");

        // User input validation
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            List<String> errors = new ArrayList<>();
            if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
                errors.add("First Name field is required");
            }
            if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
                errors.add("Last Name field is required");
            }
            if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
                errors.add("Phone field is required");
            }

            if (!errors.isEmpty()) {
                showToastPopup(errors, ToastType.ERROR, dialog.getDialogPane());
                event.consume();
                return;
            }

            // Check if user with this phone number already exists in database
            if (CUSTOMER_SERVICE.existsByPhone(phoneField.getText())) {
                showToastPopup("Customer with this phone already exists", ToastType.WARNING, dialog.getDialogPane());
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // First Name, Last Name, Phone fields are checked to that point and they are not empty
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String phone = phoneField.getText();

                // Other fields are not required fields, so we accept NULLs
                String address = getValueOrDefaultNull(addressField);
                String city = getValueOrDefaultNull(cityField);
                Customer.State state = stateComboBox.getValue();
                String postalCode = getValueOrDefaultNull(postalCodeField);
                String email = getValueOrDefaultNull(emailField);

                Customer newCustomer = new Customer();
                newCustomer.setFirstName(firstName);
                newCustomer.setLastName(lastName);
                newCustomer.setAddress(address);
                newCustomer.setCity(city);
                newCustomer.setState(state);
                newCustomer.setPostalCode(postalCode);
                newCustomer.setPhone(phone);
                newCustomer.setEmail(email);

                return newCustomer;
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();
        result.ifPresent(newCustomer -> {
            CUSTOMER_SERVICE.saveCustomer(newCustomer);
            List<Customer> updated = CUSTOMER_SERVICE.getAllCustomers();
            customerTable.setItems(FXCollections.observableArrayList(updated));
            Platform.runLater(() -> {
                customerTable.getSelectionModel().select(newCustomer);
                customerTable.scrollTo(newCustomer);
            });
        });
    }

    public static void showAddVehicleDialogFor(Customer customer, TableView<Vehicle> vehicleTable) {
        Dialog<Vehicle> dialog = new Dialog<>();
        dialog.setTitle("Add New Vehicle");

        ButtonType saveButtonType = new ButtonType("Add Vehicle", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = ButtonType.CANCEL;
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        Label newVehicleMakeLabel = new Label("Make*:");
        ComboBox<String> newVehicleMakeCombo = new ComboBox<>();

        Label newVehicleModelLabel = new Label("Model*:");
        ComboBox<String> newVehicleModelCombo = new ComboBox<>();
        populateMakeAndModelCombos(newVehicleMakeCombo, newVehicleModelCombo);

        Label newVehicleYearLabel = new Label("Year*:");
        ComboBox<String> newVehicleYearCombo = new ComboBox<>();
        populateYearCombo(newVehicleYearCombo);

        Label newVehicleVinLabel = new Label("VIN:");
        TextField newVehicleVinField = new TextField();
        newVehicleVinField.textProperty().addListener((observable, oldValue, newValue) -> {
            String allowedCharsOnly = newValue.toUpperCase().replaceAll("[^A-HJ-NPR-Z0-9]", "");

            // Limit input to 17 digits max
            if (allowedCharsOnly.length() > 17) {
                allowedCharsOnly = allowedCharsOnly.substring(0, 17);
            }

            // Prevent infinite loop: Only update if text actually changed
            if (!newValue.equals(allowedCharsOnly)) {
                int caretPosition = newVehicleVinField.getCaretPosition(); // Save caret position
                newVehicleVinField.setText(allowedCharsOnly);

                // Adjust caret position if needed
                if (caretPosition > allowedCharsOnly.length()) {
                    caretPosition = allowedCharsOnly.length();
                }
                newVehicleVinField.positionCaret(caretPosition);
            }
        });

        Label newVehicleLicensePlateLabel = new Label("License Plate:");
        TextField newVehicleLicensePlateField = new TextField();

        GridPane mainPane = new GridPane();
        mainPane.setHgap(10);
        mainPane.setVgap(10);
        mainPane.setPadding(new Insets(20, 20, 20, 20));

        mainPane.add(newVehicleVinLabel, 0, 0);
        mainPane.add(newVehicleVinField, 1, 0);
        mainPane.add(newVehicleLicensePlateLabel, 0, 1);
        mainPane.add(newVehicleLicensePlateField, 1, 1);
        mainPane.add(newVehicleMakeLabel, 0, 2);
        mainPane.add(newVehicleMakeCombo, 1, 2);
        mainPane.add(newVehicleModelLabel, 0, 3);
        mainPane.add(newVehicleModelCombo, 1, 3);
        mainPane.add(newVehicleYearLabel, 0, 4);
        mainPane.add(newVehicleYearCombo, 1, 4);

        dialog.getDialogPane().setContent(mainPane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setCursor(Cursor.HAND);
        saveButton.setStyle(
                "-fx-background-color: #2c7be5; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 6;"
        );
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            List<String> errors = new ArrayList<>();
            if (newVehicleMakeCombo.getValue() == null) {
                errors.add("Make field is required");
            }
            if (newVehicleModelCombo.getValue() == null) {
                errors.add("Model field is required");
            }
            if (newVehicleYearCombo.getValue() == null) {
                errors.add("Year field is required");
            }
            if (!errors.isEmpty()) {
                showToastPopup(errors, ToastType.ERROR, dialog.getDialogPane());
                event.consume();
                return;
            }
        });

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setCursor(Cursor.HAND);
        cancelButton.setStyle("-fx-background-radius: 6;");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // At this point we are sure that required fields are not empty, so it's not necessary to check them here again
                Make selectedMake = MAKE_SERVICE.getMakeByName(newVehicleMakeCombo.getValue());
                Model selectedModel = MODEL_SERVICE.getModelByNameAndMake(newVehicleModelCombo.getValue(), selectedMake);
                Integer newVehicleYear = Integer.parseInt(newVehicleYearCombo.getValue());

                // These are optional fields
                String newVehicleVin = getValueOrDefaultNull(newVehicleVinField);
                String newVehicleLicensePlate = getValueOrDefaultNull(newVehicleLicensePlateField);

                Vehicle newVehicle = new Vehicle();
                newVehicle.setCustomerId(customer.getId());
                newVehicle.setVin(newVehicleVin);
                newVehicle.setLicensePlate(newVehicleLicensePlate);
                newVehicle.setMake(selectedMake);
                newVehicle.setModel(selectedModel);
                newVehicle.setYear(newVehicleYear);

                return newVehicle;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newVehicle -> {
            CUSTOMER_SERVICE.addVehicleToCustomer(customer.getId(), newVehicle);
            List<Vehicle> updated = CUSTOMER_SERVICE.getVehiclesByCustomerId(customer.getId());
            vehicleTable.setItems(FXCollections.observableArrayList(updated));
            Platform.runLater(() -> {
                vehicleTable.getSelectionModel().select(newVehicle);
                vehicleTable.scrollTo(newVehicle);
            });
        });
    }

    public static void populateMakeAndModelCombos(ComboBox<String> makeCombo, ComboBox<String> modelCombo) {
        List<Make> makes = MAKE_SERVICE.getAllMakesWithModels();
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
                    List<Model> models = selectedMake.getModels();
                    ObservableList<String> modelNames = FXCollections.observableArrayList();
                    models.forEach(model -> modelNames.add(model.getName()));

                    modelCombo.setItems(modelNames);
                }
            }
        });
    }

    public static void populateYearCombo(ComboBox<String> yearCombo) {
        ObservableList<String> years = FXCollections.observableArrayList();
        int currentYear = java.time.Year.now().getValue();

        for (int i = currentYear; i >= 1980; i--) { // Populate with years from 1980 to the current year
            years.add(String.valueOf(i));
        }

        yearCombo.setItems(years);
    }

    public enum ToastType {
        SUCCESS, WARNING, ERROR
    }

    public static void showToastPopup(List<String> errors, ToastType type, Node parentNode) {
        String message = String.join("\n", errors);
        showToastPopup(message, type, parentNode);
    }

    public static void showToastPopup(String message, ToastType type, Node parentNode) {
        String backgroundColor = switch (type) {
            case SUCCESS -> "#2ecc71"; // зелёный
            case WARNING -> "#f39c12"; // янтарный
            case ERROR   -> "#e74c3c"; // красный
        };

        Label toastLabel = new Label(message);
        toastLabel.setStyle("-fx-background-color: " + backgroundColor + "; -fx-text-fill: white; " +
                "-fx-padding: 10px 20px; -fx-font-size: 13px; -fx-background-radius: 8;");
        toastLabel.setOpacity(0);

        StackPane toastPane = new StackPane(toastLabel);
        toastPane.setStyle("-fx-background-radius: 8;");
        toastPane.setPadding(new Insets(10));

        Popup popup = new Popup();
        popup.getContent().add(toastPane);
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        // Координаты на экране относительно родительского узла
        Bounds parentBounds = parentNode.localToScreen(parentNode.getBoundsInLocal());
        double x = parentBounds.getMinX() + (parentBounds.getWidth() - 200) / 2;
        double y = parentBounds.getMinY() + parentBounds.getHeight() - 150;

        popup.show(parentNode.getScene().getWindow(), x, y);

        // Toast animation
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(toastLabel.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(0.3), new KeyValue(toastLabel.opacityProperty(), 1))
        );
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.seconds(2), new KeyValue(toastLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(3), new KeyValue(toastLabel.opacityProperty(), 0))
        );

        fadeOut.setOnFinished(e -> popup.hide());

        fadeIn.play();
        fadeOut.play();
    }

}
