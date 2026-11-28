
package com.shadowbase.migration.service;

import com.shadowbase.migration.config.ShadowDatabaseProperties;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

@Service
public class ShadowDatabaseManager {

    private static final Pattern SAFE_PREFIX =
            Pattern.compile("[a-z][a-z0-9_]*");

    private final ShadowDatabaseProperties properties;

    public ShadowDatabaseManager(
            ShadowDatabaseProperties properties
    ) {
        this.properties = properties;
    }

    /**
     * Creates a shadow database for a migration.
     */
    public String createShadowDatabase(Long migrationId) {

        String databaseName = generateDatabaseName(migrationId);

        try (Connection connection = getAdminConnection()) {

            if (databaseExists(connection, databaseName)) {
                throw new IllegalStateException(
                        "Shadow database already exists: " + databaseName
                );
            }

            String sql = "CREATE DATABASE " + quoteIdentifier(databaseName);

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

            return databaseName;

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to create shadow database: " + databaseName,
                    exception
            );
        }
    }

    /**
     * Checks whether a shadow database exists.
     */
    public boolean shadowDatabaseExists(Long migrationId) {

        String databaseName = generateDatabaseName(migrationId);

        try (Connection connection = getAdminConnection()) {
            return databaseExists(connection, databaseName);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to check shadow database existence",
                    exception
            );
        }
    }

    /**
     * Deletes only the database generated for this migration.
     */
    public void deleteShadowDatabase(Long migrationId) {

        String databaseName = generateDatabaseName(migrationId);

        try (Connection connection = getAdminConnection()) {

            if (!databaseExists(connection, databaseName)) {
                return;
            }

            String sql = "DROP DATABASE "
                    + quoteIdentifier(databaseName)
                    + " WITH (FORCE)";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Failed to delete shadow database: " + databaseName,
                    exception
            );
        }
    }

    /**
     * Generates a deterministic database name.
     */
    private String generateDatabaseName(Long migrationId) {

        if (migrationId == null || migrationId <= 0) {
            throw new IllegalArgumentException(
                    "Migration ID must be positive"
            );
        }

        String prefix = properties.getDatabasePrefix();

        if (prefix == null || !SAFE_PREFIX.matcher(prefix).matches()) {
            throw new IllegalStateException(
                    "Invalid shadow database prefix"
            );
        }

        String databaseName = prefix + migrationId;

        if (databaseName.length() > 63) {
            throw new IllegalStateException(
                    "Generated PostgreSQL database name exceeds 63 characters"
            );
        }

        return databaseName;
    }

    /**
     * Opens a separate administrative PostgreSQL connection.
     */
    private Connection getAdminConnection() throws SQLException {

        return DriverManager.getConnection(
                properties.getAdminUrl(),
                properties.getUsername(),
                properties.getPassword()
        );
    }

    /**
     * Checks PostgreSQL system catalog for a database.
     */
    private boolean databaseExists(
            Connection connection,
            String databaseName
    ) throws SQLException {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM pg_database
                    WHERE datname = ?
                )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, databaseName);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        }
    }

    /**
     * Safely quotes a validated PostgreSQL identifier.
     */
    private String quoteIdentifier(String identifier) {

        if (identifier == null
                || !SAFE_PREFIX.matcher(identifier).matches()) {
            throw new IllegalArgumentException(
                    "Invalid database identifier"
            );
        }

        return "\"" + identifier + "\"";
    }
}
