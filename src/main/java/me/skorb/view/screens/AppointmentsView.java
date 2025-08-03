package me.skorb.view.screens;

import static me.skorb.view.ViewUtils.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.skorb.entity.Appointment;
import me.skorb.entity.Order;
import me.skorb.entity.Vehicle;
import me.skorb.service.AppointmentService;
import me.skorb.service.OrderService;
import me.skorb.view.ViewUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class AppointmentsView extends HBox {

    private final AppointmentService appointmentService = new AppointmentService();
    private final OrderService orderService = new OrderService();

    private Button newAppointmentButton;
    private TableView<Appointment> appointmentTable;
    private Label detailsLabel;
    private Label detailsErrorLabel;
    private TextArea detailsArea;
    private DatePicker datePicker;
    private ComboBox<String> timeComboBox;
    private LocalDateTime selectedDateTime;
    private ComboBox<String> statusComboBox;
    private Button updateAppointmentButton;
    private Button deleteAppointmentButton;
    private Button createOrder;

    public AppointmentsView() {
        initTopButtons();
        initAppointmentTable();
        initAppointmentDetailsArea();
        initLayout();
    }

    private void initTopButtons() {
        newAppointmentButton = new Button("+ New Appointment");
        newAppointmentButton.setStyle("-fx-background-color: white; " +
                "-fx-text-fill: #2D5A90; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;");
        newAppointmentButton.setCursor(Cursor.HAND);
        newAppointmentButton.setOnAction(event -> {
            NewAppointmentView newAppointmentView = new NewAppointmentView();

            // Создаем новое модальное окно
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Create New Appointment");

            // Устанавливаем содержимое окна
            Scene scene = new Scene(newAppointmentView);
            modalStage.setScene(scene);
            modalStage.setMinWidth(900);
            modalStage.setMinHeight(600);

            // Показываем окно и ждем его закрытия
            modalStage.showAndWait();

            refreshAppointmentTable();
        });
    }

    private void initAppointmentTable() {
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

        appointmentTable = new TableView<>();
        VBox.setVgrow(appointmentTable, Priority.ALWAYS);
        appointmentTable.setPlaceholder(new Label("No Appointments Found"));
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
                detailsArea.setText("");
                detailsErrorLabel.setText("");
                datePicker.setValue(null);
                timeComboBox.setValue(null);
                statusComboBox.setValue(null);

                detailsArea.setDisable(true);
                datePicker.setDisable(true);
                timeComboBox.setDisable(true);
                statusComboBox.setDisable(true);
                updateAppointmentButton.setDisable(true);
                deleteAppointmentButton.setDisable(true);
                createOrder.setDisable(true);
            } else {
                detailsArea.setDisable(false);
                datePicker.setDisable(false);
                timeComboBox.setDisable(false);
                statusComboBox.setDisable(false);
                updateAppointmentButton.setDisable(false);
                deleteAppointmentButton.setDisable(false);
                createOrder.setDisable(false);

                detailsArea.setText(selectedAppointment.getNotes());

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
        detailsLabel = new Label("Issue Description*:");
        detailsErrorLabel = new Label();
        detailsErrorLabel.setStyle("-fx-text-fill: red;");
        detailsArea = new TextArea();
        detailsArea.setDisable(true);

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

            List<String> errors = new ArrayList<>();
            if (!verifyRequiredFieldIsNotEmpty(detailsArea)) {
                errors.add("Appointment Details is required.");
            }
            if (datePicker.getValue() == null) {
                errors.add("Appointment Date is required.");
            }
            if (timeComboBox.getValue() == null) {
                errors.add("Appointment Time is required.");
            }
            if (statusComboBox.getValue() == null) {
                errors.add("Appointment Status is required.");
            }
            if (!errors.isEmpty()) {
                showToastPopup(errors, ToastType.ERROR, appointmentTable);
                event.consume();
                return;
            }

            selectedAppointment.setNotes(detailsArea.getText());

            updateSelectedDateTime();

            selectedAppointment.setDateTime(selectedDateTime);
            selectedAppointment.setStatus(Appointment.Status.valueOf(statusComboBox.getValue()));

            appointmentService.updateAppointment(selectedAppointment);

            // Refresh appointment list
            refreshAppointmentTable();

            showToastPopup("Appointment has been successfully updated.", ToastType.SUCCESS, appointmentTable);
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

            int newOrderId = orderService.createOrder(newOrder);
            if (newOrderId != -1) {
                newOrder.setId(newOrderId);
                // TODO: Open order list view
            } else {
                // TODO: Show pop-up error message with order creation fail
            }
        });
    }

    private void initLayout() {
        HBox buttonBox = new HBox(updateAppointmentButton, deleteAppointmentButton, createOrder);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(50, 10, 50, 10));
        buttonBox.setSpacing(20);

        // --- First Column: Appointments Table ---
        HBox topButtonsLayout = new HBox(10, newAppointmentButton);
        VBox appointmentTableColumn = new VBox(10);
        appointmentTableColumn.setPadding(new Insets(20));
        appointmentTableColumn.getChildren().addAll(topButtonsLayout, appointmentTable);
        HBox.setHgrow(appointmentTableColumn, Priority.ALWAYS);

        // --- Second Column: Appointment Details ---
        VBox appointmentDetailsColumn = new VBox(10);
        appointmentDetailsColumn.setPadding(new Insets(20));
        appointmentDetailsColumn.getChildren().addAll(
                detailsLabel,
                detailsArea,
                new Label("Select Date & Time*:"),
                datePicker,
                timeComboBox,
                statusComboBox,
                buttonBox);
        HBox.setHgrow(appointmentDetailsColumn, Priority.ALWAYS);

        // --- Main Layout: Arrange columns in an HBox ---
        setSpacing(10);
        getChildren().addAll(appointmentTableColumn, appointmentDetailsColumn);
        setPadding(new Insets(10));
        setAlignment(Pos.TOP_CENTER);

        appointmentTableColumn.prefWidthProperty().bind(widthProperty().multiply(0.7));
        appointmentDetailsColumn.prefWidthProperty().bind(widthProperty().multiply(0.3));
    }

    private void refreshAppointmentTable() {
        Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
        int selectedId = (selectedAppointment != null) ? selectedAppointment.getId() : -1;

        List<Appointment> appointments = appointmentService.getAllAppointments();
        appointments.sort(Comparator.comparing(Appointment::getDateTime));
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList(appointments);
        appointmentTable.setItems(appointmentList);
        if (selectedId != -1) {
            for (Appointment appt : appointmentList) {
                if (appt.getId() == selectedId) {
                    appointmentTable.getSelectionModel().select(appt);
                    break;
                }
            }
        }
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
                !Objects.equals(appointment.getNotes(), detailsArea.getText());
    }

}
