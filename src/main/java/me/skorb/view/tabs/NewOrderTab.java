package me.skorb.view.tabs;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import me.skorb.entity.*;
import me.skorb.event.EventBus;
import me.skorb.event.OpenOrdersTabEvent;
import me.skorb.service.*;
import me.skorb.view.ViewUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static me.skorb.view.ViewUtils.*;

public class NewOrderTab {

    private static final Logger logger = LogManager.getLogger(NewOrderTab.class);

    private final CustomerService customerService = new CustomerService();
    private final VehicleService vehicleService = new VehicleService();
    private final OrderService orderService = new OrderService();
    private final EmployeeService employeeService = new EmployeeService();
    private final ServiceService serviceService = new ServiceService();
    private final PartService partService = new PartService();

    private Tab newOrderTab;

    private Label customerTableLabel;
    private TableView<Customer> customerTable;
    private Label vehicleTableLabel;
    private TableView<Vehicle> vehicleTable;

    private Button newCustomerButton;
    private Button newVehicleButton;

    private Label servicesTableLabel;
    private ComboBox<Service> addServiceBox;
    private TableView<Service> servicesTable;

    private Label partsTableLabel;
    private ComboBox<Part> addPartBox;
    private TableView<Part> partsTable;

    private Label employeesTableLabel;
    private ComboBox<Employee> addEmployeeBox;
    private TableView<Employee> employeesTable;

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

    private Button attachPhotosButton;
    private Button createOrderButton;

    // Set for tracking selected Services
    private final Set<Service> selectedServices = new HashSet<>();
    // Track selected parts with their quantities
    private final Map<Part, Integer> selectedPartsWithQuantities = new HashMap<>();
    // Set for tracking selected Employees
    private final Set<Employee> selectedEmployees = new HashSet<>();
    private final List<File> selectedPhotos = new ArrayList<>();

    public Tab createNewOrderTab() {
        newOrderTab = new Tab("New Order");

        initNewOrderTabContentView();

        return newOrderTab;
    }

    private void initNewOrderTabContentView() {
        initCustomerTable();
        initVehicleTable();
        initOrderDetailsView();
        initLayout();
    }

    private void initCustomerTable() {
        customerTableLabel = new Label("Choose a Customer from list:");

        newCustomerButton = new Button("+ New Customer");
        newCustomerButton.setCursor(Cursor.HAND);
        newCustomerButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold;");
        newCustomerButton.setOnAction(e -> showNewCustomerDialog(customerTable));

        customerTable = new TableView<>();
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        customerTable.setPadding(new Insets(10, 10, 10, 10));
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

        newVehicleButton = new Button("+ New Vehicle");
        newVehicleButton.setCursor(Cursor.HAND);
        newVehicleButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold;");
        newVehicleButton.setOnAction(e -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            if (selectedCustomer == null) {
                ViewUtils.showToastPopup("Please select a customer before adding new vehicle", ToastType.WARNING, customerTable);
                return;
            }
            showAddVehicleDialogFor(selectedCustomer, vehicleTable);
        });

