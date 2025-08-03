package me.skorb.view.screens;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import me.skorb.entity.*;
import me.skorb.service.AppointmentService;
import me.skorb.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public class DashboardView extends HBox {

    private final OrderService orderService = new OrderService();
    private final AppointmentService appointmentService = new AppointmentService();

    private PieChart ordersPieChart;
    private PieChart appointmentsPieChart;

    private TableView<Order> openedOrdersTable;
    private TableView<Appointment> upcomingAppointmentsTable;

    public DashboardView() {
        initOrdersPieChart();
        initAppointmentsPieChart();
        initRecentOrdersTable();
        initTodayAppointmentsTable();
        buildLayout();
    }

    private void initOrdersPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Pending", orderService.getOrdersCountByStatus("Pending")),
                new PieChart.Data("In Progress", orderService.getOrdersCountByStatus("In_Progress")),
                new PieChart.Data("Completed", orderService.getOrdersCountByStatus("Completed")),
                new PieChart.Data("Canceled", orderService.getOrdersCountByStatus("Canceled"))
        );
        ordersPieChart = new PieChart(pieChartData);
        ordersPieChart.setTitle("Orders");
        ordersPieChart.setPadding(new Insets(0, 0, 20, 0));
    }

    private void initAppointmentsPieChart() {
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Scheduled", appointmentService.getAppointmentsCountByStatus("Scheduled")),
                new PieChart.Data("Canceled", appointmentService.getAppointmentsCountByStatus("Canceled")),
                new PieChart.Data("Completed", appointmentService.getAppointmentsCountByStatus("Completed"))
        );
        appointmentsPieChart = new PieChart(pieChartData);
        appointmentsPieChart.setTitle("Appointments");
        appointmentsPieChart.setPadding(new Insets(0, 0, 20, 0));
    }

    private void initRecentOrdersTable() {
        openedOrdersTable = new TableView<>();
        openedOrdersTable.setPlaceholder(new Label("No Opened Orders Found"));
        openedOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        openedOrdersTable.setStyle("-fx-background-radius: 10;");
        VBox.setVgrow(openedOrdersTable, Priority.ALWAYS);

        openedOrdersTable.getColumns().addAll(
                createColumn("ID", "id"),
                createColumn("Date", o -> o.getDate() != null ? o.getDate().toString() : ""),
                createColumn("Customer", o -> o.getCustomer().getFirstName() + " " + o.getCustomer().getLastName()),
                createColumn("Vehicle", o -> {
                    Vehicle v = o.getVehicle();
                    return (v.getYear() != null ? v.getYear() : "") + " " +
                            (v.getMake() != null ? v.getMake().getName() : "") + " " +
                            (v.getModel() != null ? v.getModel().getName() : "");
                }),
                createColumn("Status", "status"),
                createColumn("Total Cost", o -> {
                    BigDecimal cost = o.getTotalCost();
                    return (cost != null) ? String.format(Locale.US, "%.2f", cost) : "0.00";
                })
        );

        List<Order> orders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == Order.Status.In_Progress)
                .toList();
        openedOrdersTable.setItems(FXCollections.observableArrayList(orders));
    }

    private void initTodayAppointmentsTable() {
        upcomingAppointmentsTable = new TableView<>();
        upcomingAppointmentsTable.setPlaceholder(new Label("No Upcoming Appointments Found"));
        upcomingAppointmentsTable.setStyle("-fx-background-radius: 10;");
        upcomingAppointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(upcomingAppointmentsTable, Priority.ALWAYS);

        upcomingAppointmentsTable.getColumns().addAll(
                createColumn("Date", a -> a.getDateTime().toLocalDate().toString()),
                createColumn("Time", a -> String.format("%02d:%02d", a.getDateTime().getHour(), a.getDateTime().getMinute())),
                createColumn("First Name", a -> a.getCustomer().getFirstName()),
                createColumn("Last Name", a -> a.getCustomer().getLastName()),
                createColumn("Make", a -> {
                    Vehicle v = a.getVehicle();
                    return (v != null && v.getMake() != null) ? v.getMake().getName() : "";
                }),
                createColumn("Model", a -> {
                    Vehicle v = a.getVehicle();
                    return (v != null && v.getModel() != null) ? v.getModel().getName() : "";
                }),
                createColumn("Year", a -> {
                    Vehicle v = a.getVehicle();
                    return (v != null && v.getYear() != null && v.getYear() != 0) ? v.getYear().toString() : "";
                }),
                createColumn("Status", "status")
        );

        List<Appointment> appointments = appointmentService.getAppointmentsBetweenDates(
                LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 1)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59))
        );
        upcomingAppointmentsTable.setItems(FXCollections.observableArrayList(appointments));
    }

    private void buildLayout() {
        VBox ordersBox = new VBox(10, ordersPieChart, new Label("Opened Orders:"), openedOrdersTable);
        VBox appointmentsBox = new VBox(10, appointmentsPieChart, new Label("Upcoming Appointments:"), upcomingAppointmentsTable);
        ordersBox.setPadding(new Insets(10));
        appointmentsBox.setPadding(new Insets(10));
        setSpacing(15);
        getChildren().addAll(ordersBox, appointmentsBox);
        HBox.setHgrow(ordersBox, Priority.ALWAYS);
        HBox.setHgrow(appointmentsBox, Priority.ALWAYS);
    }

    // --- Generic column creation ---
    private <T> TableColumn<T, String> createColumn(String title, String property) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private <T> TableColumn<T, String> createColumn(String title, java.util.function.Function<T, String> extractor) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(extractor.apply(cell.getValue())));
        return col;
    }
}
