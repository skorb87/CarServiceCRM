package me.skorb.repository;

import me.skorb.entity.Make;
import me.skorb.entity.Model;
import me.skorb.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MakeRepository {

    // Private constructor to prevent instantiation
    private MakeRepository() {}

    // Inner static helper class for thread-safe Singleton
    private static class Holder {
        private static final MakeRepository INSTANCE = new MakeRepository();
    }

    // Public method to get the Singleton instance
    public static MakeRepository getInstance() {
        return Holder.INSTANCE;
    }

    // Find Make by ID (including Models)
    public Optional<Make> findById(int id) {
        String sql = "SELECT * FROM Makes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Make make = mapResultSetToMake(rs);
                make.setModels(getModelsForMake(make)); // Fetch related models
                return Optional.of(make);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Find Make by Name (including Models)
    public Optional<Make> findByName(String name) {
        String sql = "SELECT * FROM Makes WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Make make = mapResultSetToMake(rs);
                make.setModels(getModelsForMake(make)); // Fetch related models
                return Optional.of(make);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // Fetch all Makes with their Models
    public List<Make> findAll() {
        List<Make> makes = new ArrayList<>();
        String sql = "SELECT * FROM Makes";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Make make = mapResultSetToMake(rs);
                make.setModels(getModelsForMake(make)); // Fetch related models
                makes.add(make);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return makes;
    }

    // Save a new Make (or update if it already exists)
    public Make save(Make make) {
        String sql;
        boolean isUpdate = make.getId() > 0;

        if (isUpdate) {
            sql = "UPDATE Makes SET name = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO Makes (name) VALUES (?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, make.getName());

            if (isUpdate) {
                stmt.setInt(2, make.getId());
            }

            int affectedRows = stmt.executeUpdate();

            if (!isUpdate && affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    make.setId(generatedKeys.getInt(1));
                }
            }

            return make;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Delete Make by ID (Cascade deletes Models due to DB constraint)
    public boolean deleteById(int id) {
        String sql = "DELETE FROM Makes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Fetch Models associated with a specific Make
    private List<Model> getModelsForMake(Make make) {
        List<Model> models = new ArrayList<>();
        String sql = "SELECT * FROM Models WHERE make_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, make.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                models.add(mapResultSetToModelWithMake(rs, make));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return models;
    }

    // Helper method to map ResultSet to Make entity
    private Make mapResultSetToMake(ResultSet rs) throws SQLException {
        Make make = new Make();
        make.setId(rs.getInt("id"));
        make.setName(rs.getString("name"));

        return make;
    }

    // Helper method to map ResultSet to Model entity
    private Model mapResultSetToModelWithMake(ResultSet rs, Make make) throws SQLException {
        Model model = new Model();
        model.setId(rs.getInt("id"));
        model.setName(rs.getString("name"));
        model.setMakeId(make.getId());

        return model;
    }
}