        vehicleTable = new TableView<>();
        VBox.setVgrow(vehicleTable, Priority.ALWAYS);
        vehicleTable.setPadding(new Insets(10, 10, 10, 10));
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Vehicle, String> vinColumn = new TableColumn<>("VIN");
        vinColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));

        vehicleTable.getColumns().addAll(makeColumn, modelColumn, yearColumn, vinColumn);

        // Ensure all vehicle table's columns have equal width
        vehicleTable.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue();
            double columnWidth = totalWidth / vehicleTable.getColumns().size();
            vehicleTable.getColumns().forEach(column -> column.setPrefWidth(columnWidth));
        });

        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedVehicle) -> {
            if (selectedVehicle == null) {
                notesArea.setText("");

                selectedServices.clear();
                servicesTable.refresh();

                selectedPartsWithQuantities.clear();
                partsTable.refresh();

                selectedEmployees.clear();
                employeesTable.refresh();

                notesArea.setDisable(true);
                addServiceBox.setDisable(true);
                servicesTable.setDisable(true);
                addPartBox.setDisable(true);
                partsTable.setDisable(true);
                addEmployeeBox.setDisable(true);
                employeesTable.setDisable(true);
                orderTypeBox.setDisable(true);
                orderStatusBox.setDisable(true);
                orderDatePicker.setDisable(true);
                odometerField.setDisable(true);
                attachPhotosButton.setDisable(true);
                createOrderButton.setDisable(true);
            } else {
                notesArea.setDisable(false);
                addServiceBox.setDisable(false);
                servicesTable.setDisable(false);
                addPartBox.setDisable(false);
                partsTable.setDisable(false);
                addEmployeeBox.setDisable(false);
                employeesTable.setDisable(false);
                orderTypeBox.setDisable(false);
                orderStatusBox.setDisable(false);
                orderDatePicker.setDisable(false);
                odometerField.setDisable(false);
                attachPhotosButton.setDisable(false);
                createOrderButton.setDisable(false);
            }
        });
    }

    private void initOrderDetailsView() {
        orderTypeLabel = new Label("Order Type*:");
        orderTypeLabel.setMinWidth(100);
        orderTypeBox = new ComboBox<>();
        orderTypeBox.setMinWidth(220);
        orderTypeBox.setItems(FXCollections.observableArrayList(Order.Type.values()));
        orderTypeBox.setPromptText("Choose Order Type...");
        orderTypeBox.setDisable(true);

        orderStatusLabel = new Label("Order Status*:");
        orderStatusLabel.setMinWidth(100);
        orderStatusBox = new ComboBox<>();
        orderStatusBox.setMinWidth(220);
        orderStatusBox.setItems(FXCollections.observableArrayList(Order.Status.values()));
        orderStatusBox.setPromptText("Choose Order Status...");
        orderStatusBox.setDisable(true);

        orderDateLabel = new Label("Order Date*:");
        orderDateLabel.setMinWidth(100);
        orderDatePicker = new DatePicker(LocalDate.now());
        orderDatePicker.setMinWidth(220);
        orderDatePicker.setPromptText("Order date");
        orderDatePicker.setEditable(false);
        orderDatePicker.setDisable(true);

        odometerLabel = new Label("Odometer:");
        odometerLabel.setMinWidth(100);
        odometerField = new TextField();
        odometerField.setMinWidth(220);
        odometerField.setDisable(true);

        notesLabel = new Label("Order Notes:");
        notesArea = new TextArea();
        notesArea.setDisable(true);

        initServicesProvidedView();
        initPartsUsedView();
        initEmployeesListView();
        initAttachPhotosButton();
        initCreateOrderButton();
    }

    private void initServicesProvidedView() {
        servicesTableLabel = new Label("Service(s) provided:");

        List<Service> allServices = serviceService.getAllServices();
        ObservableList<Service> allServiceList = FXCollections.observableArrayList(allServices);
        FilteredList<Service> filteredList = new FilteredList<>(allServiceList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addServiceBox = new ComboBox<>();
        addServiceBox.setItems(filteredList);
        addServiceBox.setDisable(true);
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

        // Обновление фильтра при вводе текста
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
        servicesTable.setDisable(true);
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

    private void initPartsUsedView() {
        partsTableLabel = new Label("Part(s) used:");

        // Fetch all Parts from the database
        List<Part> allParts = partService.getAllParts();
        ObservableList<Part> allPartsList = FXCollections.observableArrayList(allParts);
        FilteredList<Part> filteredList = new FilteredList<>(allPartsList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addPartBox = new ComboBox<>();
        addPartBox.setItems(filteredList);
        addPartBox.setDisable(true);
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
        partsTable.setDisable(true);
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

    private void initEmployeesListView() {
        employeesTableLabel = new Label("Employee(s) Assigned:");

        // Fetch all employees from the database
        List<Employee> allEmployees = employeeService.getAllEmployees();
        ObservableList<Employee> allEmployeesList = FXCollections.observableArrayList(allEmployees);
        FilteredList<Employee> filteredList = new FilteredList<>(allEmployeesList, p -> true); // Обёртка для фильтрации результатов поиска в списке сервисов

        addEmployeeBox = new ComboBox<>();
        addEmployeeBox.setItems(filteredList);
        addEmployeeBox.setDisable(true);
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
        employeesTable.setDisable(true);
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
        attachPhotosButton.setCursor(Cursor.HAND);
        attachPhotosButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
        attachPhotosButton.setDisable(true);
        attachPhotosButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
            if (selectedFiles == null || selectedFiles.isEmpty()) return;

//            selectedPhotos = new ArrayList<>(selectedFiles);
            selectedPhotos.addAll(selectedFiles);

            ViewUtils.showAlert("Photos Attached", "Photos successfully attached to order.");
        });
    }

    private void initCreateOrderButton() {
        createOrderButton = new Button("Create Order");
        createOrderButton.setCursor(Cursor.HAND);
        createOrderButton.setStyle("-fx-background-color: #2D5A90; -fx-text-fill: white; -fx-font-weight: bold;");
        createOrderButton.setDisable(true);
        createOrderButton.setOnAction(event -> {
            Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
            Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();

            // TODO: Add fields validation
            if (orderTypeBox.getValue() == null) {

            }


            if (selectedCustomer != null && selectedVehicle != null) {
                Order order = new Order();
                order.setCustomer(selectedCustomer);
                order.setVehicle(selectedVehicle);

                order.setServicesProvided(List.copyOf(selectedServices));

                Map<Part, Integer> updatedPartsWithQuantities = new HashMap<>(selectedPartsWithQuantities);
                order.setPartsWithQuantities(updatedPartsWithQuantities);

                order.setEmployeesAssigned(List.copyOf(selectedEmployees));

                Order.Type orderType = orderTypeBox.getValue();
                order.setType(orderType);

                Order.Status orderStatus = orderStatusBox.getValue();
                order.setStatus(orderStatus);

                LocalDate selectedDate = orderDatePicker.getValue();
                order.setDate(selectedDate != null ? selectedDate : LocalDate.now());

                String odometerText = odometerField.getText();
                int odometerInt = (odometerText != null && !odometerText.trim().isEmpty()) ? Integer.parseInt(odometerText) : 0;
                order.setOdometer(odometerInt);

                String notes = (notesArea.getText() == null || notesArea.getText().trim().isEmpty()) ? null : notesArea.getText();
                order.setNotes(notes);

                int orderId = orderService.createOrder(order);
                PhotoUtils.attachPhotosToOrder(orderId, selectedPhotos);

                // Close this tab dynamically
                Platform.runLater(() -> {
                    TabPane tabPane = TabManager.getInstance().getTabPane();
                    tabPane.getTabs().remove(newOrderTab); // close current tab
                });

                // Open Orders List tab
                EventBus.fireEvent(new OpenOrdersTabEvent());
            }

        });
    }

    private void initLayout() {
        // --- Top Content First Column: Customer Information ---
        Region customerHeaderSpacer = new Region();
        HBox.setHgrow(customerHeaderSpacer, Priority.ALWAYS);
        HBox customerHeader = new HBox(customerTableLabel, customerHeaderSpacer, newCustomerButton);
        customerHeader.setAlignment(Pos.CENTER_LEFT);
        VBox customerInfoLayout = new VBox(10);
        customerInfoLayout.getChildren().addAll(customerHeader, customerTable);
        HBox.setHgrow(customerInfoLayout, Priority.ALWAYS);

        // --- Top Content Second Column: Vehicle Information ---
        Region vehicleHeaderSpacer = new Region();
        HBox.setHgrow(vehicleHeaderSpacer, Priority.ALWAYS);
        HBox vehicleHeader = new HBox(vehicleTableLabel, vehicleHeaderSpacer, newVehicleButton);
        vehicleHeader.setAlignment(Pos.CENTER_LEFT);
        VBox vehicleInfoLayout = new VBox(10);
        vehicleInfoLayout.getChildren().addAll(vehicleHeader, vehicleTable);
        HBox.setHgrow(vehicleInfoLayout, Priority.ALWAYS);

        // --- Top Layout: Arrange top columns in an HBox ---
        HBox topLayout = new HBox(20, customerInfoLayout, vehicleInfoLayout);
        topLayout.setPadding(new Insets(10, 15, 10, 15));
        topLayout.setAlignment(Pos.TOP_CENTER);
        customerInfoLayout.prefWidthProperty().bind(topLayout.widthProperty().divide(2));
        vehicleInfoLayout.prefWidthProperty().bind(topLayout.widthProperty().divide(2));


        HBox orderTypeLayout = new HBox(10, orderTypeLabel, orderTypeBox);
        HBox orderStatusLayout = new HBox(10, orderStatusLabel, orderStatusBox);
        HBox orderDateLayout = new HBox(10, orderDateLabel, orderDatePicker);
        HBox odometerLayout = new HBox(10, odometerLabel, odometerField);
        VBox orderInfoLayout = new VBox(10, orderTypeLayout, orderStatusLayout, orderDateLayout, odometerLayout);
        HBox.setHgrow(orderInfoLayout, Priority.ALWAYS);
        VBox orderNotesLayout = new VBox(10, notesLabel, notesArea);

        HBox middleLayout = new HBox(20, orderInfoLayout, orderNotesLayout);
        middleLayout.setPadding(new Insets(10, 15, 10, 15));
        middleLayout.setAlignment(Pos.TOP_CENTER);
        orderInfoLayout.prefWidthProperty().bind(middleLayout.widthProperty().divide(2));
        orderNotesLayout.prefWidthProperty().bind(middleLayout.widthProperty().divide(2));


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

        HBox buttonBox = new HBox(attachPhotosButton, createOrderButton);
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
        mainPane.add(middleLayout, 0, 1);
        mainPane.add(bottomLayout, 0, 2);

        // Make rows to take corresponding heights
//        RowConstraints orderTablesRowConstraint = new RowConstraints();
//        orderTablesRowConstraint.setPercentHeight(30); // customerTable 35% of available height
//
//        RowConstraints detailsPaneRowConstraint = new RowConstraints();
//        detailsPaneRowConstraint.setPercentHeight(70); // detailsPane takes 65% of available height
//        detailsPaneRowConstraint.setVgrow(Priority.ALWAYS); // Ensure it expands when window resizes
//
//        mainPane.getRowConstraints().addAll(orderTablesRowConstraint, detailsPaneRowConstraint);

        GridPane.setVgrow(topLayout, Priority.ALWAYS);
        GridPane.setVgrow(middleLayout, Priority.ALWAYS);
        GridPane.setVgrow(bottomLayout, Priority.ALWAYS);
        GridPane.setHgrow(topLayout, Priority.ALWAYS);
        GridPane.setHgrow(middleLayout, Priority.ALWAYS);
        GridPane.setHgrow(bottomLayout, Priority.ALWAYS);

        // Ensure mainPane itself stretches across the full tab width
        mainPane.setPrefWidth(Double.MAX_VALUE);

        newOrderTab.setContent(mainPane);
    }

}
