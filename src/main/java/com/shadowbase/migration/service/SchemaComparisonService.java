
package com.shadowbase.migration.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Service
public class SchemaComparisonService {

    private final DataSource sourceDataSource;
    private final ShadowDatabaseConnectionFactory connectionFactory;
    private final ShadowSchemaInspector schemaInspector;

    public SchemaComparisonService(
            DataSource sourceDataSource,
            ShadowDatabaseConnectionFactory connectionFactory,
            ShadowSchemaInspector schemaInspector) {

        this.sourceDataSource = sourceDataSource;
        this.connectionFactory = connectionFactory;
        this.schemaInspector = schemaInspector;
    }

    /**
     * Compares the configured source database with
     * the shadow database belonging to a migration.
     */
    public SchemaComparisonResult compare(Long migrationId)
            throws SQLException {

        try (Connection sourceConnection =
                     sourceDataSource.getConnection();

             Connection shadowConnection =
                     connectionFactory.getConnection(migrationId)) {

            List<String> sourceTables =
                    schemaInspector.getTables(sourceConnection);

            List<String> shadowTables =
                    schemaInspector.getTables(shadowConnection);

            Set<String> sourceTableSet =
                    new HashSet<>(sourceTables);

            Set<String> shadowTableSet =
                    new HashSet<>(shadowTables);

            List<String> missingTables = new ArrayList<>();
            List<String> extraTables = new ArrayList<>();
            List<ColumnDifference> columnDifferences =
                    new ArrayList<>();

            // Tables present in source but missing in shadow.
            for (String table : sourceTables) {

                if (!shadowTableSet.contains(table)) {
                    missingTables.add(table);
                    continue;
                }

                compareTableColumns(
                        sourceConnection,
                        shadowConnection,
                        table,
                        columnDifferences
                );
            }

            // Tables present in shadow but absent in source.
            for (String table : shadowTables) {

                if (!sourceTableSet.contains(table)) {
                    extraTables.add(table);
                }
            }

            return new SchemaComparisonResult(
                    migrationId,
                    missingTables,
                    extraTables,
                    columnDifferences,
                    missingTables.isEmpty()
                            && extraTables.isEmpty()
                            && columnDifferences.isEmpty()
            );
        }
    }

    /**
     * Compares columns and primary keys of a common table.
     */

    private void compareTableColumns(
            Connection sourceConnection,
            Connection shadowConnection,
            String tableName,
            List<ColumnDifference> differences)
            throws SQLException {

        List<ShadowSchemaInspector.ColumnMetadata> sourceColumns =
                schemaInspector.getColumns(
                        sourceConnection,
                        tableName
                );

        List<ShadowSchemaInspector.ColumnMetadata> shadowColumns =
                schemaInspector.getColumns(
                        shadowConnection,
                        tableName
                );

        Map<String, ShadowSchemaInspector.ColumnMetadata> sourceColumnMap =
                toColumnMap(sourceColumns);

        Map<String, ShadowSchemaInspector.ColumnMetadata> shadowColumnMap =
                toColumnMap(shadowColumns);

        // 1. Check source columns against shadow columns.
        for (String columnName : sourceColumnMap.keySet()) {

            var sourceColumn = sourceColumnMap.get(columnName);
            var shadowColumn = shadowColumnMap.get(columnName);

            if (shadowColumn == null) {

                differences.add(new ColumnDifference(
                        tableName,
                        columnName,
                        "MISSING_COLUMN",
                        sourceColumn.toString(),
                        null
                ));

                continue;
            }

            compareColumnProperty(
                    tableName,
                    columnName,
                    "DATA_TYPE",
                    sourceColumn.dataType(),
                    shadowColumn.dataType(),
                    differences
            );

            compareColumnProperty(
                    tableName,
                    columnName,
                    "COLUMN_SIZE",
                    String.valueOf(sourceColumn.columnSize()),
                    String.valueOf(shadowColumn.columnSize()),
                    differences
            );

            compareColumnProperty(
                    tableName,
                    columnName,
                    "NULLABLE",
                    String.valueOf(sourceColumn.nullable()),
                    String.valueOf(shadowColumn.nullable()),
                    differences
            );

            compareColumnProperty(
                    tableName,
                    columnName,
                    "DEFAULT_VALUE",
                    normalizeDefault(sourceColumn.defaultValue()),
                    normalizeDefault(shadowColumn.defaultValue()),
                    differences
            );
        }

        // 2. Check for columns that exist only in shadow.
        for (String columnName : shadowColumnMap.keySet()) {

            if (!sourceColumnMap.containsKey(columnName)) {

                var shadowColumn = shadowColumnMap.get(columnName);

                differences.add(new ColumnDifference(
                        tableName,
                        columnName,
                        "EXTRA_COLUMN",
                        null,
                        shadowColumn.toString()
                ));
            }
        }

        // 3. Compare primary keys.
        List<String> sourcePrimaryKeys =
                schemaInspector.getPrimaryKeys(
                        sourceConnection,
                        tableName
                );

        List<String> shadowPrimaryKeys =
                schemaInspector.getPrimaryKeys(
                        shadowConnection,
                        tableName
                );

        if (!new HashSet<>(sourcePrimaryKeys)
                .equals(new HashSet<>(shadowPrimaryKeys))) {

            differences.add(new ColumnDifference(
                    tableName,
                    null,
                    "PRIMARY_KEY",
                    sourcePrimaryKeys.toString(),
                    shadowPrimaryKeys.toString()
            ));
        }
    }

