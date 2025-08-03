package me.skorb.repository;

import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModelRepository {

    // Private constructor to prevent instantiation
    private ModelRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final ModelRepository INSTANCE = new ModelRepository();
    }

    // Public method to get the Singleton instance
    public static ModelRepository getInstance() {
        return Holder.INSTANCE;
    }

    // Find Model by ID
    public Optional<Model> findById(int id) {
        String sql = "SELECT * FROM Models WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToModel(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Find Model by Name and Make
    public Optional<Model> findByNameAndMake(String name, Make make) {
        String sql = "SELECT * FROM Models WHERE name = ? AND make_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, make.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToModelWithMake(rs, make));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Fetch all Models
    public List<Model> findAll() {
        List<Model> models = new ArrayList<>();
        String sql = "SELECT * FROM Models";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                models.add(mapResultSetToModel(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return models;
    }

    // Save a new Model (or update if it already exists)
    public Model save(Model model) {
        String sql;
        boolean isUpdate = model.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE Models SET name = ?, make_id = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO Models (name, make_id) VALUES (?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, model.getName());
            stmt.setInt(2, model.getMakeId());

            if (isUpdate) {
                stmt.setInt(3, model.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (!isUpdate && affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    model.setId(generatedKeys.getInt(1));
                }
            }

            return model;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete Model by ID
    public boolean deleteById(int id) {
        String sql = "DELETE FROM Models WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Count all Models
    public long count() {
        String sql = "SELECT COUNT(*) FROM Models";

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

    // Check if a Model exists by ID
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM Models WHERE id = ?";

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

    // Helper method to map ResultSet to Model entity
    private Model mapResultSetToModel(ResultSet rs) throws SQLException {
        Make make = new Make(rs.getInt("make_id"), null); // Fetch Make separately if needed
        Model model = new Model();
        model.setId(rs.getInt("id"));
        model.setName(rs.getString("name"));
        model.setMakeId(make.getId());

        return model;
    }

    private Model mapResultSetToModelWithMake(ResultSet rs, Make make) throws SQLException {
        Model model = new Model();
        model.setId(rs.getInt("id"));
        model.setName(rs.getString("name"));
        model.setMakeId(make.getId());

        return model;
    }
}
