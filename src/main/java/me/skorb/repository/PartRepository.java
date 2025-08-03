package me.skorb.repository;

import me.skorb.entity.Part;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartRepository {

    // Private constructor to prevent instantiation
    private PartRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final PartRepository INSTANCE = new PartRepository();
    }

    // Public method to get the Singleton instance
    public static PartRepository getInstance() {
        return Holder.INSTANCE;
    }

    public List<Part> getAllParts() {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM Parts p " +
                "LEFT JOIN PartCategories c ON p.category_id = c.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                parts.add(mapResultSetToPart(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return parts;
    }

    public Part getPartById(int id) {
        String sql = "SELECT p.*, c.name AS category_name FROM Parts p " +
                "LEFT JOIN PartCategories c ON p.category_id = c.id WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPart(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Part> getPartsByName(String name) {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT p.*, c.name AS category_name FROM Parts p " +
                "LEFT JOIN PartCategories c ON p.category_id = c.id " +
                "WHERE LOWER(p.name) LIKE LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%"); // Allow partial name matches
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                parts.add(mapResultSetToPart(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parts;
    }

    public List<String> getAllPartCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM PartCategories";
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

        String sql = "SELECT id FROM PartCategories WHERE name = ?";
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

    public void save(Part part) {
        String sql = "INSERT INTO Parts (name, description, category_id, price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, part.getName());
            stmt.setString(2, part.getDescription());
            stmt.setInt(3, part.getCategoryId());
            stmt.setBigDecimal(4, part.getPrice());
            stmt.setInt(5, part.getStockQuantity());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveCategory(String categoryName) {
        String sql = "INSERT INTO PartCategories (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Part part) {
        String sql = "UPDATE Parts SET name = ?, description = ?, category_id = ?, price = ?, stock_quantity = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, part.getName());
            stmt.setString(2, part.getDescription());
            stmt.setInt(3, part.getCategoryId());
            stmt.setBigDecimal(4, part.getPrice());
            stmt.setInt(5, part.getStockQuantity());
            stmt.setInt(6, part.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCategory(int id, String newName) {
        String sql = "UPDATE PartCategories SET name = ? WHERE id = ?";
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
        String sql = "DELETE FROM Parts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(String categoryName) {
        String sql = "DELETE FROM PartCategories WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCategoryUsed(String categoryName) {
        String sql = "SELECT COUNT(*) FROM Parts p " +
                "JOIN PartCategories c ON p.category_id = c.id " +
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

    private Part mapResultSetToPart(ResultSet rs) throws SQLException {
        Part part = new Part();
        part.setId(rs.getInt("id"));
        part.setName(rs.getString("name"));
        part.setDescription(rs.getString("description"));
        part.setCategoryId(rs.getInt("category_id"));
        part.setCategoryName(rs.getString("category_name"));
        part.setPrice(rs.getBigDecimal("price"));
        part.setStockQuantity(rs.getInt("stock_quantity"));
        return part;
    }

}
