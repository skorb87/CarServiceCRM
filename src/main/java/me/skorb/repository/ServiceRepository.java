package me.skorb.repository;

import me.skorb.entity.Service;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {

    // Private constructor to prevent instantiation
    private ServiceRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final ServiceRepository INSTANCE = new ServiceRepository();
    }

    // Public method to get the Singleton instance
    public static ServiceRepository getInstance() {
        return Holder.INSTANCE;
    }

    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, c.name AS category_name FROM Services s " +
                "LEFT JOIN ServiceCategories c ON s.category_id = c.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public Service getServiceById(int id) {
        String sql = "SELECT s.*, c.name AS category_name FROM Services s " +
                "LEFT JOIN ServiceCategories c ON s.category_id = c.id WHERE s.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToService(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Service> getServiceByCategoryId(int categoryId) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, c.name AS category_name FROM Services s " +
                "LEFT JOIN ServiceCategories c ON s.category_id = c.id WHERE s.category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public List<Service> getServiceByCategoryName(String categoryName) {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT s.*, c.name AS category_name FROM Services s " +
                "LEFT JOIN ServiceCategories c ON s.category_id = c.id " +
                "WHERE c.name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public List<String> getAllServiceCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM ServiceCategories";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public int getCategoryIdByCategoryName(String categoryName) {
        int categoryId = 0;

        String sql = "SELECT id FROM ServiceCategories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                categoryId = rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categoryId;
    }

    public void save(Service service) {
        String sql = "INSERT INTO Services (name, category_id, description, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, service.getName());
            stmt.setInt(2, service.getCategoryId());
            stmt.setString(3, service.getDescription());
            stmt.setBigDecimal(4, service.getPrice());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCategory(String categoryName) {
        String sql = "INSERT INTO ServiceCategories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Service service) {
        String sql = "UPDATE Services SET name = ?, category_id = ?, description = ?, price = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            stmt.setInt(2, service.getCategoryId());
            stmt.setString(3, service.getDescription());
            stmt.setBigDecimal(4, service.getPrice());
            stmt.setInt(5, service.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCategory(int id, String newName) {
        String sql = "UPDATE ServiceCategories SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM Services WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(String categoryName) {
        String sql = "DELETE FROM ServiceCategories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCategoryUsed(String categoryName) {
        String sql = "SELECT COUNT(*) FROM Services s " +
                "JOIN ServiceCategories c ON s.category_id = c.id " +
                "WHERE c.name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Returns true if category is still in use
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getInt("id"));
        service.setName(rs.getString("name"));
        service.setCategoryId(rs.getInt("category_id"));
        service.setCategoryName(rs.getString("category_name"));
        service.setDescription(rs.getString("description"));
        service.setPrice(rs.getBigDecimal("price"));
        return service;
    }

}
