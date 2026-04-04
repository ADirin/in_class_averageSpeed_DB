package org.example.avgspd;

import org.example.avgspd.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AverageSpeedDAO {

    private static final String INSERT_SQL =
            "INSERT INTO average_speed(distance, time, avg_speed, created_at, " +
                    "distance_localized, time_localized, avg_speed_localized, avg_result_localized, language_code) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void saveRecord(
            double distance,
            double time,
            double avgSpeed,
            String distanceLocalized,
            String timeLocalized,
            String avgSpeedLocalized,
            String avgResultLocalized,
            String languageCode
    ) {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setDouble(1, distance);
            stmt.setDouble(2, time);
            stmt.setDouble(3, avgSpeed);
            stmt.setObject(4, LocalDateTime.now());

            stmt.setString(5, distanceLocalized);
            stmt.setString(6, timeLocalized);
            stmt.setString(7, avgSpeedLocalized);
            stmt.setString(8, avgResultLocalized);
            stmt.setString(9, languageCode);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* row database localization
  private static final String INSERT_SQL =
          "INSERT INTO average_speed(distance, time, avg_speed, created_at, locale_code) " +
                  "VALUES (?, ?, ?, ?, ?)";

    public static void saveRecord(double distance, double time,
                                  double avgSpeed, String localeCode) {
        if (localeCode == null || localeCode.trim().isEmpty()) {
            localeCode = "en";  // Default fallback
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setDouble(1, distance);
            stmt.setDouble(2, time);
            stmt.setDouble(3, avgSpeed);
            stmt.setObject(4, LocalDateTime.now());
            stmt.setString(5, localeCode);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Insert failed, no rows affected");
            }

        } catch (SQLException e) {
            // Log properly instead of printStackTrace
            System.err.println("Database error saving record: " + e.getMessage());
            throw new RuntimeException("Failed to save speed record", e);
        }
    }*/
}
