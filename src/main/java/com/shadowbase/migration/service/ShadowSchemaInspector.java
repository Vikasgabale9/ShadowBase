
package com.shadowbase.migration.service;

import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ShadowSchemaInspector {

    private static final String DEFAULT_SCHEMA = "public";

    /**
     * Returns all base tables in the specified schema.
     */
    public List<String> getTables(Connection connection)
            throws SQLException {

        List<String> tables = new ArrayList<>();

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getTables(
                connection.getCatalog(),
                DEFAULT_SCHEMA,
                "%",
                new String[]{"TABLE"})) {

            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
            }
        }

        return tables;
    }

    /**
     * Returns column metadata for a specific table.
     */
    public List<ColumnMetadata> getColumns(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<ColumnMetadata> columns = new ArrayList<>();

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getColumns(
                connection.getCatalog(),
                DEFAULT_SCHEMA,
                tableName,
                "%")) {

            while (resultSet.next()) {

                String columnName =
                        resultSet.getString("COLUMN_NAME");

                String dataType =
                        resultSet.getString("TYPE_NAME");

                int columnSize =
                        resultSet.getInt("COLUMN_SIZE");

                boolean nullable =
                        resultSet.getInt("NULLABLE")
                                == DatabaseMetaData.columnNullable;

                String defaultValue =
                        resultSet.getString("COLUMN_DEF");

                columns.add(new ColumnMetadata(
                        columnName,
                        dataType,
                        columnSize,
                        nullable,
                        defaultValue
                ));
            }
        }

        return columns;
    }

    /**
     * Returns primary-key column names for a table.
     */
    public List<String> getPrimaryKeys(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<String> primaryKeys = new ArrayList<>();

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getPrimaryKeys(
                connection.getCatalog(),
                DEFAULT_SCHEMA,
                tableName)) {

            while (resultSet.next()) {
                primaryKeys.add(
                        resultSet.getString("COLUMN_NAME")
                );
            }
        }

        return primaryKeys;
    }

    /**
     * Prevents invalid table names from being used.
     */
    private void validateTableName(String tableName) {

        if (tableName == null
                || !tableName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {

            throw new IllegalArgumentException(
                    "Invalid PostgreSQL table name"
            );
        }
    }

    /**
     * Represents one database column.
     */
    public record ColumnMetadata(
            String columnName,
            String dataType,
            int columnSize,
            boolean nullable,
            String defaultValue
    ) {
    }


    /**
     * Retrieves index metadata for a PostgreSQL table.
     */
    public List<IndexMetadata> getIndexes(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<IndexMetadata> indexes = new ArrayList<>();

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getIndexInfo(
                connection.getCatalog(),
                DEFAULT_SCHEMA,
                tableName,
                false,
                false)) {

            while (resultSet.next()) {

                // Ignore PostgreSQL table statistics rows.
                String indexName = resultSet.getString("INDEX_NAME");

                if (indexName == null) {
                    continue;
                }

                String columnName =
                        resultSet.getString("COLUMN_NAME");

                boolean nonUnique =
                        resultSet.getBoolean("NON_UNIQUE");

                short ordinalPosition =
                        resultSet.getShort("ORDINAL_POSITION");

                short indexType =
                        resultSet.getShort("TYPE");

                indexes.add(new IndexMetadata(
                        indexName,
                        columnName,
                        !nonUnique,
                        ordinalPosition,
                        indexType
                ));
            }
        }

        indexes.sort(
                Comparator.comparing(IndexMetadata::indexName)
                        .thenComparingInt(IndexMetadata::ordinalPosition)
        );

        return indexes;
    }

    /**
     * Represents one indexed column.
     */
    public record IndexMetadata(
            String indexName,
            String columnName,
            boolean unique,
            int ordinalPosition,
            int indexType
    ) {
    }


    /**
     * Retrieves foreign-key metadata for a PostgreSQL table.
     */
    public List<ForeignKeyMetadata> getForeignKeys(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<ForeignKeyMetadata> foreignKeys = new ArrayList<>();

        DatabaseMetaData metadata = connection.getMetaData();

        try (ResultSet resultSet = metadata.getImportedKeys(
                connection.getCatalog(),
                DEFAULT_SCHEMA,
                tableName)) {

            while (resultSet.next()) {

                foreignKeys.add(new ForeignKeyMetadata(
                        resultSet.getString("FK_NAME"),
                        resultSet.getString("FKCOLUMN_NAME"),
                        resultSet.getString("PKTABLE_NAME"),
                        resultSet.getString("PKCOLUMN_NAME"),
                        resultSet.getShort("KEY_SEQ"),
                        resultSet.getShort("UPDATE_RULE"),
                        resultSet.getShort("DELETE_RULE")
                ));
            }
        }

        foreignKeys.sort(
                java.util.Comparator
                        .comparing(
                                ForeignKeyMetadata::constraintName,
                                java.util.Comparator.nullsFirst(
                                        String::compareTo
                                )
                        )
                        .thenComparingInt(
                                ForeignKeyMetadata::sequence
                        )
        );

        return foreignKeys;
    }

    /**
     * Represents one foreign-key column mapping.
     */
    public record ForeignKeyMetadata(
            String constraintName,
            String columnName,
            String referencedTable,
            String referencedColumn,
            int sequence,
            int updateRule,
            int deleteRule
    ) {
    }

    /**
     * Retrieves unique constraints from a PostgreSQL table.
     */
    public List<UniqueConstraintMetadata> getUniqueConstraints(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<UniqueConstraintMetadata> constraints = new ArrayList<>();

        String sql = """
            SELECT
                constraint_info.conname,
                pg_get_constraintdef(constraint_info.oid) AS definition,
                constraint_info.condeferrable,
                constraint_info.condeferred,
                constraint_info.convalidated
            FROM pg_constraint constraint_info
            JOIN pg_class table_info
                ON table_info.oid = constraint_info.conrelid
            JOIN pg_namespace schema_info
                ON schema_info.oid = table_info.relnamespace
            WHERE constraint_info.contype = 'u'
              AND schema_info.nspname = ?
              AND table_info.relname = ?
            ORDER BY constraint_info.conname
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, DEFAULT_SCHEMA);
            statement.setString(2, tableName);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    constraints.add(new UniqueConstraintMetadata(
                            resultSet.getString("conname"),
                            resultSet.getString("definition"),
                            resultSet.getBoolean("condeferrable"),
                            resultSet.getBoolean("condeferred"),
                            resultSet.getBoolean("convalidated")
                    ));
                }
            }
        }

        return constraints;
    }

    /**
     * Represents a PostgreSQL unique constraint.
     */
    public record UniqueConstraintMetadata(
            String constraintName,
            String definition,
            boolean deferrable,
            boolean initiallyDeferred,
            boolean validated
    ) {
    }

    /**
     * Retrieves CHECK constraints from a PostgreSQL table.
     */
    public List<CheckConstraintMetadata> getCheckConstraints(
            Connection connection,
            String tableName) throws SQLException {

        validateTableName(tableName);

        List<CheckConstraintMetadata> constraints = new ArrayList<>();

        String sql = """
            SELECT
                constraint_info.conname,
                pg_get_constraintdef(constraint_info.oid) AS definition,
                constraint_info.condeferrable,
                constraint_info.condeferred,
                constraint_info.convalidated
            FROM pg_constraint constraint_info
            JOIN pg_class table_info
                ON table_info.oid = constraint_info.conrelid
            JOIN pg_namespace schema_info
                ON schema_info.oid = table_info.relnamespace
            WHERE constraint_info.contype = 'c'
              AND schema_info.nspname = ?
              AND table_info.relname = ?
            ORDER BY constraint_info.conname
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, DEFAULT_SCHEMA);
            statement.setString(2, tableName);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    constraints.add(new CheckConstraintMetadata(
                            resultSet.getString("conname"),
                            resultSet.getString("definition"),
                            resultSet.getBoolean("condeferrable"),
                            resultSet.getBoolean("condeferred"),
                            resultSet.getBoolean("convalidated")
                    ));
                }
            }
        }

        return constraints;
    }

    /**
     * Represents a PostgreSQL CHECK constraint.
     */
    public record CheckConstraintMetadata(
            String constraintName,
            String definition,
            boolean deferrable,
            boolean initiallyDeferred,
            boolean validated
    ) {
    }

    /**
     * Retrieves sequence metadata from the public schema.
     */
    public List<SequenceMetadata> getSequences(
            Connection connection) throws SQLException {

        List<SequenceMetadata> sequences = new ArrayList<>();

        String sql = """
            SELECT
                schemaname,
                sequencename,
                data_type,
                start_value,
                min_value,
                max_value,
                increment_by,
                cycle,
                cache_size
            FROM pg_sequences
            WHERE schemaname = ?
            ORDER BY sequencename
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, DEFAULT_SCHEMA);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    sequences.add(new SequenceMetadata(
                            resultSet.getString("sequencename"),
                            resultSet.getString("data_type"),
                            resultSet.getLong("start_value"),
                            resultSet.getLong("min_value"),
                            resultSet.getLong("max_value"),
                            resultSet.getLong("increment_by"),
                            resultSet.getBoolean("cycle"),
                            resultSet.getLong("cache_size")
                    ));
                }
            }
        }

        return sequences;
    }

    /**
     * Represents a PostgreSQL sequence.
     */
    public record SequenceMetadata(
            String sequenceName,
            String dataType,
            long startValue,
            long minValue,
            long maxValue,
            long incrementBy,
            boolean cycle,
            long cacheSize
    ) {
    }

}
