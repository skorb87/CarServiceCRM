package me.skorb.repository;

import me.skorb.entity.*;
import me.skorb.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    // Private constructor to prevent instantiation
    private OrderRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final OrderRepository INSTANCE = new OrderRepository();
    }

    // Public method to get the Singleton instance
    public static OrderRepository getInstance() {
        return Holder.INSTANCE;
    }

    public Order findById(int id) {
        String sql = "SELECT o.*, c.id as customer_id, c.first_name, c.last_name, c.address, c.city, c.state, c.postal_code, c.phone, c.email, " +
                "v.id as vehicle_id, v.vin, v.license_plate, v.year, m.id as make_id, m.name as make_name, mo.id as model_id, mo.name as model_name " +
                "FROM Orders o " +
                "JOIN Customers c ON o.customer_id = c.id " +
                "JOIN Vehicles v ON o.vehicle_id = v.id " +
                "LEFT JOIN Makes m ON v.make_id = m.id " +
                "LEFT JOIN Models mo ON v.model_id = mo.id " +
                "WHERE o.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                int orderId = order.getId();
                order.setPartsWithQuantities(getPartsForOrder(orderId));
                order.setServicesProvided(getServicesForOrder(orderId));
                order.setEmployeesAssigned(getEmployeesForOrder(orderId));

                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.id as customer_id, c.first_name, c.last_name, c.address, c.city, c.state, c.postal_code, c.phone, c.email, " +
                "v.id as vehicle_id, v.vin, v.license_plate, v.year, m.id as make_id, m.name as make_name, mo.id as model_id, mo.name as model_name " +
                "FROM Orders o " +
                "JOIN Customers c ON o.customer_id = c.id " +
                "JOIN Vehicles v ON o.vehicle_id = v.id " +
                "LEFT JOIN Makes m ON v.make_id = m.id " +
                "LEFT JOIN Models mo ON v.model_id = mo.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                int orderId = order.getId();
                order.setPartsWithQuantities(getPartsForOrder(orderId));
                order.setServicesProvided(getServicesForOrder(orderId));
                order.setEmployeesAssigned(getEmployeesForOrder(orderId));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> findByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.id as customer_id, c.first_name, c.last_name, c.address, c.city, c.state, c.postal_code, c.phone, c.email, " +
                "v.id as vehicle_id, v.vin, v.license_plate, v.year, m.id as make_id, m.name as make_name, mo.id as model_id, mo.name as model_name " +
                "FROM Orders o " +
                "JOIN Customers c ON o.customer_id = c.id " +
                "JOIN Vehicles v ON o.vehicle_id = v.id " +
                "LEFT JOIN Makes m ON v.make_id = m.id " +
                "LEFT JOIN Models mo ON v.model_id = mo.id " +
                "WHERE o.customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                int orderId = order.getId();
                order.setPartsWithQuantities(getPartsForOrder(orderId));
                order.setServicesProvided(getServicesForOrder(orderId));
                order.setEmployeesAssigned(getEmployeesForOrder(orderId));

                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int save(Order order) {
        String sql = "INSERT INTO Orders (customer_id, vehicle_id, odometer, type, order_date, status, labor_hours, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getCustomer().getId());
            stmt.setInt(2, order.getVehicle().getId());
            stmt.setInt(3, order.getOdometer());
            stmt.setString(4, order.getType().name());
            stmt.setDate(5, java.sql.Date.valueOf(order.getDate()));
            stmt.setString(6, order.getStatus().name());
            stmt.setBigDecimal(7, order.getLaborHours());
            stmt.setString(8, order.getNotes());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    savePartsForOrder(orderId, order.getPartsWithQuantities(), conn);
                    saveServicesForOrder(orderId, order.getServicesProvided(), conn);
                    saveEmployeesForOrder(orderId, order.getEmployeesAssigned(), conn);

                    return orderId;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Return -1 if order creation fails
        }
    }

    public void update(Order order) {
        String sql = "UPDATE Orders SET odometer = ?, order_date = ?, type = ?, status = ?, labor_hours = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getOdometer());
            stmt.setDate(2, java.sql.Date.valueOf(order.getDate()));
            stmt.setString(3, order.getType().name());
            stmt.setString(4, order.getStatus().name());
            stmt.setBigDecimal(5, order.getLaborHours());
            stmt.setString(6, order.getNotes());
            stmt.setInt(7, order.getId());

            stmt.executeUpdate();

            // Remove old entries and insert new ones
            deletePartsForOrder(order.getId(), conn);
            savePartsForOrder(order.getId(), order.getPartsWithQuantities(), conn);

            deleteServicesForOrder(order.getId(), conn);
            saveServicesForOrder(order.getId(), order.getServicesProvided(), conn);

            deleteEmployeesForOrder(order.getId(), conn);
            saveEmployeesForOrder(order.getId(), order.getEmployeesAssigned(), conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Orders WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePhotoPath(int orderId, String photoPath) {
        String sql = "INSERT INTO Order_Photos (order_id, photo_path) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, photoPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPhotoPaths(int orderId) {
        List<String> photoPaths = new ArrayList<>();
        String sql = "SELECT photo_path FROM Order_Photos WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                photoPaths.add(rs.getString("photo_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return photoPaths;
    }

    public int getOrdersCountByStatus(String orderStatus) {
        String sql = String.format("SELECT COUNT(*) FROM Orders WHERE status = '%s'", orderStatus);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void savePartsForOrder(int orderId, Map<Part, Integer> partsWithQuantities, Connection conn) throws SQLException {
        if (partsWithQuantities == null || partsWithQuantities.isEmpty()) return;

        String sql = "INSERT INTO Order_Parts (order_id, part_id, part_price, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<Part, Integer> entry : partsWithQuantities.entrySet()) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, entry.getKey().getId());
                stmt.setBigDecimal(3, entry.getKey().getPrice());
                stmt.setInt(4, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void saveServicesForOrder(int orderId, List<Service> services, Connection conn) throws SQLException {
        if (services == null || services.isEmpty()) return;

        String sql = "INSERT INTO Order_Services (order_id, service_id, service_price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Service service : services) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, service.getId());
                stmt.setBigDecimal(3, service.getPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void saveEmployeesForOrder(int orderId, List<Employee> employees, Connection conn) throws SQLException {
        if (employees == null || employees.isEmpty()) return;

        String sql = "INSERT INTO Order_Employees (order_id, employee_id, labor_price) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Employee employee : employees) {
                stmt.setInt(1, orderId);
                stmt.setInt(2, employee.getId());
                stmt.setBigDecimal(3, employee.getLaborPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deletePartsForOrder(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Order_Parts WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    private void deleteServicesForOrder(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Order_Services WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    public void deletePhotoPath(String photoPath) {
        String sql = "DELETE FROM Order_Photos WHERE photo_path = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoPath);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<Part, Integer> getPartsForOrder(int orderId) {
        Map<Part, Integer> partsWithQuantities = new HashMap<>();
        String sql = "SELECT p.id, p.name, p.description, op.part_price, op.quantity " +
                "FROM Order_Parts op " +
                "JOIN Parts p ON op.part_id = p.id " +
                "WHERE op.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Part part = new Part();
                part.setId(rs.getInt("id"));
                part.setName(rs.getString("name"));
                part.setDescription(rs.getString("description"));
                part.setPrice(rs.getBigDecimal("part_price"));
                int quantity = rs.getInt("quantity");

                partsWithQuantities.put(part, quantity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partsWithQuantities;
    }


    private List<Service> getServicesForOrder(int orderId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.id, s.name, s.description, os.service_price " +
                "FROM Order_Services os " +
                "JOIN Services s ON os.service_id = s.id " +
                "WHERE os.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Service service = new Service();
                service.setId(rs.getInt("id"));
                service.setName(rs.getString("name"));
                service.setDescription(rs.getString("description"));
                service.setPrice(rs.getBigDecimal("service_price"));
                services.add(service);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    private void deleteEmployeesForOrder(int orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Order_Employees WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    public List<Employee> getEmployeesForOrder(int orderId) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.id, e.first_name, e.last_name, e.role, e.phone, e.email, oe.labor_price " +
                "FROM Order_Employees oe " +
                "JOIN Employees e ON oe.employee_id = e.id " +
                "WHERE oe.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setId(rs.getInt("id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setRole(Employee.Role.valueOf(rs.getString("role")));
                employee.setPhone(rs.getString("phone"));
                employee.setEmail(rs.getString("email"));
                employee.setLaborPrice(rs.getBigDecimal("labor_price"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));

        String state = rs.getString("state");
        if (state != null) {
            customer.setState(Customer.State.valueOf(state));
        }

        customer.setPostalCode(rs.getString("postal_code"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));

        Make make = null;
        int makeId = rs.getInt("make_id");
        if (makeId != 0) {
            make = new Make();
            make.setId(makeId);
            make.setName(rs.getString("make_name"));
        }

        Model model = null;
        int modelId = rs.getInt("model_id");
        if (modelId != 0) {
            model = new Model();
            model.setId(modelId);
            model.setName(rs.getString("model_name"));
            model.setMakeId(make.getId());
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("vehicle_id"));
        vehicle.setVin(rs.getString("vin"));
        vehicle.setLicensePlate(rs.getString("license_plate"));
        vehicle.setYear(rs.getInt("year"));
        vehicle.setMake(make);
        vehicle.setModel(model);

        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomer(customer);
        order.setVehicle(vehicle);
        order.setOdometer(rs.getInt("odometer"));
        order.setDate(rs.getDate("order_date").toLocalDate());
        order.setType(Order.Type.valueOf(rs.getString("type")));
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setLaborHours(rs.getBigDecimal("labor_hours"));
        order.setNotes(rs.getString("notes"));

        return order;
    }
}
