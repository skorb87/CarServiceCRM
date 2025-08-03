package me.skorb.repository;

import me.skorb.entity.Customer;
import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.entity.Vehicle;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    // Private constructor to prevent instantiation
    private CustomerRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final CustomerRepository INSTANCE = new CustomerRepository();
    }

    // Public method to get the Singleton instance
    public static CustomerRepository getInstance() {
        return Holder.INSTANCE;
    }

    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customer.setVehicles(getVehiclesByCustomerId(customer.getId()));
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customer.setVehicles(getVehiclesByCustomerId(customer.getId()));
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    public Customer save(Customer customer) {
        String sql;
        boolean isUpdate = customer.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, city = ?, state = ?, postal_code = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO customers (first_name, last_name, email, phone, address, city, state, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            stmt.setString(6, customer.getCity());

            String state = (customer.getState() == null) ? null : customer.getState().name();
            stmt.setString(7, state);

            stmt.setString(8, customer.getPostalCode());

            if (isUpdate) {
                stmt.setInt(9, customer.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (!isUpdate && affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    customer.setId(generatedKeys.getInt(1));
                }
            }

            return customer;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM customers WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM customers";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Customer> findByName(String firstName, String lastName) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE first_name = ? AND last_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Find by last name
    public List<Customer> findByLastName(String lastName) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE last_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, lastName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Find by email
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Customer> findByPhone(String phone) {
        String sql = "SELECT * FROM Customers WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customer.setVehicles(getVehiclesByCustomerId(customer.getId()));
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Customer> findByVehicleVin(String vin) {
        String sql = "SELECT c.* FROM Customers c " +
                "JOIN vehicles v ON c.id = v.customer_id " +
                "WHERE v.vin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = mapResultSetToCustomer(rs);
                customer.setVehicles(getVehiclesByCustomerId(customer.getId()));
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Customer> findByCity(String city) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE city = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }

    // Check if phone number exists
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM customers WHERE phone = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update first name
    public int updateFirstName(int id, String firstName) {
        return updateCustomerField("first_name", firstName, id);
    }

    // Update last name
    public int updateLastName(int id, String lastName) {
        return updateCustomerField("last_name", lastName, id);
    }

    // Update address
    public int updateAddress(int id, String address) {
        return updateCustomerField("address", address, id);
    }

    // Update city
    public int updateCity(int id, String city) {
        return updateCustomerField("city", city, id);
    }

    // Update state
    public int updateState(int id, String state) {
        return updateCustomerField("state", state, id);
    }

    // Update postal code
    public int updatePostalCode(int id, String postalCode) {
        return updateCustomerField("postal_code", postalCode, id);
    }

    // Update phone number
    public int updatePhone(int id, String phone) {
        return updateCustomerField("phone", phone, id);
    }

    // Update email
    public int updateEmail(int id, String email) {
        return updateCustomerField("email", email, id);
    }

    // Generic method to update customer fields
    private int updateCustomerField(String field, String value, int id) {
        String sql = "UPDATE customers SET " + field + " = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Helper method to map ResultSet to Customer entity
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));

        Customer.State state = rs.getString("state") == null ? null : Customer.State.valueOf(rs.getString("state"));
        customer.setState(state);

        customer.setPostalCode(rs.getString("postal_code"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));

        return customer;
    }

    public List<Vehicle> getVehiclesByCustomerId(int customerId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.id, v.vin, v.year, v.license_plate, " +
                "m.id AS make_id, m.name AS make_name, " +
                "mo.id AS model_id, mo.name AS model_name " +
                "FROM Vehicles v " +
                "LEFT JOIN Makes m ON v.make_id = m.id " +
                "LEFT JOIN Models mo ON v.model_id = mo.id " +
                "WHERE v.customer_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setId(rs.getInt("id"));
                vehicle.setVin(rs.getString("vin"));

                Integer year = (rs.getInt("year")) == 0 ? null : rs.getInt("year");
                vehicle.setYear(year);

                vehicle.setLicensePlate(rs.getString("license_plate"));

                // Setting Make
                int makeId = rs.getInt("make_id");
                if (!rs.wasNull()) {
                    vehicle.setMake(new Make(makeId, rs.getString("make_name")));
                }

                // Setting Model
                int modelId = rs.getInt("model_id");
                if (!rs.wasNull()) {
                    vehicle.setModel(new Model(modelId, rs.getString("model_name")));
                }

                vehicle.setCustomerId(customerId);

                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }


}
