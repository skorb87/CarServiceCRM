package me.skorb.view.screens;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import me.skorb.entity.*;
import me.skorb.service.EmployeeService;
import me.skorb.service.OrderService;
import me.skorb.service.PartService;
import me.skorb.service.ServiceService;
import me.skorb.util.InvoiceUtils;
import me.skorb.view.tabs.PhotoUtils;
import me.skorb.view.ViewUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static me.skorb.view.ViewUtils.ToastType;
import static me.skorb.view.ViewUtils.showToastPopup;

public class OrdersView extends GridPane {

    private static final Logger logger = LogManager.getLogger(me.skorb.view.tabs.OrdersTab.class);

    private final OrderService orderService = new OrderService();
    private final EmployeeService employeeService = new EmployeeService();
    private final ServiceService serviceService = new ServiceService();
    private final PartService partService = new PartService();

    private Button newOrderButton;

    private TableView<Order> ordersTable;

    private Label customerLabel;
    private Label customerNameLabel;
    private Label vehicleLabel;
    private Label vehicleInfoLabel;

    private Label orderTypeLabel;
    private ComboBox<Order.Type> orderTypeBox;

    private Label orderStatusLabel;
    private ComboBox<Order.Status> orderStatusBox;

    private Label orderDateLabel;
    private DatePicker orderDatePicker;

    private Label odometerLabel;
    private TextField odometerField;

    private Label notesLabel;
    private TextArea notesArea;

    private Label servicesTableLabel;
    private ComboBox<Service> addServiceBox;
    private TableView<Service> servicesTable;

    private Label partsTableLabel;
    private ComboBox<Part> addPartBox;
    private TableView<Part> partsTable;

    private Label employeesTableLabel;
    private ComboBox<Employee> addEmployeeBox;
    private TableView<Employee> employeesTable;

    private Button attachPhotosButton;
    private Button showPhotosButton;
    private Button printInvoiceButton;
    private Button updateOrderButton;
    private Button deleteOrderButton;

    private List<Service> selectedServices;
    private Map<Part, Integer> selectedPartsWithQuantities;
    private Set<Employee> selectedEmployees;

    public OrdersView() {
        initTopButtons();
        initOrdersTable();
        initLayout();
    }

    private void initTopButtons() {
        newOrderButton = new Button("+ New Order");
        newOrderButton.setStyle("-fx-background-color: white; " +
                "-fx-text-fill: #2D5A90; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;");
        newOrderButton.setCursor(Cursor.HAND);
        newOrderButton.setOnAction(event -> {
            NewOrderView newOrderView = new NewOrderView();

            // Создаем новое модальное окно
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Create New Order");

            // Устанавливаем содержимое окна
            Scene scene = new Scene(newOrderView);
            modalStage.setScene(scene);
            modalStage.setMinWidth(900);
            modalStage.setMinHeight(600);

            // Показываем окно и ждем его закрытия
            modalStage.showAndWait();

            refreshOrdersTable();
        });
    }

