package me.skorb.view.tabs;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import me.skorb.entity.*;
import me.skorb.service.AppointmentService;
import me.skorb.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public class DashboardTab {

    private final OrderService orderService = new OrderService();
    private final AppointmentService appointmentService = new AppointmentService();

    private Tab dashboardTab;

    private PieChart ordersPieChart;
    private PieChart appointmentsPieChart;

    private Label openedOrdersTableLabel;
    private TableView<Order> openedOrdersTable;
    private Label upcomingAppointmentsTableLabel;
    private TableView<Appointment> upcomingAppointmentsTable;

    public Tab createDashboardTab() {
        dashboardTab = new Tab("Dashboard");

        initDashboardTabContentView();

        return dashboardTab;
    }

    private void initDashboardTabContentView() {
        initOrdersPieChart();
        initRecentOrdersTable();
        initAppointmentsPieChart();
        initTodayAppointmentsTable();
        initLayout();
    }

    private void initOrdersPieChart() {
        int pendingOrdersCount = orderService.getOrdersCountByStatus("Pending");
        int inProgressOrdersCount = orderService.getOrdersCountByStatus("In_Progress");
        int completedOrdersCount = orderService.getOrdersCountByStatus("Completed");
        int canceledOrdersCount = orderService.getOrdersCountByStatus("Canceled");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("Pending: %d", pendingOrdersCount), pendingOrdersCount),
                new PieChart.Data(String.format("In Progress: %d", inProgressOrdersCount), inProgressOrdersCount),
                new PieChart.Data(String.format("Completed: %d", completedOrdersCount), completedOrdersCount),
                new PieChart.Data(String.format("Canceled: %d", canceledOrdersCount), canceledOrdersCount)
        );

        ordersPieChart = new PieChart(pieChartData);
        ordersPieChart.setTitle("Orders Chart");
        ordersPieChart.setPadding(new Insets(0, 0, 40, 0));
    }

    private void initRecentOrdersTable() {
        openedOrdersTableLabel = new Label("Opened Orders:");
        openedOrdersTableLabel.setPadding(new Insets(0,0,10,0));

        openedOrdersTable = new TableView<>();
        VBox.setVgrow(openedOrdersTable, Priority.ALWAYS);
        openedOrdersTable.setPadding(new Insets(10, 10, 10, 10));
        openedOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Order, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> {
            String date = (cellData.getValue().getDate() == null) ? null : cellData.getValue().getDate().toString();
            return new SimpleStringProperty(date);
        });

        TableColumn<Order, String> customerColumn = new TableColumn<>("Customer");
        customerColumn.setCellValueFactory(cellData -> {
            String firstName = cellData.getValue().getCustomer().getFirstName();
            String lastName = (cellData.getValue().getCustomer().getLastName() == null) ? "" : cellData.getValue().getCustomer().getLastName();
            return new SimpleStringProperty(firstName + " " + lastName);
        });

        TableColumn<Order, String> vehicleColumn = new TableColumn<>("Vehicle");
        vehicleColumn.setCellValueFactory(cellData -> {
            Integer year = cellData.getValue().getVehicle().getYear();
            String yearString = (year != null && year != 0) ? String.valueOf(year) : "";

            Make make = cellData.getValue().getVehicle().getMake();
            String makeString = (make != null) ? make.getName() : "";

            Model model = cellData.getValue().getVehicle().getModel();
            String modelString = (model != null) ? model.getName() : "";

            return new SimpleStringProperty(yearString + " " + makeString + " " + modelString);
        });

        TableColumn<Order, String> costColumn = new TableColumn<>("Total Cost");
        costColumn.setCellValueFactory(cellData -> {
            BigDecimal totalCost = cellData.getValue().getTotalCost();
            String totalCostString = (totalCost != null) ? String.format(Locale.US, "%.2f", totalCost) : "0.00";
            return new SimpleStringProperty(totalCostString);
        });

        TableColumn<Order, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        openedOrdersTable.getColumns().addAll(idColumn, dateColumn, customerColumn, vehicleColumn, statusColumn, costColumn);

        // Load orders data from the database
        List<Order> orders = orderService.getAllOrders()
                .stream()
                .filter(o -> o.getStatus() == Order.Status.In_Progress)
                .toList();
        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
        openedOrdersTable.setItems(orderList);
    }

    private void initAppointmentsPieChart() {
        int scheduledAppointmentsCount = appointmentService.getAppointmentsCountByStatus("Scheduled");
        int canceledAppointmentsCount = appointmentService.getAppointmentsCountByStatus("Canceled");
        int completedAppointmentsCount = appointmentService.getAppointmentsCountByStatus("Completed");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("Scheduled: %d", scheduledAppointmentsCount), scheduledAppointmentsCount),
                new PieChart.Data(String.format("Canceled: %d", canceledAppointmentsCount), canceledAppointmentsCount),
                new PieChart.Data(String.format("Completed: %d", completedAppointmentsCount), completedAppointmentsCount)
        );

        appointmentsPieChart = new PieChart(pieChartData);
        appointmentsPieChart.setTitle("Appointments Chart");
        appointmentsPieChart.setPadding(new Insets(0, 0, 40, 0));
    }

    private void initTodayAppointmentsTable() {
        upcomingAppointmentsTableLabel = new Label("Upcoming Appointments:");
        upcomingAppointmentsTableLabel.setPadding(new Insets(0,0,10,0));

        upcomingAppointmentsTable = new TableView<>();
        VBox.setVgrow(upcomingAppointmentsTable, Priority.ALWAYS);
        upcomingAppointmentsTable.setPadding(new Insets(10, 10, 10, 10));
        upcomingAppointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        upcomingAppointmentsTable.getColumns().addAll(/*idColumn,*/ dateColumn, timeColumn, firstNameColumn, lastNameColumn, makeColumn, modelColumn, yearColumn, statusColumn);

        List<Appointment> upcomingAppointments = appointmentService.getAppointmentsBetweenDates(LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 1)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59)));
        ObservableList<Appointment> appointmentList = FXCollections.observableArrayList(upcomingAppointments);
        upcomingAppointmentsTable.setItems(appointmentList);
    }

    private void initLayout() {
        VBox ordersColumn = new VBox();
        ordersColumn.setPadding(new Insets(10, 15, 30, 15));
        ordersColumn.getChildren().addAll(ordersPieChart, openedOrdersTableLabel, openedOrdersTable);
        HBox.setHgrow(ordersColumn, Priority.ALWAYS);

        VBox appointmentsColumn = new VBox();
        appointmentsColumn.setPadding(new Insets(10, 15, 30, 15));
        appointmentsColumn.getChildren().addAll(appointmentsPieChart, upcomingAppointmentsTableLabel, upcomingAppointmentsTable);
        HBox.setHgrow(appointmentsColumn, Priority.ALWAYS);

        GridPane mainPane = new GridPane();
        mainPane.setVgap(10);
        mainPane.add(ordersColumn, 0, 0);
        mainPane.add(appointmentsColumn, 1, 0);

        GridPane.setVgrow(ordersColumn, Priority.ALWAYS);
        GridPane.setVgrow(appointmentsColumn, Priority.ALWAYS);

        // Ensure mainPane itself stretches across the full tab width
        mainPane.setPrefWidth(Double.MAX_VALUE);

        dashboardTab.setContent(mainPane);
    }

}
