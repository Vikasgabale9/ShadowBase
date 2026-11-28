
package com.shadowbase.migration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MigrationRequest {

    @NotBlank(message = "Migration name is required")
    @Size(max = 150, message = "Migration name cannot exceed 150 characters")
    private String migrationName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Size(max = 50, message = "Target schema version cannot exceed 50 characters")
    private String targetSchemaVersion;

    public String getMigrationName() {
        return migrationName;
    }

    public void setMigrationName(String migrationName) {
        this.migrationName = migrationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetSchemaVersion() {
        return targetSchemaVersion;
    }

    public void setTargetSchemaVersion(String targetSchemaVersion) {
        this.targetSchemaVersion = targetSchemaVersion;
    }
}