    private Map<String, ShadowSchemaInspector.ColumnMetadata>
    toColumnMap(
            List<ShadowSchemaInspector.ColumnMetadata> columns) {

        Map<String, ShadowSchemaInspector.ColumnMetadata> map =
                new HashMap<>();

        for (var column : columns) {
            map.put(column.columnName(), column);
        }

        return map;
    }

    private void compareColumnProperty(
            String tableName,
            String columnName,
            String property,
            String sourceValue,
            String shadowValue,
            List<ColumnDifference> differences) {

        if (!java.util.Objects.equals(sourceValue, shadowValue)) {

            differences.add(new ColumnDifference(
                    tableName,
                    columnName,
                    property,
                    sourceValue,
                    shadowValue
            ));
        }
    }

    private String normalizeDefault(String value) {

        return value == null ? null : value.trim();
    }

    public record ColumnDifference(
            String tableName,
            String columnName,
            String property,
            String sourceValue,
            String shadowValue
    ) {
    }

    public record SchemaComparisonResult(
            Long migrationId,
            List<String> missingTables,
            List<String> extraTables,
            List<ColumnDifference> columnDifferences,
            boolean identical
    ) {
    }

    /**
     * Compares indexes between source and shadow tables.
     */
    private void compareTableIndexes(
            Connection sourceConnection,
            Connection shadowConnection,
            String tableName,
            List<ColumnDifference> differences) throws SQLException {

        List<String> sourceIndexes = getIndexDefinitions(
                sourceConnection,
                tableName
        );

        List<String> shadowIndexes = getIndexDefinitions(
                shadowConnection,
                tableName
        );

        for (String index : sourceIndexes) {
            if (!shadowIndexes.contains(index)) {
                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "MISSING_INDEX",
                        index,
                        null
                ));
            }
        }

        for (String index : shadowIndexes) {
            if (!sourceIndexes.contains(index)) {
                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "EXTRA_INDEX",
                        null,
                        index
                ));
            }
        }
    }

    /**
     * Builds normalized index definitions.
     * Index names are excluded because generated names can differ
     * between databases.
     */
    private List<String> getIndexDefinitions(
            Connection connection,
            String tableName) throws SQLException {

        List<ShadowSchemaInspector.IndexMetadata> indexes =
                schemaInspector.getIndexes(connection, tableName);

        Map<String, List<ShadowSchemaInspector.IndexMetadata>> grouped =
                new TreeMap<>();

        for (var index : indexes) {
            grouped.computeIfAbsent(
                    index.indexName(),
                    key -> new ArrayList<>()
            ).add(index);
        }

        List<String> definitions = new ArrayList<>();

        for (List<ShadowSchemaInspector.IndexMetadata> group
                : grouped.values()) {

            group.sort(
                    Comparator.comparingInt(
                            ShadowSchemaInspector.IndexMetadata::ordinalPosition
                    )
            );

            boolean unique = group.get(0).unique();

            String columns = group.stream()
                    .map(ShadowSchemaInspector.IndexMetadata::columnName)
                    .collect(java.util.stream.Collectors.joining(","));

            definitions.add(
                    "UNIQUE=" + unique + ";COLUMNS=" + columns
            );
        }

        definitions.sort(String::compareTo);

        return definitions;
    }


    /**
     * Compares foreign-key constraints between source and shadow tables.
     */
    private void compareTableForeignKeys(
            Connection sourceConnection,
            Connection shadowConnection,
            String tableName,
            List<ColumnDifference> differences) throws SQLException {

        List<String> sourceForeignKeys = getForeignKeyDefinitions(
                sourceConnection,
                tableName
        );

        List<String> shadowForeignKeys = getForeignKeyDefinitions(
                shadowConnection,
                tableName
        );

        // Foreign keys missing from the shadow database.
        for (String foreignKey : sourceForeignKeys) {

            if (!shadowForeignKeys.contains(foreignKey)) {

                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "MISSING_FOREIGN_KEY",
                        foreignKey,
                        null
                ));
            }
        }

        // Foreign keys existing only in the shadow database.
        for (String foreignKey : shadowForeignKeys) {

            if (!sourceForeignKeys.contains(foreignKey)) {

                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "EXTRA_FOREIGN_KEY",
                        null,
                        foreignKey
                ));
            }
        }
    }

    /**
     * Creates normalized foreign-key definitions.
     * Constraint names are excluded because names may differ
     * between source and shadow databases.
     */
    private List<String> getForeignKeyDefinitions(
            Connection connection,
            String tableName) throws SQLException {

        List<ShadowSchemaInspector.ForeignKeyMetadata> foreignKeys =
                schemaInspector.getForeignKeys(connection, tableName);

        Map<String, List<ShadowSchemaInspector.ForeignKeyMetadata>> grouped =
                new java.util.TreeMap<>();

        for (var foreignKey : foreignKeys) {

            String groupKey = foreignKey.constraintName() == null
                    ? "unnamed"
                    : foreignKey.constraintName();

            grouped.computeIfAbsent(
                    groupKey,
                    key -> new ArrayList<>()
            ).add(foreignKey);
        }

        List<String> definitions = new ArrayList<>();

        for (List<ShadowSchemaInspector.ForeignKeyMetadata> group
                : grouped.values()) {

            group.sort(
                    Comparator.comparingInt(
                            ShadowSchemaInspector.ForeignKeyMetadata::sequence
                    )
            );

            var first = group.get(0);

            String mappings = group.stream()
                    .map(fk -> fk.columnName()
                            + "->"
                            + fk.referencedTable()
                            + "."
                            + fk.referencedColumn())
                    .collect(java.util.stream.Collectors.joining(","));

            definitions.add(
                    "MAPPING=" + mappings
                            + ";UPDATE_RULE=" + first.updateRule()
                            + ";DELETE_RULE=" + first.deleteRule()
            );
        }

        definitions.sort(String::compareTo);

        return definitions;
    }

    /**
     * Compares CHECK constraints between source and shadow tables.
     */
    private void compareCheckConstraints(
            Connection sourceConnection,
            Connection shadowConnection,
            String tableName,
            List<ColumnDifference> differences) throws SQLException {

        List<String> sourceConstraints = getCheckConstraintDefinitions(
                sourceConnection,
                tableName
        );

        List<String> shadowConstraints = getCheckConstraintDefinitions(
                shadowConnection,
                tableName
        );

        for (String constraint : sourceConstraints) {

            if (!shadowConstraints.contains(constraint)) {

                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "MISSING_CHECK_CONSTRAINT",
                        constraint,
                        null
                ));
            }
        }

        for (String constraint : shadowConstraints) {

            if (!sourceConstraints.contains(constraint)) {

                differences.add(new ColumnDifference(
                        tableName,
                        null,
                        "EXTRA_CHECK_CONSTRAINT",
                        null,
                        constraint
                ));
            }
        }
    }

    /**
     * Creates normalized CHECK constraint definitions.
     */
    private List<String> getCheckConstraintDefinitions(
            Connection connection,
            String tableName) throws SQLException {

        List<ShadowSchemaInspector.CheckConstraintMetadata> constraints =
                schemaInspector.getCheckConstraints(connection, tableName);

        List<String> definitions = new ArrayList<>();

        for (var constraint : constraints) {

            String definition = constraint.definition() == null
                    ? ""
                    : constraint.definition().trim();

            definitions.add(
                    definition
                            + ";DEFERRABLE=" + constraint.deferrable()
                            + ";INITIALLY_DEFERRED="
                            + constraint.initiallyDeferred()
                            + ";VALIDATED=" + constraint.validated()
            );
        }

        definitions.sort(String::compareTo);

        return definitions;
    }

    /**
     * Compares PostgreSQL sequence definitions.
     */
    private void compareSequences(
            Connection sourceConnection,
            Connection shadowConnection,
            List<ColumnDifference> differences) throws SQLException {

        List<ShadowSchemaInspector.SequenceMetadata> sourceSequences =
                schemaInspector.getSequences(sourceConnection);

        List<ShadowSchemaInspector.SequenceMetadata> shadowSequences =
                schemaInspector.getSequences(shadowConnection);

        Map<String, ShadowSchemaInspector.SequenceMetadata> sourceMap =
                new HashMap<>();

        Map<String, ShadowSchemaInspector.SequenceMetadata> shadowMap =
                new HashMap<>();

        for (var sequence : sourceSequences) {
            sourceMap.put(sequence.sequenceName(), sequence);
        }

        for (var sequence : shadowSequences) {
            shadowMap.put(sequence.sequenceName(), sequence);
        }

        // Detect missing sequences and changed definitions.
        for (var entry : sourceMap.entrySet()) {

            String name = entry.getKey();
            var source = entry.getValue();
            var shadow = shadowMap.get(name);

            if (shadow == null) {
                differences.add(new ColumnDifference(
                        null,
                        name,
                        "MISSING_SEQUENCE",
                        source.toString(),
                        null
                ));
                continue;
            }

            if (!sameSequenceDefinition(source, shadow)) {
                differences.add(new ColumnDifference(
                        null,
                        name,
                        "DIFFERENT_SEQUENCE",
                        source.toString(),
                        shadow.toString()
                ));
            }
        }

        // Detect sequences existing only in shadow.
        for (var entry : shadowMap.entrySet()) {

            if (!sourceMap.containsKey(entry.getKey())) {

                differences.add(new ColumnDifference(
                        null,
                        entry.getKey(),
                        "EXTRA_SEQUENCE",
                        null,
                        entry.getValue().toString()
                ));
            }
        }
    }

    /**
     * Compares sequence configuration, not current sequence position.
     */
    private boolean sameSequenceDefinition(
            ShadowSchemaInspector.SequenceMetadata source,
            ShadowSchemaInspector.SequenceMetadata shadow) {

        return java.util.Objects.equals(
                source.dataType(), shadow.dataType())
                && source.startValue() == shadow.startValue()
                && source.minValue() == shadow.minValue()
                && source.maxValue() == shadow.maxValue()
                && source.incrementBy() == shadow.incrementBy()
                && source.cycle() == shadow.cycle()
                && source.cacheSize() == shadow.cacheSize();
    }

}
