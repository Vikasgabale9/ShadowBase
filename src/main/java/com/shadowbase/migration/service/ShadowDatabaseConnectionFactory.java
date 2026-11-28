
package com.shadowbase.migration.service;

import com.shadowbase.migration.config.ShadowDatabaseProperties;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ShadowDatabaseConnectionFactory {

    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile(
                    "^(jdbc:postgresql://[^/]+/)([^/?]+)(\\?.*)?$"
            );

    private static final Pattern SAFE_IDENTIFIER =
            Pattern.compile("[a-z][a-z0-9_]*");

    private final ShadowDatabaseProperties properties;
    private final ShadowDatabaseManager shadowDatabaseManager;

    public ShadowDatabaseConnectionFactory(
            ShadowDatabaseProperties properties,
            ShadowDatabaseManager shadowDatabaseManager) {

        this.properties = properties;
        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    /**
     * Creates a JDBC connection to the shadow database
     * belonging to the given migration.
     *
     * The returned connection must be closed by the caller.
     */
    public Connection getConnection(Long migrationId)
            throws SQLException {

        if (migrationId == null || migrationId <= 0) {
            throw new IllegalArgumentException(
                    "Migration ID must be positive"
            );
        }

        if (!shadowDatabaseManager.shadowDatabaseExists(migrationId)) {
            throw new IllegalStateException(
                    "Shadow database does not exist for migration: "
                            + migrationId
            );
        }

        String databaseName =
                properties.getDatabasePrefix() + migrationId;

        if (!SAFE_IDENTIFIER.matcher(databaseName).matches()) {
            throw new IllegalStateException(
                    "Invalid shadow database name"
            );
        }

        String shadowUrl = buildShadowDatabaseUrl(databaseName);

        return DriverManager.getConnection(
                shadowUrl,
                properties.getUsername(),
                properties.getPassword()
        );
    }

    /**
     * Replaces the maintenance database name in the
     * configured administrative JDBC URL.
     */
    private String buildShadowDatabaseUrl(String databaseName) {

        String adminUrl = properties.getAdminUrl();

        Matcher matcher = JDBC_URL_PATTERN.matcher(adminUrl);

        if (!matcher.matches()) {
            throw new IllegalStateException(
                    "Invalid PostgreSQL administrative JDBC URL"
            );
        }

        String serverPart = matcher.group(1);
        String queryParameters = matcher.group(3) == null
                ? ""
                : matcher.group(3);

        return serverPart + databaseName + queryParameters;
    }
}
