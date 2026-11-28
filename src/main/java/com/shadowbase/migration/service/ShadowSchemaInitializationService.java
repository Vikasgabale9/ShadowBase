
package com.shadowbase.migration.service;

import com.shadowbase.migration.config.SchemaInitializationProperties;
import com.shadowbase.migration.entity.Migration;
import com.shadowbase.migration.enums.SchemaInitializationStatus;
import com.shadowbase.migration.repository.MigrationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ShadowSchemaInitializationService {

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile(
            "^jdbc:postgresql://([^/:?]+)(?::(\\d+))?/([^?]+)"
    );

    private static final Pattern PUBLIC_SCHEMA_CREATION_PATTERN =
            Pattern.compile(
                    "(?im)^CREATE SCHEMA public;[ \\t]*(?:\\r?\\n)?"
            );

    private final DataSource sourceDataSource;
    private final MigrationRepository migrationRepository;
    private final ShadowDatabaseManager shadowDatabaseManager;
    private final SchemaInitializationProperties properties;

    @Value("${spring.datasource.username}")
    private String sourceUsername;

    @Value("${spring.datasource.password}")
    private String sourcePassword;

    @Value("${shadowbase.shadow-db.admin-url}")
    private String shadowAdminUrl;

    @Value("${shadowbase.shadow-db.username}")
    private String shadowUsername;

    @Value("${shadowbase.shadow-db.password}")
    private String shadowPassword;

    @Value("${shadowbase.shadow-db.database-prefix}")
    private String shadowDatabasePrefix;

    public ShadowSchemaInitializationService(
            DataSource sourceDataSource,
            MigrationRepository migrationRepository,
            ShadowDatabaseManager shadowDatabaseManager,
            SchemaInitializationProperties properties
    ) {
        this.sourceDataSource = sourceDataSource;
        this.migrationRepository = migrationRepository;
        this.shadowDatabaseManager = shadowDatabaseManager;
        this.properties = properties;
    }

    /**
     * Initializes the shadow database with the source schema.
     */
    public void initializeShadowSchema(
            Long migrationId,
            String createdBy
    ) {

        Connection lockConnection =
                acquireInitializationLock(migrationId);

        Path dumpFile = null;
        Path logFile = null;
        Migration migration = null;

        try {

            migration = getOwnedMigration(migrationId, createdBy);

            SchemaInitializationStatus status =
                    migration.getSchemaInitializationStatus();

            if (status == SchemaInitializationStatus.INITIALIZED) {
                throw new IllegalStateException(
                        "Shadow schema has already been initialized"
                );
            }

            if (status == SchemaInitializationStatus.IN_PROGRESS) {
                throw new IllegalStateException(
                        "Schema initialization is already in progress"
                );
            }

            if (status == SchemaInitializationStatus.FAILED) {
                throw new IllegalStateException(
                        "Previous initialization failed. "
                                + "Recover the shadow database before retrying."
                );
            }

            String shadowDatabase =
                    validateShadowDatabase(migration, migrationId);

            if (!shadowDatabaseManager.shadowDatabaseExists(migrationId)) {
                throw new IllegalStateException(
                        "Shadow database does not exist"
                );
            }

            ensureShadowDatabaseIsEmpty(shadowDatabase);

            // Persist IN_PROGRESS before executing PostgreSQL utilities.
            migration.setSchemaInitializationStatus(
                    SchemaInitializationStatus.IN_PROGRESS
            );

            migrationRepository.saveAndFlush(migration);

            DatabaseConnectionDetails sourceDetails =
                    getConnectionDetails(sourceDataSource);

            DatabaseConnectionDetails shadowDetails =
                    parseJdbcUrl(shadowAdminUrl);

            dumpFile = Files.createTempFile(
                    "shadowbase-schema-", ".sql"
            );

            logFile = Files.createTempFile(
                    "shadowbase-process-", ".log"
            );

            // Export source schema.
            exportSourceSchema(
                    sourceDetails,
                    dumpFile,
                    logFile
            );

            // Restore schema into shadow database.
            restoreSchema(
                    shadowDetails,
                    shadowDatabase,
                    dumpFile,
                    logFile
            );

            // Persist successful initialization.
            migration.setSchemaInitializationStatus(
                    SchemaInitializationStatus.INITIALIZED
            );

            migrationRepository.saveAndFlush(migration);

        } catch (Exception exception) {

            /*
             * If initialization had started, attempt to record FAILED.
             */
            if (migration != null) {

                SchemaInitializationStatus currentStatus =
                        migration.getSchemaInitializationStatus();

                if (currentStatus == SchemaInitializationStatus.IN_PROGRESS
                        || currentStatus == SchemaInitializationStatus.INITIALIZED) {

                    try {
                        migration.setSchemaInitializationStatus(
                                SchemaInitializationStatus.FAILED
                        );

                        migrationRepository.saveAndFlush(migration);

                    } catch (Exception statusException) {
                        exception.addSuppressed(statusException);
                    }
                }
            }

            throw new IllegalStateException(
                    "Shadow schema initialization failed: "
                            + exception.getMessage(),
                    exception
            );

        } finally {

            deleteTemporaryFile(dumpFile);
            deleteTemporaryFile(logFile);

            releaseInitializationLock(
                    lockConnection,
                    migrationId
            );
        }
    }

    /**
     * Recovers a failed initialization by recreating its shadow database.
     *
     * WARNING: Existing shadow database contents are permanently deleted.
     */
    public void recoverFailedInitialization(
            Long migrationId,
            String createdBy
    ) {

        Connection lockConnection =
                acquireInitializationLock(migrationId);

        try {

            Migration migration =
                    getOwnedMigration(migrationId, createdBy);

            if (migration.getSchemaInitializationStatus()
                    != SchemaInitializationStatus.FAILED) {

                throw new IllegalStateException(
                        "Only failed schema initializations can be recovered"
                );
            }

            String expectedDatabaseName =
                    generateExpectedDatabaseName(migrationId);

            if (!expectedDatabaseName.equals(migration.getShadowDatabase())) {
                throw new IllegalStateException(
                        "Invalid shadow database assignment"
                );
            }

            // Delete only the managed shadow database.
            shadowDatabaseManager.deleteShadowDatabase(migrationId);

            // Recreate an empty shadow database.
            String recreatedDatabase =
                    shadowDatabaseManager.createShadowDatabase(migrationId);

            if (!expectedDatabaseName.equals(recreatedDatabase)) {
                throw new IllegalStateException(
                        "Recreated database name does not match"
                );
            }

            migration.setSchemaInitializationStatus(
                    SchemaInitializationStatus.NOT_STARTED
            );

            migrationRepository.saveAndFlush(migration);

        } finally {

            releaseInitializationLock(
                    lockConnection,
                    migrationId
            );
        }
    }

    /**
     * Retrieves a migration and verifies ownership.
     */
    private Migration getOwnedMigration(
            Long migrationId,
            String createdBy
    ) {

        Migration migration = migrationRepository.findById(migrationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Migration not found")
                );

        if (!Objects.equals(migration.getCreatedBy(), createdBy)) {
            throw new IllegalArgumentException("Migration not found");
        }

        return migration;
    }

    /**
     * Validates the shadow database name against the configured prefix.
     */
    private String validateShadowDatabase(
            Migration migration,
            Long migrationId
    ) {

        String shadowDatabase = migration.getShadowDatabase();

        if (shadowDatabase == null || shadowDatabase.isBlank()) {
            throw new IllegalStateException(
                    "Shadow database has not been created"
            );
        }

        String expectedDatabaseName =
                generateExpectedDatabaseName(migrationId);

        if (!shadowDatabase.equals(expectedDatabaseName)
                || !shadowDatabase.matches("[a-z][a-z0-9_]*")) {

            throw new IllegalStateException(
                    "Invalid shadow database assignment"
            );
        }

        return shadowDatabase;
    }

    private String generateExpectedDatabaseName(Long migrationId) {

        if (migrationId == null || migrationId <= 0) {
            throw new IllegalArgumentException(
                    "Migration ID must be positive"
            );
        }

        if (shadowDatabasePrefix == null
                || !shadowDatabasePrefix.matches("[a-z][a-z0-9_]*")) {

            throw new IllegalStateException(
                    "Invalid shadow database prefix"
            );
        }

        String expectedName = shadowDatabasePrefix + migrationId;

        if (expectedName.length() > 63) {
            throw new IllegalStateException(
                    "Shadow database name exceeds PostgreSQL limit"
            );
        }

        return expectedName;
    }

    /**
     * Ensures that the public schema of the shadow database is empty.
     */
    private void ensureShadowDatabaseIsEmpty(
            String databaseName
    ) throws SQLException {

        DatabaseConnectionDetails adminDetails =
                parseJdbcUrl(shadowAdminUrl);

        String shadowJdbcUrl = "jdbc:postgresql://"
                + adminDetails.host()
                + ":" + adminDetails.port()
                + "/" + databaseName;

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM pg_class c
                    JOIN pg_namespace n
                        ON n.oid = c.relnamespace
                    WHERE n.nspname = 'public'
                      AND c.relkind IN ('r', 'p', 'v', 'm', 'S', 'f')
                )
                OR EXISTS (
                    SELECT 1
                    FROM pg_proc p
                    JOIN pg_namespace n
                        ON n.oid = p.pronamespace
                    WHERE n.nspname = 'public'
                )
                OR EXISTS (
                    SELECT 1
                    FROM pg_type t
                    JOIN pg_namespace n
                        ON n.oid = t.typnamespace
                    WHERE n.nspname = 'public'
                      AND t.typtype IN ('e', 'd', 'r')
                )
                """;

        try (
                Connection connection = DriverManager.getConnection(
                        shadowJdbcUrl,
                        shadowUsername,
                        shadowPassword
                );

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet = statement.executeQuery()
        ) {

            if (resultSet.next() && resultSet.getBoolean(1)) {
                throw new IllegalStateException(
                        "Shadow database is not empty. "
                                + "Schema initialization was rejected."
                );
            }
        }
    }

    /**
     * Exports the public schema using pg_dump.
     */
    private void exportSourceSchema(
            DatabaseConnectionDetails source,
            Path dumpFile,
            Path logFile
    ) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();

        command.add(properties.getPgDumpPath());
        command.add("--host=" + source.host());
        command.add("--port=" + source.port());
        command.add("--username=" + source.username());
        command.add("--dbname=" + source.database());
        command.add("--schema-only");
        command.add("--schema=public");
        command.add("--no-owner");
        command.add("--no-privileges");
        command.add("--format=plain");
        command.add("--file=" + dumpFile.toAbsolutePath());

        executeCommand(command, source.password(), logFile);

        // PostgreSQL creates the public schema automatically.
        // Remove only the redundant CREATE SCHEMA statement.
        removeExistingPublicSchemaCreation(dumpFile);
    }

    /**
     * Removes the CREATE SCHEMA public statement from the dump.
     *
     * The public schema already exists in a newly created PostgreSQL
     * database, so restoring this statement causes a duplicate-schema error.
     */
    private void removeExistingPublicSchemaCreation(
            Path dumpFile
    ) throws IOException {

        String sql = Files.readString(dumpFile);

        Matcher matcher =
                PUBLIC_SCHEMA_CREATION_PATTERN.matcher(sql);

        if (!matcher.find()) {
            throw new IllegalStateException(
                    "CREATE SCHEMA public statement was not found in the dump"
            );
        }

        String updatedSql = matcher.replaceFirst("");

        Files.writeString(dumpFile, updatedSql);
    }

    /**
     * Restores the exported schema using psql.
     */
    private void restoreSchema(
            DatabaseConnectionDetails target,
            String shadowDatabase,
            Path dumpFile,
            Path logFile
    ) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();

        command.add(properties.getPsqlPath());
        command.add("--host=" + target.host());
        command.add("--port=" + target.port());
        command.add("--username=" + shadowUsername);
        command.add("--dbname=" + shadowDatabase);
        command.add("--set=ON_ERROR_STOP=1");
        command.add("--single-transaction");
        command.add("--file=" + dumpFile.toAbsolutePath());

        executeCommand(command, shadowPassword, logFile);
    }

    /**
     * Executes a PostgreSQL command with a timeout.
     */
    private void executeCommand(
            List<String> command,
            String password,
            Path logFile
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(logFile.toFile());

        processBuilder.environment().put(
                "PGPASSWORD",
                password == null ? "" : password
        );

        Process process = processBuilder.start();

        boolean completed = process.waitFor(
                5,
                TimeUnit.MINUTES
        );

        if (!completed) {
            process.destroyForcibly();
            process.waitFor();

            throw new IllegalStateException(
                    "PostgreSQL command timed out"
            );
        }

        if (process.exitValue() != 0) {

            String output = Files.readString(logFile);

            throw new IllegalStateException(
                    "PostgreSQL command failed. Output: " + output
            );
        }
    }

    /**
     * Reads the source database connection details.
     */
    private DatabaseConnectionDetails getConnectionDetails(
            DataSource dataSource
    ) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata =
                    connection.getMetaData();

            DatabaseConnectionDetails details =
                    parseJdbcUrl(metadata.getURL());

            return new DatabaseConnectionDetails(
                    details.host(),
                    details.port(),
                    details.database(),
                    sourceUsername,
                    sourcePassword
            );
        }
    }

    /**
     * Parses a PostgreSQL JDBC URL.
     */
    private DatabaseConnectionDetails parseJdbcUrl(
            String jdbcUrl
    ) {

        Matcher matcher = JDBC_URL_PATTERN.matcher(jdbcUrl);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Unsupported PostgreSQL JDBC URL"
            );
        }

        String host = matcher.group(1);

        String port = matcher.group(2) == null
                ? "5432"
                : matcher.group(2);

        String database = matcher.group(3).split("\\?")[0];

        return new DatabaseConnectionDetails(
                host,
                port,
                database,
                null,
                null
        );
    }

    /**
     * Deletes temporary files.
     */
    private void deleteTemporaryFile(Path path) {

        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                // Preserve the original operation result.
            }
        }
    }

    /**
     * Acquires a PostgreSQL session-level advisory lock.
     */
    private Connection acquireInitializationLock(Long migrationId) {

        try {
            Connection connection = sourceDataSource.getConnection();

            String sql = """
                    SELECT pg_try_advisory_lock(
                        hashtextextended(?, 0)
                    )
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(
                        1,
                        "shadowbase-schema-initialization:" + migrationId
                );

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next() && resultSet.getBoolean(1)) {
                        return connection;
                    }
                }
            }

            connection.close();

            throw new IllegalStateException(
                    "Schema initialization or recovery is already running "
                            + "for this migration"
            );

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not acquire schema initialization lock",
                    exception
            );
        }
    }

    /**
     * Releases the PostgreSQL advisory lock.
     */
    private void releaseInitializationLock(
            Connection connection,
            Long migrationId
    ) {

        if (connection == null) {
            return;
        }

        try {
            String sql = """
                    SELECT pg_advisory_unlock(
                        hashtextextended(?, 0)
                    )
                    """;

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(
                        1,
                        "shadowbase-schema-initialization:" + migrationId
                );

                statement.executeQuery();
            }

        } catch (SQLException exception) {
            // Closing the connection also releases its session-level lock.
        } finally {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Preserve the original operation result.
            }
        }
    }

    private record DatabaseConnectionDetails(
            String host,
            String port,
            String database,
            String username,
            String password
    ) {
    }
}
