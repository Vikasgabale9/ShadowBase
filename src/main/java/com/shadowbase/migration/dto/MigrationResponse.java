
package com.shadowbase.migration.dto;

import com.shadowbase.migration.enums.MigrationStatus;
import java.time.LocalDateTime;

public class MigrationResponse {

    private Long id;
    private String migrationName;
    private String description;
    private String sourceDatabase;
    private String shadowDatabase;
    private String sourceSchemaVersion;
    private String targetSchemaVersion;
    private MigrationStatus migrationStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MigrationResponse() {
    }

    public MigrationResponse(
            Long id,
            String migrationName,
            String description,
            String sourceDatabase,
            String shadowDatabase,
            String sourceSchemaVersion,
            String targetSchemaVersion,
            MigrationStatus migrationStatus,
            String createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.migrationName = migrationName;
        this.description = description;
        this.sourceDatabase = sourceDatabase;
        this.shadowDatabase = shadowDatabase;
        this.sourceSchemaVersion = sourceSchemaVersion;
        this.targetSchemaVersion = targetSchemaVersion;
        this.migrationStatus = migrationStatus;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getMigrationName() {
        return migrationName;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public String getShadowDatabase() {
        return shadowDatabase;
    }

    public String getSourceSchemaVersion() {
        return sourceSchemaVersion;
    }

    public String getTargetSchemaVersion() {
        return targetSchemaVersion;
    }

    public MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
