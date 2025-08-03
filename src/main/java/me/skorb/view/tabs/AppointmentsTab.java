package me.skorb.view.tabs;

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
import me.skorb.entity.Appointment;
import me.skorb.entity.Order;
import me.skorb.entity.Vehicle;
import me.skorb.event.EventBus;
import me.skorb.event.OpenOrdersTabEvent;
import me.skorb.service.AppointmentService;
import me.skorb.service.OrderService;
import me.skorb.view.ViewUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AppointmentsTab {

    private final AppointmentService appointmentService = new AppointmentService();
    private final OrderService orderService = new OrderService();

    private Tab appointmentsTab;

    private TableView<Appointment> appointmentTable;
    private Label issueLabel;
    private Label issueErrorLabel;
    private TextArea issueArea;
    private DatePicker datePicker;
    private ComboBox<String> timeComboBox;
    private LocalDateTime selectedDateTime;
    private ComboBox<String> statusComboBox;
    private Button updateAppointmentButton;
    private Button deleteAppointmentButton;
    private Button createOrder;

    public AppointmentsTab() {}

    public Tab createAppointmentsTab() {
        appointmentsTab = new Tab("Appointments");

        initAppointmentsTabContentView();

        return appointmentsTab;
    }

    private void initAppointmentsTabContentView() {
        initAppointmentTable();
        initAppointmentDetailsArea();
        initLayout();
    }

    private void initAppointmentTable() {
        appointmentTable = new TableView<>();
        VBox.setVgrow(appointmentTable, Priority.ALWAYS);

        TableColumn<Appointment, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getDateTime().toLocalDate())));

        TableColumn<Appointment, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateTime();
            int hour = dateTime.getHour();
            int minute = dateTime.getMinute();
            String time = String.format("%02d:%02d", hour, minute);
            return new SimpleStringProperty(time);
        });

        TableColumn<Appointment, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomer().getFirstName()));

        TableColumn<Appointment, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomer().getLastName()));

        TableColumn<Appointment, String> makeColumn = new TableColumn<>("Make");
        makeColumn.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue().getVehicle();
            String make = (vehicle != null && vehicle.getMake() != null) ? vehicle.getMake().getName() : null;
            return new SimpleStringProperty(make);
        });

        TableColumn<Appointment, String> modelColumn = new TableColumn<>("Model");
        modelColumn.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue().getVehicle();
            String model = (vehicle != null && vehicle.getModel() != null) ? vehicle.getModel().getName() : null;
            return new SimpleStringProperty(model);
        });

        TableColumn<Appointment, String> yearColumn = new TableColumn<>("Year");
        yearColumn.setCellValueFactory(cellData -> {
            Vehicle vehicle = cellData.getValue().getVehicle();
            String year = (vehicle != null && vehicle.getYear() != null && vehicle.getYear() != 0) ? vehicle.getYear().toString() : null;
            return new SimpleStringProperty(year);
        });

        TableColumn<Appointment, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        appointmentTable.getColumns().addAll(/*idColumn,*/ dateColumn, timeColumn, firstNameColumn, lastNameColumn, makeColumn, modelColumn, yearColumn, statusColumn);

        // Ensure all customer table's columns have equal width
        appointmentTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / appointmentTable.getColumns().size();
            appointmentTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        // Load appointments from database and sort them by DateTime
        refreshAppointmentTable();

        // Update an appointment details when the appointment is selected
        appointmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedAppointment) -> {
            if (selectedAppointment == null) {
                issueArea.setText("");
                issueErrorLabel.setText("");
                datePicker.setValue(null);
                timeComboBox.setValue(null);
                statusComboBox.setValue(null);

                issueArea.setDisable(true);
                datePicker.setDisable(true);
                timeComboBox.setDisable(true);
                statusComboBox.setDisable(true);
                updateAppointmentButton.setDisable(true);
                deleteAppointmentButton.setDisable(true);
                createOrder.setDisable(true);
            } else {
                issueArea.setDisable(false);
                datePicker.setDisable(false);
                timeComboBox.setDisable(false);
                statusComboBox.setDisable(false);
                updateAppointmentButton.setDisable(false);
                deleteAppointmentButton.setDisable(false);
                createOrder.setDisable(false);

                issueArea.setText(selectedAppointment.getNotes());

                LocalDateTime dateTime = selectedAppointment.getDateTime();
                LocalDate date = dateTime.toLocalDate();
                datePicker.setValue(date);

                int hour = dateTime.getHour();
                int minute = dateTime.getMinute();
                String time = String.format("%02d:%02d", hour, minute);
                timeComboBox.setValue(time);

                statusComboBox.setValue(selectedAppointment.getStatus().name());
            }
        });
    }

    private void initAppointmentDetailsArea() {
        issueLabel = new Label("Issue Description*:");
        issueErrorLabel = new Label();
        issueErrorLabel.setStyle("-fx-text-fill: red;");
        issueArea = new TextArea();
        issueArea.setDisable(true);

        initDateTimeFields();
        initStatusComboBox();
        initAppointmentUpdateButton();
        initAppointmentDeleteButton();
        initCreateOrderButton();
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

    private void initStatusComboBox() {
        statusComboBox = new ComboBox<>();
        statusComboBox.setDisable(true);
        Appointment.Status[] statuses = Appointment.Status.values();
        for (Appointment.Status status : statuses) {
            statusComboBox.getItems().add(status.name());
        }
    }

    private void initAppointmentUpdateButton() {
        updateAppointmentButton = new Button("Update Appointment");
        updateAppointmentButton.setCursor(Cursor.HAND);
        updateAppointmentButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateAppointmentButton.setDisable(true);
        updateAppointmentButton.setOnAction(event -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

            if (selectedAppointment == null) {
                ViewUtils.showAlert("No Appointment Selected", "Please select an appointment before updating.");
                return;
            }

            if (!isAppointmentModified(selectedAppointment)) {
                ViewUtils.showAlert("No Changes Detected", "There are no changes to update.");
                return;
            }

            if (issueArea.getText() == null || issueArea.getText().trim().isEmpty()) {
                ViewUtils.showAlert("Required field is empty", "Issue Description - is a required field.");
                return;
            }

            if (datePicker.getValue() == null) {
                ViewUtils.showAlert("Required field is empty", "Date - is a required field.");
                return;
            }

            if (timeComboBox.getValue() == null) {
                ViewUtils.showAlert("Required field is empty", "Date - is a required field.");
                return;
            }

            if (statusComboBox.getValue() == null) {
                ViewUtils.showAlert("Required field is empty", "Date - is a required field.");
                return;
            }

            selectedAppointment.setNotes(issueArea.getText());

            updateSelectedDateTime();

            selectedAppointment.setDateTime(selectedDateTime);
            selectedAppointment.setStatus(Appointment.Status.valueOf(statusComboBox.getValue()));

            appointmentService.updateAppointment(selectedAppointment);

            // Refresh appointment list
            refreshAppointmentTable();

            ViewUtils.showAlert("Appointment Updated", "Appointment details have been successfully updated.");
        });
    }

    private void initAppointmentDeleteButton() {
        deleteAppointmentButton = new Button("Delete Appointment");
        deleteAppointmentButton.setCursor(Cursor.HAND);
        deleteAppointmentButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteAppointmentButton.setDisable(true);
        deleteAppointmentButton.setOnAction(event -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

            if (selectedAppointment == null) {
                ViewUtils.showAlert("No Appointment Selected", "Please select an appointment before updating.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this appointment?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the Appointment
                appointmentService.deleteAppointment(selectedAppointment);

                refreshAppointmentTable();

                ViewUtils.showAlert("Appointment Deleted", "Appointment has been successfully deleted.");
            }
        });
    }

    private void initCreateOrderButton() {
        createOrder = new Button("Create Order");
        createOrder.setCursor(Cursor.HAND);
        createOrder.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        createOrder.setDisable(true);
        createOrder.setOnAction(event -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

            if (selectedAppointment == null) {
                ViewUtils.showAlert("No Appointment Selected", "Please select an appointment before updating.");
                return;
            }

            Order newOrder = new Order();
            newOrder.setCustomer(selectedAppointment.getCustomer());
            newOrder.setVehicle(selectedAppointment.getVehicle());
            newOrder.setDate(LocalDate.now());
            newOrder.setNotes(selectedAppointment.getNotes());

            orderService.createOrder(newOrder);

            // Close this tab dynamically
//            TabPane tabPane = newOrderTab.getTabPane();
//            if (tabPane != null) {
//                Platform.runLater(() -> tabPane.getTabs().remove(newOrderTab));
//            }

            // Open Orders List tab
            EventBus.fireEvent(new OpenOrdersTabEvent());
        });
    }

    private void initLayout() {
        HBox buttonBox = new HBox(updateAppointmentButton, deleteAppointmentButton, createOrder);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(50, 10, 50, 10));
        buttonBox.setSpacing(20);

        // --- First Column: Appointments Table ---
        VBox appointmentTableColumn = new VBox(10);
        appointmentTableColumn.setPadding(new Insets(20));
        appointmentTableColumn.getChildren().addAll(appointmentTable);
        HBox.setHgrow(appointmentTableColumn, Priority.ALWAYS);

        // --- Second Column: Appointment Details ---
        VBox appointmentDetailsColumn = new VBox(10);
        appointmentDetailsColumn.setPadding(new Insets(20));
        appointmentDetailsColumn.getChildren().addAll(
                issueLabel,
                issueArea,
                issueErrorLabel,
                new Label("Select Date & Time*:"),
                datePicker,
                timeComboBox,
                statusComboBox,
                buttonBox);
        HBox.setHgrow(appointmentDetailsColumn, Priority.ALWAYS);

        // --- Main Layout: Arrange columns in an HBox ---
        HBox mainContent = new HBox(30, appointmentTableColumn, appointmentDetailsColumn);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);

        appointmentTableColumn.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.7));
        appointmentDetailsColumn.prefWidthProperty().bind(mainContent.widthProperty().multiply(0.3));

        // --- Final Layout ---
        VBox layout = new VBox(mainContent);
        layout.setPadding(new Insets(20));

        appointmentsTab.setContent(layout);
    }

    private void refreshAppointmentTable() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        appointments.sort(Comparator.comparing(Appointment::getDateTime));
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList(appointments);
        appointmentTable.setItems(appointmentList);
        appointmentTable.refresh();
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

    private boolean isAppointmentModified(Appointment appointment) {
        LocalDate date = datePicker.getValue();
        String[] timeParts = timeComboBox.getValue().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));

        return !Objects.equals(appointment.getDateTime(), localDateTime) ||
                !Objects.equals(appointment.getStatus(), Appointment.Status.valueOf(statusComboBox.getValue())) ||
                !Objects.equals(appointment.getNotes(), issueArea.getText());
    }

}
