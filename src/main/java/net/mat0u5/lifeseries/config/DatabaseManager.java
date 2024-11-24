package net.mat0u5.lifeseries.config;

import com.google.gson.Gson;
import net.mat0u5.lifeseries.Main;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {

    private static final String FOLDER_PATH = "./config/"+ Main.MOD_ID;
    private static final String FILE_PATH = FOLDER_PATH+"/"+Main.MOD_ID+".db";
    public static final String URL = "jdbc:sqlite:"+FILE_PATH;

    public static void initialize() {
        System.out.println("Initializing database");
        try {
            createFolderIfNotExists();
            try (Connection connection = DriverManager.getConnection(URL)) {
                if (connection != null) {
                    System.out.println("Connection established successfully.");
                    createDoubleLifeSoulmatesTable(connection);
                    System.out.println("Database initialized.");
                } else {
                    System.err.println("Failed to establish connection.");
                }
            } catch (SQLException e) {
                System.err.println("SQLException encountered during initialization: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unexpected error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void createFolderIfNotExists() {
        File folder = new File(FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
    private static void createDoubleLifeSoulmatesTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS DoubleLifeSoulmates (" +
                "player TEXT NOT NULL PRIMARY KEY," +
                "soulmate TEXT NOT NULL" +
                ");";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeUpdate();
    }
    public static void deleteDoubleLifeSoulmates() {
        String sql = "DELETE FROM DoubleLifeSoulmates";
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setAllSoulmates(Map<UUID, UUID> soulmates) {
        String sql = "INSERT OR REPLACE INTO DoubleLifeSoulmates (player, soulmate) VALUES (?, ?);";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Begin transaction
            connection.setAutoCommit(false);

            for (Map.Entry<UUID, UUID> entry : soulmates.entrySet()) {
                statement.setString(1, entry.getKey().toString());
                statement.setString(2, entry.getValue().toString());
                statement.addBatch();
            }

            // Execute batch update
            statement.executeBatch();

            // Commit transaction
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static Map<UUID, UUID> getAllSoulmates() {
        String sql = "SELECT player, soulmate FROM DoubleLifeSoulmates";
        Map<UUID, UUID> soulmates = new HashMap<>();

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                UUID player = UUID.fromString(resultSet.getString("player"));
                UUID soulmate = UUID.fromString(resultSet.getString("soulmate"));
                soulmates.put(player, soulmate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return soulmates;
    }
}
