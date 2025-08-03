package me.skorb.repository;

import me.skorb.entity.Employee;
import me.skorb.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {

    // Private constructor to prevent instantiation
    private EmployeeRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final EmployeeRepository INSTANCE = new EmployeeRepository();
    }

    // Public method to get the Singleton instance
    public static EmployeeRepository getInstance() {
        return Holder.INSTANCE;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM Employees";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Employee employee = mapResultSetToEmployee(rs);
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public void save(Employee employee) {
        String sql = "INSERT INTO Employees (first_name, last_name, role, labor_price, phone, email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getRole().name());
            stmt.setBigDecimal(4, employee.getLaborPrice());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Employee employee) {
        String sql = "UPDATE Employees SET first_name = ?, last_name = ?, role = ?, labor_price = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getRole().name());
            stmt.setBigDecimal(4, employee.getLaborPrice());
            stmt.setString(5, employee.getPhone());
            stmt.setString(6, employee.getEmail());
            stmt.setInt(7, employee.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int employeeId) {
        String sql = "DELETE FROM Employees WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setRole(Employee.Role.valueOf(rs.getString("role")));
        employee.setLaborPrice(rs.getBigDecimal("labor_price"));
        employee.setPhone(rs.getString("phone"));
        employee.setEmail(rs.getString("email"));
        return employee;
    }
}