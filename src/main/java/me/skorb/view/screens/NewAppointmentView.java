package me.skorb.view.screens;

import static me.skorb.view.ViewUtils.*;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.skorb.entity.Appointment;
import me.skorb.entity.Customer;
import me.skorb.entity.Vehicle;
import me.skorb.service.AppointmentService;
import me.skorb.service.CustomerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class NewAppointmentView extends VBox {

    private final AppointmentService appointmentService = new AppointmentService();
    private final CustomerService customerService = new CustomerService();

    private Label customerTableLabel;
    private TableView<Customer> customerTable;
    private Label vehicleTableLabel;
    private TableView<Vehicle> vehicleTable;
    private Label detailsLabel;
    private Label detailsErrorLabel;
    private TextArea detailsArea;
    private DatePicker datePicker;
    private ComboBox<String> timeComboBox;
    private LocalDateTime selectedDateTime;
    private Button createAppointmentButton;

    public NewAppointmentView() {
        initCustomerTable();
        initVehicleTable();
        initAppointmentDetailsArea();
        initCreateAppointmentButton();
        initLayout();
    }

    private void initLayout() {
        // --- First Column: Customer Information ---
        VBox customerTableColumn = new VBox(10);
        customerTableColumn.setPadding(new Insets(20));
        customerTableColumn.getChildren().addAll(customerTableLabel, customerTable);
        HBox.setHgrow(customerTableColumn, Priority.ALWAYS);

        // --- Second Column: Vehicle Information ---
        VBox vehicleTableColumn = new VBox(10);
        vehicleTableColumn.setPadding(new Insets(20));
        vehicleTableColumn.getChildren().addAll(vehicleTableLabel, vehicleTable);
        HBox.setHgrow(vehicleTableColumn, Priority.ALWAYS);

        // --- Third Column: Issue Description ---
        VBox issueDescriptionColumn = new VBox(10);
        issueDescriptionColumn.setPadding(new Insets(20));
        issueDescriptionColumn.getChildren().addAll(detailsLabel, detailsArea, detailsErrorLabel, new Label("Select Date & Time:"), datePicker, timeComboBox);
        HBox.setHgrow(issueDescriptionColumn, Priority.ALWAYS);

        // --- Main Layout: Arrange columns in an HBox ---
        HBox mainContent = new HBox(30, customerTableColumn, vehicleTableColumn, issueDescriptionColumn);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);

        customerTableColumn.prefWidthProperty().bind(mainContent.widthProperty().divide(3));
        vehicleTableColumn.prefWidthProperty().bind(mainContent.widthProperty().divide(3));
        issueDescriptionColumn.prefWidthProperty().bind(mainContent.widthProperty().divide(3));

        // --- Button Section ---
        HBox buttonBox = new HBox(createAppointmentButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));

        // --- Final Layout ---
        setSpacing(10);
        getChildren().addAll(mainContent, buttonBox);
        setPadding(new Insets(20));
    }

    private void initCustomerTable() {
        customerTableLabel = new Label("Choose a Customer from list:");
        customerTable = new TableView<>();
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        customerTable.setPadding(new Insets(10));
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Customer, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Customer, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Customer, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        customerTable.getColumns().addAll(firstNameColumn, lastNameColumn, phoneColumn);

        // Ensure all customer table's columns have equal width
        customerTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / customerTable.getColumns().size();
            customerTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        // Load customer data
        List<Customer> customers = customerService.getAllCustomers();
        ObservableList<Customer> customerList = FXCollections.observableArrayList(customers);
        customerTable.setItems(customerList);

        // Update vehicle table when a customer is selected
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedCustomer) -> {
            if (selectedCustomer != null) {
                // Load vehicles for selected customer
                List<Vehicle> vehicleList = customerService.getVehiclesByCustomerId(selectedCustomer.getId());
                vehicleTable.setItems(FXCollections.observableArrayList(vehicleList));
            } else {
                vehicleTable.getItems().clear();
            }
        });
    }

    private void initVehicleTable() {
        vehicleTableLabel = new Label("Choose a Vehicle from list:");
        vehicleTable = new TableView<>();
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);
        vehicleTable.setPadding(new Insets(10));
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
            if (selectedVehicle == null) {
                detailsArea.setText("");
                detailsErrorLabel.setText("");

                detailsArea.setDisable(true);
                datePicker.setDisable(true);
                timeComboBox.setDisable(true);
            } else {
                detailsArea.setDisable(false);
                datePicker.setDisable(false);
                timeComboBox.setDisable(false);
            }
        });
    }

    private void initAppointmentDetailsArea() {
        detailsLabel = new Label("Appointment Details*:");
        detailsErrorLabel = new Label();
        detailsErrorLabel.setStyle("-fx-text-fill: red;");
        detailsArea = new TextArea();
        detailsArea.setDisable(true);

        initDateTimeFields();
    }

    private void initDateTimeFields() {
        datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");
        datePicker.setDisable(true);

        // Time Selection ComboBox
        timeComboBox = new ComboBox<>();
        timeComboBox.setPromptText("Select Time");
        populateTimeComboBox();
        timeComboBox.setDisable(true);

        // Listener to update LocalDateTime
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateSelectedDateTime());
        timeComboBox.valueProperty().addListener((obs, oldTime, newTime) -> updateSelectedDateTime());
    }

    private void initCreateAppointmentButton() {
        createAppointmentButton = new Button("Create Appointment");
        createAppointmentButton.setCursor(Cursor.HAND);
        createAppointmentButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        createAppointmentButton.setOnAction(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();

            List<String> errors = new ArrayList<>();
            if (!verifyRequiredFieldIsNotEmpty(detailsArea)) {
                errors.add("Appointment Notes are required");
            }
            if (selectedDateTime == null) {
                errors.add("Appointment date and time are required");
            }
            if (!errors.isEmpty()) {
//                showToastPopup(errors, ToastType.ERROR, mainLayout);
                event.consume();
                return;
            }

            if (selectedCustomer != null && selectedVehicle != null && selectedDateTime != null) {
                Appointment appointment = new Appointment();
                appointment.setCustomer(selectedCustomer);
                appointment.setVehicle(selectedVehicle);
                appointment.setDateTime(selectedDateTime);
                appointment.setNotes(detailsArea.getText());

                appointmentService.createAppointment(appointment);

                Platform.runLater(() -> {
                    ((Stage) createAppointmentButton.getScene().getWindow()).close();
                });
            }
        });
    }

    private void populateTimeComboBox() {
        for (int hour = 8; hour <= 18; hour++) { // Example: Business hours 8 AM - 6 PM
            for (int minute = 0; minute < 60; minute += 30) { // 30-minute intervals
                String time = String.format("%02d:%02d", hour, minute);
                timeComboBox.getItems().add(time);
            }
        }
    }

    private void updateSelectedDateTime() {
        if (datePicker.getValue() != null && timeComboBox.getValue() != null) {
            LocalDate date = datePicker.getValue();
            String[] timeParts = timeComboBox.getValue().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            selectedDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
        }
    }

}
