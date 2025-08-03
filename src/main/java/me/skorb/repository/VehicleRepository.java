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

public class VehicleRepository {

    private VehicleRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final VehicleRepository INSTANCE = new VehicleRepository();
    }

    // Public method to get the Singleton instance
    public static VehicleRepository getInstance() {
        return VehicleRepository.Holder.INSTANCE;
    }

    // Find vehicles for customer
    public List<Vehicle> findByCustomer(Customer customer) {
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
            stmt.setInt(1, customer.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicleWithCustomer(rs, customer));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    // Find vehicle by ID
    public Optional<Vehicle> findById(int id) {
        String sql = "SELECT * FROM Vehicles v JOIN Customers c ON v.customer_id = c.id WHERE v.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Find all vehicles
    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM Vehicles v JOIN Customers c ON v.customer_id = c.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    // Save or update a vehicle
    public Vehicle save(Vehicle vehicle) {
        String sql;
        boolean isUpdate = vehicle.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE Vehicles SET customer_id = ?, make_id = ?, model_id = ?, year = ?, vin = ?, license_plate = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO Vehicles (customer_id, make_id, model_id, year, vin, license_plate) VALUES (?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, vehicle.getCustomerId());
            stmt.setObject(2, vehicle.getMake() != null ? vehicle.getMake().getId() : null, Types.INTEGER);
            stmt.setObject(3, vehicle.getModel() != null ? vehicle.getModel().getId() : null, Types.INTEGER);
            stmt.setObject(4, vehicle.getYear(), Types.INTEGER);
            stmt.setString(5, vehicle.getVin());
            stmt.setString(6, vehicle.getLicensePlate());

            if (isUpdate) {
                stmt.setInt(7, vehicle.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (!isUpdate && affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    vehicle.setId(generatedKeys.getInt(1));
                }
            }

            return vehicle;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete a vehicle by ID
    public boolean deleteById(int id) {
        String sql = "DELETE FROM Vehicles WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Check if a vehicle exists by ID
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM Vehicles WHERE id = ?";

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

    // Check if a vehicle with that VIN is already exists in the Database
    public boolean existsByVIN(String vin) {
        String sql = "SELECT COUNT(*) FROM Vehicles WHERE vin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Count all vehicles
    public long count() {
        String sql = "SELECT COUNT(*) FROM Vehicles";

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

    // Helper method to map ResultSet to Vehicle entity
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        customer.setCity(rs.getString("city"));
        customer.setState(Customer.State.valueOf(rs.getString("state")));
        customer.setPostalCode(rs.getString("postal_code"));

        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("id"));
        vehicle.setCustomerId(customer.getId());
        vehicle.setYear(rs.getInt("year"));
        vehicle.setVin(rs.getString("vin"));
        vehicle.setLicensePlate(rs.getString("license_plate"));

        return vehicle;
    }

    private Vehicle mapResultSetToVehicleWithCustomer(ResultSet rs, Customer customer) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("id"));
        vehicle.setVin(rs.getString("vin"));

        Integer year = (rs.getInt("year")) == 0 ? null : rs.getInt("year");
        vehicle.setYear(year);

        vehicle.setLicensePlate(rs.getString("license_plate"));

        int makeId = rs.getInt("make_id");
        if (!rs.wasNull()) {
            vehicle.setMake(new Make(makeId, rs.getString("make_name")));
        }

        int modelId = rs.getInt("model_id");
        if (!rs.wasNull()) {
            vehicle.setModel(new Model(modelId, rs.getString("model_name")));
        }

        vehicle.setCustomerId(customer.getId());

        return vehicle;
    }
}
