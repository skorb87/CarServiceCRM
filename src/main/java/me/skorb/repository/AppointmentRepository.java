package me.skorb.repository;

import me.skorb.entity.*;
import me.skorb.entity.Appointment.Status;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentRepository {

    // Private constructor to prevent instantiation
    private AppointmentRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final AppointmentRepository INSTANCE = new AppointmentRepository();
    }

    // Public method to get the Singleton instance
    public static AppointmentRepository getInstance() {
        return Holder.INSTANCE;
    }

    // Find Appointment by ID
    public Optional<Appointment> findById(int id) {
        String sql = "SELECT * FROM Appointments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Find Appointments by Customer ID
    public List<Appointment> findByCustomerId(int customerId) {
        return findAppointmentsByField("customer_id", customerId);
    }

    // Find Appointments by Vehicle ID
    public List<Appointment> findByVehicleId(int vehicleId) {
        return findAppointmentsByField("vehicle_id", vehicleId);
    }

    // Find Appointments by Status
    public List<Appointment> findByStatus(Status status) {
        String sql = "SELECT * FROM Appointments WHERE status = ?";
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Find Appointments within a date range
    public List<Appointment> findByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
//        String sql = "SELECT * FROM Appointments WHERE date_time BETWEEN ? AND ?";
        String sql = """
                    SELECT
                        a.id AS appointment_id, a.date_time, a.status, a.issue,
                        c.id AS customer_id, c.first_name, c.last_name, c.phone, c.email,
                        v.id AS vehicle_id, v.vin, v.license_plate, v.year,
                        m.name AS model_name, mk.name AS make_name
                    FROM Appointments a
                    JOIN Customers c ON a.customer_id = c.id
                    JOIN Vehicles v ON a.vehicle_id = v.id
                    LEFT JOIN Models m ON v.model_id = m.id
                    LEFT JOIN Makes mk ON v.make_id = mk.id
                    WHERE a.date_time BETWEEN ? AND ?
                """;
        List<Appointment> appointments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Fetch all Appointments with Customer and Vehicle details
    public List<Appointment> findAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = """
                    SELECT
                        a.id AS appointment_id, a.date_time, a.status, a.issue,
                        c.id AS customer_id, c.first_name, c.last_name, c.phone, c.email,
                        v.id AS vehicle_id, v.vin, v.license_plate, v.year,
                        m.name AS model_name, mk.name AS make_name
                    FROM Appointments a
                    JOIN Customers c ON a.customer_id = c.id
                    JOIN Vehicles v ON a.vehicle_id = v.id
                    LEFT JOIN Models m ON v.model_id = m.id
                    LEFT JOIN Makes mk ON v.make_id = mk.id
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public int getAppointmentsCountByStatus(String status) {
        String sql = String.format("SELECT COUNT(*) FROM Appointments WHERE status = '%s'", status);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Save a new Appointment (or update if it already exists)
    public Appointment save(Appointment appointment) {
        String sql;
        boolean isUpdate = appointment.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE Appointments SET customer_id = ?, vehicle_id = ?, date_time = ?, status = ?, issue = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO Appointments (customer_id, vehicle_id, date_time, status, issue) VALUES (?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appointment.getCustomer().getId());
            stmt.setInt(2, appointment.getVehicle().getId());

//            stmt.setTimestamp(3, Timestamp.valueOf(appointment.getDateTime()));
            stmt.setObject(3, appointment.getDateTime());

            stmt.setString(4, appointment.getStatus().name());
            stmt.setString(5, appointment.getNotes());

            if (isUpdate) {
                stmt.setInt(6, appointment.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (!isUpdate && affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    appointment.setId(generatedKeys.getInt(1));
                }
            }

            return appointment;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete Appointment by ID
    public boolean deleteById(int id) {
        String sql = "DELETE FROM Appointments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Count all Appointments
    public long count() {
        String sql = "SELECT COUNT(*) FROM Appointments";

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

    // Check if an Appointment exists by ID
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM Appointments WHERE id = ?";

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

    // Generic method to find appointments by field
    private List<Appointment> findAppointmentsByField(String fieldName, int value) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM Appointments WHERE " + fieldName + " = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appointments.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // Helper method to map ResultSet to Appointment entity
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        // Map Customer
        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));

        // Map Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("vehicle_id"));
        vehicle.setVin(rs.getString("vin"));
        vehicle.setLicensePlate(rs.getString("license_plate"));
        vehicle.setYear(rs.getInt("year"));

        Make make = new Make();
        make.setName(rs.getString("make_name"));
        vehicle.setMake(make);

        Model model = new Model();
        model.setName(rs.getString("model_name"));
        vehicle.setModel(model);

        // Map Appointment
        Appointment appointment = new Appointment();
        appointment.setId(rs.getInt("appointment_id"));
        appointment.setCustomer(customer);
        appointment.setVehicle(vehicle);
        appointment.setDateTime(rs.getObject("date_time", LocalDateTime.class));
        appointment.setStatus(Appointment.Status.valueOf(rs.getString("status")));
        appointment.setNotes(rs.getString("issue"));

        return appointment;
    }



}
