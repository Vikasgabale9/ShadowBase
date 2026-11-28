
package com.shadowbase.migration.dto;

import java.util.List;

public class SchemaComparisonResponse {

    private Long migrationId;
    private boolean identical;

    private List<String> missingTables;
    private List<String> extraTables;

    private List<ColumnDifferenceResponse> columnDifferences;

    private String summary;

    public SchemaComparisonResponse() {
    }

    public SchemaComparisonResponse(
            Long migrationId,
            boolean identical,
            List<String> missingTables,
            List<String> extraTables,
            List<ColumnDifferenceResponse> columnDifferences,
            String summary) {

        this.migrationId = migrationId;
        this.identical = identical;
        this.missingTables = missingTables;
        this.extraTables = extraTables;
        this.columnDifferences = columnDifferences;
        this.summary = summary;
    }

    public Long getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(Long migrationId) {
        this.migrationId = migrationId;
    }

    public boolean isIdentical() {
        return identical;
    }

    public void setIdentical(boolean identical) {
        this.identical = identical;
    }

    public List<String> getMissingTables() {
        return missingTables;
    }

    public void setMissingTables(List<String> missingTables) {
        this.missingTables = missingTables;
    }

    public List<String> getExtraTables() {
        return extraTables;
    }

    public void setExtraTables(List<String> extraTables) {
        this.extraTables = extraTables;
    }

    public List<ColumnDifferenceResponse> getColumnDifferences() {
        return columnDifferences;
    }

    public void setColumnDifferences(
            List<ColumnDifferenceResponse> columnDifferences) {
        this.columnDifferences = columnDifferences;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public static class ColumnDifferenceResponse {

        private String tableName;
        private String columnName;
        private String property;
        private String sourceValue;
        private String shadowValue;

        public ColumnDifferenceResponse() {
        }

        public ColumnDifferenceResponse(
                String tableName,
                String columnName,
                String property,
                String sourceValue,
                String shadowValue) {

            this.tableName = tableName;
            this.columnName = columnName;
            this.property = property;
            this.sourceValue = sourceValue;
            this.shadowValue = shadowValue;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getSourceValue() {
            return sourceValue;
        }

        public void setSourceValue(String sourceValue) {
            this.sourceValue = sourceValue;
        }

        public String getShadowValue() {
            return shadowValue;
        }

        public void setShadowValue(String shadowValue) {
            this.shadowValue = shadowValue;
        }
    }
}