    private void initOrdersTable() {
        ordersTable = new TableView<>();
        VBox.setVgrow(ordersTable, Priority.ALWAYS);
        ordersTable.setPadding(new Insets(10, 10, 10, 10));
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        // Create the Status Column as a Dropdown (ComboBox)
        TableColumn<Order, Order.Status> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setEditable(true);
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final HBox hbox = new HBox(20);
            private final Circle statusCircle = new Circle(6); // Радиус кружка
            private final ComboBox<Order.Status> comboBox = new ComboBox<>();
            private final EventHandler<ActionEvent> comboBoxHandler = event -> {
                Order order = getTableView().getItems().get(getIndex());
                if (order != null) {
                    Order.Status oldStatus = order.getStatus();
                    Order.Status newStatus = comboBox.getValue();

                    if (oldStatus != newStatus) {
                        order.setStatus(newStatus); // Update Order status

                        try {
                            orderService.updateOrder(order); // We are trying to update order status in the database
                            statusCircle.setFill(getStatusColor(newStatus)); // Updating order status circle color

                            String orderUpdateMessage = String.format("Order #%d status updated to: %s", order.getId(), newStatus);
                            showToastPopup(orderUpdateMessage, ToastType.SUCCESS, ordersTable);
                        } catch (Exception e) {
                            // Rollback if we failed to update order status in the database
                            order.setStatus(oldStatus);
                            comboBox.setValue(oldStatus);
                            statusCircle.setFill(getStatusColor(oldStatus));

                            String orderUpdateMessage = String.format("Failed to update Order #%d status to: %s", order.getId(), newStatus);
                            showToastPopup(orderUpdateMessage, ToastType.ERROR, ordersTable);
                        }

                        getTableView().refresh(); // Refresh UI
                    }
                }
            };

            {
                comboBox.setItems(FXCollections.observableArrayList(Order.Status.values()));
                comboBox.setScaleY(0.9); // Shrinks the height of the ComboBox
                comboBox.setStyle("-fx-font-size: 12px; -fx-padding: 0px;");
                HBox.setHgrow(comboBox, Priority.ALWAYS);
                comboBox.setOnAction(comboBoxHandler);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().addAll(statusCircle, comboBox);
            }

            @Override
            protected void updateItem(Order.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    comboBox.setOnAction(null);             // ⛔ отключаем временно
                    comboBox.setValue(status);              // 🔄 обновляем UI
                    comboBox.setOnAction(comboBoxHandler);  // ✅ снова включаем

                    statusCircle.setFill(getStatusColor(status));
                    setGraphic(hbox);
                }
            }
        });

        TableColumn<Order, String> serviceSummaryColumn = new TableColumn<>("Service Summary");
        serviceSummaryColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue();
            List<Service> services = order.getServicesProvided();
            if (services == null || services.isEmpty()) {
                return new SimpleStringProperty("-");
            }

            String summary = services.stream()
                    .map(Service::getName)
                    .limit(3) // показываем максимум 3 услуги
                    .collect(Collectors.joining(", "));

            if (services.size() > 3) {
                summary += ", ...";
            }

            return new SimpleStringProperty(summary);
        });

        // "Service Summary" column tooltip that shows all services provided for selected order
        serviceSummaryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String summaryText, boolean empty) {
                super.updateItem(summaryText, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(summaryText);

                    Order order = getTableRow().getItem();
                    List<Service> services = order.getServicesProvided();

                    if (services != null && !services.isEmpty()) {
                        String fullList = services.stream()
                                .map(Service::getName)
                                .collect(Collectors.joining("\n"));
                        Tooltip tooltip = new Tooltip(fullList);
                        tooltip.setWrapText(true);
                        tooltip.setMaxWidth(300);
                        setTooltip(tooltip);
                    } else {
                        setTooltip(null);
                    }
                }
            }
        });

        TableColumn<Order, Void> invoiceColumn = new TableColumn<>("Invoice");
        invoiceColumn.setCellFactory(col -> new TableCell<>() {
            private final Button invoiceButton = new Button("📄"); // Unicode document icon

            {
                invoiceButton.setScaleX(0.8);
                invoiceButton.setScaleY(0.8);
                invoiceButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    File invoiceFile = InvoiceUtils.generateInvoice(order);
                    // Open the generated PDF
                    try {
                        Desktop.getDesktop().open(invoiceFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(invoiceButton);
                }
            }
        });



        TableColumn<Order, Void> deleteColumn = new TableColumn<>("");
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("\uD83D\uDDD1");

            {
                deleteButton.setStyle(
                        "-fx-background-color: transparent;" +  // убирает фон
                                "-fx-padding: 0;" +                     // убирает отступы
                                "-fx-font-size: 24px;" +                // размер иконки
                                "-fx-text-fill: #e74c3c;" +             // цвет иконки (например, красный)
                                "-fx-cursor: hand;"                     // курсор при наведении
                );
                deleteButton.setMaxHeight(Double.MAX_VALUE);
                deleteButton.setAlignment(Pos.CENTER);
                deleteButton.setCursor(Cursor.HAND);
                deleteButton.setScaleY(0.9);
                deleteButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order != null) {
                        // Подтверждение удаления
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete Confirmation");
                        alert.setHeaderText("Are you sure you want to delete Order #" + order.getId() + "?");
                        alert.setContentText("This action cannot be undone.");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            orderService.deleteOrder(order);
                            refreshOrdersTable();
                            showToastPopup("Order #" + order.getId() + " deleted", ToastType.SUCCESS, ordersTable);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-padding: 0;"); // убирает отступы у ячейки
                }
            }
        });

        // Enable editing for the Status column
        ordersTable.setEditable(true);

        ordersTable.getColumns().addAll(idColumn, dateColumn, customerColumn, vehicleColumn,
                statusColumn, serviceSummaryColumn, costColumn, invoiceColumn, deleteColumn);

        ordersTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();

            idColumn.setPrefWidth(totalWidth * 0.05);          // 5%
            dateColumn.setPrefWidth(totalWidth * 0.10);        // 10%
            customerColumn.setPrefWidth(totalWidth * 0.15);    // 15%
            vehicleColumn.setPrefWidth(totalWidth * 0.15);     // 15%
            statusColumn.setPrefWidth(totalWidth * 0.10);      // 10%
            serviceSummaryColumn.setPrefWidth(totalWidth * 0.30); // 30%
            costColumn.setPrefWidth(totalWidth * 0.05);        // 5%
            invoiceColumn.setPrefWidth(totalWidth * 0.05);     // 5%
            deleteColumn.setPrefWidth(totalWidth * 0.05);      // 5%
        });


        // Load orders from the database
        List<Order> orders = orderService.getAllOrders();
        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
        ordersTable.setItems(orderList);

        ordersTable.setRowFactory(tableView -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Order selectedOrder = row.getItem();
                    openOrderDetailsWindow(selectedOrder);
                }
            });
            return row;
        });
    }

    private void openOrderDetailsWindow(Order order) {
        selectedServices = new ArrayList<>(order.getServicesProvided());
        selectedPartsWithQuantities = new HashMap<>(order.getPartsWithQuantities());
        selectedEmployees = new HashSet<>(order.getEmployeesAssigned());

        customerLabel = new Label("Customer: ");
        customerLabel.setMinWidth(100);
        customerNameLabel = new Label(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
        customerNameLabel.setMinWidth(220);

        vehicleLabel = new Label("Vehicle: ");
        vehicleLabel.setMinWidth(100);
        vehicleInfoLabel = new Label( + order.getVehicle().getYear() + " " + order.getVehicle().getMake().getName() + " " +
                order.getVehicle().getModel().getName());
        vehicleInfoLabel.setMinWidth(220);

        orderTypeLabel = new Label("Order Type*:");
        orderTypeLabel.setMinWidth(100);
        orderTypeBox = new ComboBox<>();
        orderTypeBox.setMinWidth(220);
        orderTypeBox.setItems(FXCollections.observableArrayList(Order.Type.values()));
        orderTypeBox.setPromptText("Choose Order Type...");
        orderTypeBox.setValue(order.getType());

        orderStatusLabel = new Label("Order Status*:");
        orderStatusLabel.setMinWidth(100);
        orderStatusBox = new ComboBox<>();
        orderStatusBox.setMinWidth(220);
        orderStatusBox.setItems(FXCollections.observableArrayList(Order.Status.values()));
        orderStatusBox.setPromptText("Choose Order Status...");
        orderStatusBox.setValue(order.getStatus());

        orderDateLabel = new Label("Order Date*:");
        orderDateLabel.setMinWidth(100);
        orderDatePicker = new DatePicker(order.getDate());
        orderDatePicker.setMinWidth(220);
        orderDatePicker.setPromptText("Order date");
        orderDatePicker.setEditable(false);

        odometerLabel = new Label("Odometer:");
        odometerLabel.setMinWidth(100);
        odometerField = new TextField();
        odometerField.setMinWidth(220);
        odometerField.setText(Integer.toString(order.getOdometer()));

        notesLabel = new Label("Order Notes:");
        notesArea = new TextArea();
        notesArea.setText(order.getNotes());

        initServicesTableView();
        initPartsTableView();
        initEmployeesTableView();
        initAttachPhotosButton();
        initShowPhotosButton();
        initPrintInvoiceButton();
        initUpdateOrderButton();
        initDeleteOrderButton();


        HBox customerInfoLayout = new HBox(10, customerLabel, customerNameLabel);
        HBox vehicleInfoLayout = new HBox(10, vehicleLabel, vehicleInfoLabel);
        HBox orderTypeLayout = new HBox(10, orderTypeLabel, orderTypeBox);
        HBox orderStatusLayout = new HBox(10, orderStatusLabel, orderStatusBox);
        HBox orderDateLayout = new HBox(10, orderDateLabel, orderDatePicker);
        HBox odometerLayout = new HBox(10, odometerLabel, odometerField);
        VBox orderInfoLayout = new VBox(10, customerInfoLayout, vehicleInfoLayout, orderTypeLayout, orderStatusLayout, orderDateLayout, odometerLayout);
        HBox.setHgrow(orderInfoLayout, Priority.ALWAYS);
        VBox orderNotesLayout = new VBox(10, notesLabel, notesArea);

        HBox topLayout = new HBox(20, orderInfoLayout, orderNotesLayout);
        topLayout.setPadding(new Insets(10, 15, 10, 15));
        topLayout.setAlignment(Pos.TOP_CENTER);
        orderInfoLayout.prefWidthProperty().bind(topLayout.widthProperty().divide(2));
        orderNotesLayout.prefWidthProperty().bind(topLayout.widthProperty().divide(2));


        // --- Bottom Content First Column: Services Provided ---
        Region servicesSpacer = new Region();
        HBox.setHgrow(servicesSpacer, Priority.ALWAYS);
        HBox servicesProvidedTopLayout = new HBox(20, servicesTableLabel, servicesSpacer, addServiceBox);
        VBox servicesProvidedLayout = new VBox(10, servicesProvidedTopLayout, servicesTable);

        // --- Bottom Content Second Column: Parts Used ---
        Region partsSpacer = new Region();
        HBox.setHgrow(partsSpacer, Priority.ALWAYS);
        HBox partsUsedTopLayout = new HBox(20, partsTableLabel, partsSpacer, addPartBox);
        VBox partsUsedLayout = new VBox(10, partsUsedTopLayout, partsTable);

        // --- Bottom Content Third Column: Employees Assigned ---
        Region employeesSpacer = new Region();
        HBox.setHgrow(employeesSpacer, Priority.ALWAYS);
        HBox employeesAssignedTopLayout = new HBox(20, employeesTableLabel, employeesSpacer, addEmployeeBox);
        VBox employeesAssignedLayout = new VBox(10, employeesAssignedTopLayout, employeesTable);

        // --- Bottom Layout: Arrange bottom tables content in an HBox ---
        HBox servicesPartsEmployeesLayout = new HBox(20, servicesProvidedLayout, partsUsedLayout, employeesAssignedLayout);
        servicesPartsEmployeesLayout.setPadding(new Insets(10, 15, 10, 15));
        servicesPartsEmployeesLayout.setAlignment(Pos.TOP_CENTER);
        servicesProvidedLayout.prefWidthProperty().bind(servicesPartsEmployeesLayout.widthProperty().divide(2.5));
        partsUsedLayout.prefWidthProperty().bind(servicesPartsEmployeesLayout.widthProperty().divide(2.5));
        employeesAssignedLayout.prefWidthProperty().bind(servicesPartsEmployeesLayout.widthProperty().divide(5));

        HBox buttonBox = new HBox(showPhotosButton, updateOrderButton, deleteOrderButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 10, 50, 10));
        buttonBox.setSpacing(20);

        // --- Assemble Bottom part UI (order fields with order creating button) ---
        VBox bottomLayout = new VBox(10, servicesPartsEmployeesLayout, buttonBox);
        bottomLayout.setPadding(new Insets(10, 15, 10, 15));

        // mainPane is a GridPane to control height distribution between customerTable and detailsPane
        GridPane mainPane = new GridPane();
        mainPane.setVgap(10);
        mainPane.add(topLayout, 0, 0);
        mainPane.add(bottomLayout, 0, 1);
        mainPane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        mainPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Scene scene = new Scene(mainPane, 800, 600);

        // Resize mainPane to the Scene size
        mainPane.prefWidthProperty().bind(scene.widthProperty());
        mainPane.prefHeightProperty().bind(scene.heightProperty());
        GridPane.setHgrow(topLayout, Priority.ALWAYS);
        GridPane.setVgrow(topLayout, Priority.ALWAYS);
        GridPane.setHgrow(bottomLayout, Priority.ALWAYS);
        GridPane.setVgrow(bottomLayout, Priority.ALWAYS);

        VBox.setVgrow(servicesTable, Priority.ALWAYS);
        VBox.setVgrow(partsTable, Priority.ALWAYS);
        VBox.setVgrow(employeesTable, Priority.ALWAYS);

        Stage orderDetailsWindow = new Stage();
        orderDetailsWindow.setTitle("Order #" + order.getId());
        orderDetailsWindow.setScene(scene);
        orderDetailsWindow.initModality(Modality.APPLICATION_MODAL); // Blocks main window
        orderDetailsWindow.show();
    }

    private void initServicesTableView() {
        servicesTableLabel = new Label("Service(s) provided:");

        List<Service> allServices = serviceService.getAllServices();
        ObservableList<Service> allServiceList = FXCollections.observableArrayList(allServices);
        FilteredList<Service> filteredList = new FilteredList<>(allServiceList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addServiceBox = new ComboBox<>();
        addServiceBox.setItems(filteredList);
        addServiceBox.setEditable(true);
        addServiceBox.setPromptText("Add Service...");

        // Конвертер: отображение имени в списке и обратный поиск
        addServiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Service service) {
                return service != null ? service.getName() : "";
            }

            @Override
            public Service fromString(String name) {
                return allServices.stream()
                        .filter(s -> s.getName().equalsIgnoreCase(name.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        // 🔍 Обновление фильтра при вводе текста
        addServiceBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Избегаем циклического обновления
            // Platform.runLater "разрывает цикл" между обновлением predicate и реакцией ComboBox
            Platform.runLater(() -> {
                String lower = newText.toLowerCase().trim();
                filteredList.setPredicate(service ->
                        lower.isEmpty() || service.getName().toLowerCase().contains(lower));
            });
        });

        addServiceBox.setOnAction(event -> {
            String input = addServiceBox.getEditor().getText().trim();
            Service selected = allServices.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);

            if (selected != null && !selectedServices.contains(selected)) {
                selectedServices.add(selected);
                servicesTable.getItems().add(selected);
                servicesTable.refresh();
            }

            /*
             * ComboBox внутри использует ListView.
             * При вызове setValue(null) сразу в valueProperty().addListener(...) мы вмешиваемся в момент,
             * когда JavaFX ещё не завершил обработку предыдущего выбора.
             * Отложенный вызов через Platform.runLater позволяет дождаться завершения всех внутренних действий
             * и только потом безопасно сбрасывает выбор.
             * */
            Platform.runLater(() -> {
                addServiceBox.setValue(null);
                addServiceBox.getEditor().setText(""); // на всякий случай
                addServiceBox.getEditor().clear();       // очистка текста
                addServiceBox.getParent().requestFocus(); // убираем фокус с ComboBox
            });
        });


        servicesTable = new TableView<>();
        servicesTable.setItems(FXCollections.observableArrayList(selectedServices));
        servicesTable.setEditable(true);
        servicesTable.setStyle("-fx-table-cell-border-color: transparent; -fx-border-color: transparent;");

        TableColumn<Service, String> nameColumn = new TableColumn<>("Service Name");
        nameColumn.setResizable(false);
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        // Колонка с ценой (BigDecimal через строку)
        TableColumn<Service, String> priceColumn = new TableColumn<>("Price  \uD83D\uDD89");
        priceColumn.setResizable(false);

        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPrice() != null ? data.getValue().getPrice().toString() : "0.00")
        );

        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        priceColumn.setOnEditCommit(event -> {
            Service service = event.getRowValue();
            String newValue = event.getNewValue();
            try {
                BigDecimal parsed = new BigDecimal(newValue);
                service.setPrice(parsed);
            } catch (NumberFormatException e) {
                logger.warn("Invalid price entered: {}", newValue);
            }
            servicesTable.refresh();
        });

        // Колонка "Удалить"
        TableColumn<Service, Void> removeColumn = new TableColumn<>("");
        removeColumn.setResizable(false);
        removeColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("✖");

            {
                removeButton.setStyle("-fx-font-size: 12px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeButton.setCursor(Cursor.HAND);
                removeButton.setScaleY(0.9);
                removeButton.setOnAction(e -> {
                    Service service = getTableView().getItems().get(getIndex());
                    selectedServices.remove(service);
                    servicesTable.getItems().remove(service);
                    servicesTable.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        servicesTable.getColumns().addAll(nameColumn, priceColumn, removeColumn);

        servicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        servicesTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double totalWidth = newVal.doubleValue();
            nameColumn.setPrefWidth(totalWidth * 0.79);
            priceColumn.setPrefWidth(totalWidth * 0.12);
            removeColumn.setPrefWidth(totalWidth * 0.08);
        });
    }

    private void initPartsTableView() {
        partsTableLabel = new Label("Part(s) used:");

        // Fetch all Parts from the database
        List<Part> allParts = partService.getAllParts();
        ObservableList<Part> allPartsList = FXCollections.observableArrayList(allParts);
        FilteredList<Part> filteredList = new FilteredList<>(allPartsList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addPartBox = new ComboBox<>();
        addPartBox.setItems(filteredList);
        addPartBox.setEditable(true);
        addPartBox.setPromptText("Add Part...");

        // Конвертер: отображение имени в списке и обратный поиск
        addPartBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Part part) {
                return part != null ? part.getName() : "";
            }

            @Override
            public Part fromString(String name) {
                return allParts.stream()
                        .filter(s -> s.getName().equalsIgnoreCase(name.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        // 🔍 Обновление фильтра при вводе текста
        addPartBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Избегаем циклического обновления
            // Platform.runLater "разрывает цикл" между обновлением predicate и реакцией ComboBox
            Platform.runLater(() -> {
                String lower = newText.toLowerCase().trim();
                filteredList.setPredicate(part ->
                        lower.isEmpty() || part.getName().toLowerCase().contains(lower));
            });
        });

        addPartBox.setOnAction(event -> {
            String input = addPartBox.getEditor().getText().trim();
            Part selected = allParts.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);

//            if (selected != null && !selectedServices.contains(selected)) {
            if (selected != null && !selectedPartsWithQuantities.containsKey(selected)) {
                selectedPartsWithQuantities.put(selected, 1);
                partsTable.getItems().add(selected);
                partsTable.refresh();
            }

            /*
             * ComboBox внутри использует ListView.
             * При вызове setValue(null) сразу в valueProperty().addListener(...) мы вмешиваемся в момент,
             * когда JavaFX ещё не завершил обработку предыдущего выбора.
             * Отложенный вызов через Platform.runLater позволяет дождаться завершения всех внутренних действий
             * и только потом безопасно сбрасывает выбор.
             * */
            Platform.runLater(() -> {
                addPartBox.setValue(null);
                addPartBox.getEditor().setText(""); // на всякий случай
                addPartBox.getEditor().clear();       // очистка текста
                addPartBox.getParent().requestFocus(); // убираем фокус с ComboBox
            });
        });

        partsTable = new TableView<>();
        partsTable.setItems(FXCollections.observableArrayList(selectedPartsWithQuantities.keySet()));
        partsTable.setEditable(true);
        partsTable.setStyle("-fx-table-cell-border-color: transparent; -fx-border-color: transparent;");

        TableColumn<Part, String> nameColumn = new TableColumn<>("Part Name");
        nameColumn.setResizable(false);
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        // Колонка с ценой (BigDecimal через строку)
        TableColumn<Part, String> priceColumn = new TableColumn<>("Price  \uD83D\uDD89");
        priceColumn.setResizable(false);
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPrice() != null ? data.getValue().getPrice().toString() : "0.00")
        );
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        priceColumn.setOnEditCommit(event -> {
            Part part = event.getRowValue();
            String newValue = event.getNewValue();
            try {
                BigDecimal parsed = new BigDecimal(newValue);
                part.setPrice(parsed);
            } catch (NumberFormatException e) {
                logger.warn("Invalid price entered: {}", newValue);
            }
            partsTable.refresh();
        });

        TableColumn<Part, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setResizable(false);
        quantityColumn.setCellFactory(col -> new TableCell<>() {
            private final Spinner<Integer> spinner = new Spinner<>(1, 100, 1);

            {
                spinner.setEditable(false);
                spinner.setPrefWidth(80);
                spinner.setScaleY(0.9);
                spinner.setStyle("-fx-font-size: 12px; -fx-padding: 0px;");
                spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                    Part part = getTableView().getItems().get(getIndex());
                    if (part != null) {
                        selectedPartsWithQuantities.put(part, newVal);
                    }
                });
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Part part = getTableView().getItems().get(getIndex());
                    int currentQty = selectedPartsWithQuantities.getOrDefault(part, 1);
                    spinner.getValueFactory().setValue(currentQty);
                    setGraphic(spinner);
                }
            }
        });

        // Unnamed column with parts removing button
        TableColumn<Part, Void> removeColumn = new TableColumn<>("");
        removeColumn.setResizable(false);
        removeColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("✖");

            {
                removeButton.setStyle("-fx-font-size: 12px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeButton.setCursor(Cursor.HAND);
                removeButton.setScaleY(0.9);
                removeButton.setOnAction(e -> {
                    Part part = getTableView().getItems().get(getIndex());
                    selectedPartsWithQuantities.remove(part);
                    partsTable.getItems().remove(part);
                    partsTable.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        partsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, removeColumn);

        partsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        partsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double totalWidth = newVal.doubleValue();
            nameColumn.setPrefWidth(totalWidth * 0.67);
            priceColumn.setPrefWidth(totalWidth * 0.12);
            quantityColumn.setPrefWidth(totalWidth * 0.12);
            removeColumn.setPrefWidth(totalWidth * 0.08);
        });
    }

    private void initEmployeesTableView() {
        employeesTableLabel = new Label("Employee(s) Assigned:");

        // Fetch all employees from the database
        List<Employee> allEmployees = employeeService.getAllEmployees();
        ObservableList<Employee> allEmployeesList = FXCollections.observableArrayList(allEmployees);
        FilteredList<Employee> filteredList = new FilteredList<>(allEmployeesList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addEmployeeBox = new ComboBox<>();
        addEmployeeBox.setItems(filteredList);
        addEmployeeBox.setEditable(true);
        addEmployeeBox.setPromptText("Add Employee...");

        // Конвертер: отображение имени в списке и обратный поиск
        addEmployeeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee employee) {
                if (employee != null) {
                    return employee.getFirstName() + " " + employee.getLastName();
                } else {
                    return "";
                }
            }

            @Override
            public Employee fromString(String name) {
                return allEmployees.stream()
                        .filter(employee -> {
                            String employeeName = employee.getFirstName() + " " + employee.getLastName();
                            return employeeName.equalsIgnoreCase(name.trim());
                        })
                        .findFirst()
                        .orElse(null);
            }
        });

        // 🔍 Обновление фильтра при вводе текста
        addEmployeeBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Избегаем циклического обновления
            // Platform.runLater "разрывает цикл" между обновлением predicate и реакцией ComboBox
            Platform.runLater(() -> {
                String lower = newText.toLowerCase().trim();
                filteredList.setPredicate(employee -> {
                    String employeeName = employee.getFirstName() + " " + employee.getLastName();
                    return lower.isEmpty() || employeeName.toLowerCase().contains(lower);
                });
            });
        });

        addEmployeeBox.setOnAction(event -> {
            String input = addEmployeeBox.getEditor().getText().trim();
            Employee selected = allEmployees.stream()
                    .filter(employee -> {
                        String employeeName = employee.getFirstName() + " " + employee.getLastName();
                        return employeeName.equalsIgnoreCase(input);
                    })
                    .findFirst()
                    .orElse(null);

            if (selected != null && !selectedEmployees.contains(selected)) {
                selectedEmployees.add(selected);
                employeesTable.getItems().add(selected);
                employeesTable.refresh();
            }

            /*
             * ComboBox внутри использует ListView.
             * При вызове setValue(null) сразу в valueProperty().addListener(...) мы вмешиваемся в момент,
             * когда JavaFX ещё не завершил обработку предыдущего выбора.
             * Отложенный вызов через Platform.runLater позволяет дождаться завершения всех внутренних действий
             * и только потом безопасно сбрасывает выбор.
             * */
            Platform.runLater(() -> {
                addEmployeeBox.setValue(null);
                addEmployeeBox.getEditor().setText(""); // на всякий случай
                addEmployeeBox.getEditor().clear();       // очистка текста
                addEmployeeBox.getParent().requestFocus(); // убираем фокус с ComboBox
            });
        });


        employeesTable = new TableView<>();
        employeesTable.setItems(FXCollections.observableArrayList(selectedEmployees));
        employeesTable.setEditable(true);
        employeesTable.setStyle("-fx-table-cell-border-color: transparent; -fx-border-color: transparent;");

        TableColumn<Employee, String> nameColumn = new TableColumn<>("Employee Name");
        nameColumn.setResizable(false);
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName())
        );

        // Колонка "Удалить"
        TableColumn<Employee, Void> removeColumn = new TableColumn<>("");
        removeColumn.setResizable(false);
        removeColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("✖");

            {
                removeButton.setStyle("-fx-font-size: 12px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
                removeButton.setCursor(Cursor.HAND);
                removeButton.setScaleY(0.9);
                removeButton.setOnAction(e -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    selectedEmployees.remove(employee);
                    employeesTable.getItems().remove(employee);
                    employeesTable.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        employeesTable.getColumns().addAll(nameColumn, removeColumn);

        employeesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        employeesTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double totalWidth = newVal.doubleValue();
            nameColumn.setPrefWidth(totalWidth * 0.87);
            removeColumn.setPrefWidth(totalWidth * 0.12);
        });
    }

    private void initAttachPhotosButton() {
        attachPhotosButton = new Button("Attach Photos");
        attachPhotosButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        attachPhotosButton.setDisable(true);
        attachPhotosButton.setOnAction(event -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                ViewUtils.showAlert("No Order Selected", "Please select an order before attaching photos.");
                return;
            }

            PhotoUtils.attachPhotosToOrder(selectedOrder.getId());
        });
    }

    private void initShowPhotosButton() {
        showPhotosButton = new Button("Order Photos");
        showPhotosButton.setCursor(Cursor.HAND);
        showPhotosButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        showPhotosButton.setOnAction(event -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
            if (selectedOrder == null) {
                ViewUtils.showAlert("No Order Selected", "Please select an order before viewing photos.");
                return;
            }

            PhotoUtils.showOrderPhotosDialog(selectedOrder);
        });
    }

    private void initPrintInvoiceButton() {
        printInvoiceButton = new Button("Print Invoice");
        printInvoiceButton.setCursor(Cursor.HAND);
        printInvoiceButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
//        printInvoiceButton.setOnAction(event -> {
//            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
//            if (selectedOrder == null) {
//                TabUtils.showAlert("No Order Selected", "Please select an order before printing.");
//                return;
//            }
//
//            String invoiceText = PrinterUtils.generateInvoiceText(selectedOrder);
//            PrinterUtils.printInvoice(invoiceText);
//        });
    }

    private void initUpdateOrderButton() {
        updateOrderButton = new Button("Update Order");
        updateOrderButton.setCursor(Cursor.HAND);
        updateOrderButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        updateOrderButton.setOnAction(event -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                ViewUtils.showAlert("No Order Selected", "Please select an Order before updating.");
                return;
            }

            Order.Type orderType = orderTypeBox.getValue();
            selectedOrder.setType(orderType);

            Order.Status orderStatus = orderStatusBox.getValue();
            selectedOrder.setStatus(orderStatus);

            LocalDate selectedDate = orderDatePicker.getValue();
            selectedOrder.setDate(selectedDate != null ? selectedDate : LocalDate.now());

            int odometer = (odometerField.getText() == null || odometerField.getText().isEmpty()) ? 0 : Integer.parseInt(odometerField.getText());
            selectedOrder.setOdometer(odometer);

            String notes = (notesArea.getText() == null || notesArea.getText().isEmpty()) ? null : notesArea.getText();
            selectedOrder.setNotes(notes);

            Map<Part, Integer> updatedPartsWithQuantities = new HashMap<>(selectedPartsWithQuantities);
            selectedOrder.setPartsWithQuantities(updatedPartsWithQuantities);

            selectedOrder.setServicesProvided(new ArrayList<>(selectedServices));
            selectedOrder.setEmployeesAssigned(new ArrayList<>(selectedEmployees));

            orderService.updateOrder(selectedOrder);

            // Refresh orders table
            refreshOrdersTable();

            ViewUtils.showAlert("Order Updated", "Order details have been successfully updated.");
        });
    }

    private void initDeleteOrderButton() {
        deleteOrderButton = new Button("Delete Order");
        deleteOrderButton.setCursor(Cursor.HAND);
        deleteOrderButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteOrderButton.setOnAction(event -> {
            Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

            if (selectedOrder == null) {
                ViewUtils.showAlert("No Order Selected", "Please select an Order before deleting.");
                return;
            }

            // Create a confirmation alert
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to delete this Order?");
            confirmationAlert.setContentText("This action cannot be undone.");

            // Wait for user response
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If the user confirms, delete the Order
                orderService.deleteOrder(selectedOrder);

                refreshOrdersTable();

                ViewUtils.showAlert("Order Deleted", "Order has been successfully deleted.");
            }
        });
    }

    private void initLayout() {
        HBox topButtonBox = new HBox(10, newOrderButton);
        setVgrow(ordersTable, Priority.ALWAYS);
        setHgrow(ordersTable, Priority.ALWAYS);
        VBox container = new VBox(10, topButtonBox, ordersTable);
        GridPane.setVgrow(container, Priority.ALWAYS);     // чтобы VBox росла внутри GridPane
        GridPane.setHgrow(container, Priority.ALWAYS);
        add(container, 0, 0);
        setPrefWidth(Double.MAX_VALUE); // Ensure mainPane itself stretches across the full window width
    }

    private void refreshOrdersTable() {
        // Load orders data from the database
        List<Order> orders = orderService.getAllOrders();
        ObservableList<Order> orderList = FXCollections.observableArrayList(orders);
        ordersTable.setItems(orderList);
        ordersTable.refresh();
    }

    private Paint getStatusColor(Order.Status status) {
        return switch (status) {
            case In_Progress -> Color.LIMEGREEN;
            case Pending     -> Color.GOLD;
            case Canceled    -> Color.CRIMSON;
            case Completed   -> Color.DODGERBLUE;
            default          -> Color.GRAY;
        };
    }

}
