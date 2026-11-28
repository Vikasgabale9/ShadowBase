
package com.shadowbase.migration.entity;

import com.shadowbase.migration.enums.MigrationStatus;
import com.shadowbase.migration.enums.SchemaInitializationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schema_migrations")
public class Migration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "migration_name", nullable = false, length = 150)
    private String migrationName;

    @Column(length = 500)
    private String description;

    @Column(name = "source_database", nullable = false, length = 100)
    private String sourceDatabase;

    @Column(name = "shadow_database", length = 100)
    private String shadowDatabase;

    @Column(name = "source_schema_version", length = 50)
    private String sourceSchemaVersion;

    @Column(name = "target_schema_version", length = 50)
    private String targetSchemaVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "migration_status", nullable = false, length = 30)
    private MigrationStatus migrationStatus;

    @Column(name = "created_by", nullable = false, length = 150)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "schema_initialization_status",
            nullable = false,
            length = 30
    )
    private SchemaInitializationStatus schemaInitializationStatus =
            SchemaInitializationStatus.NOT_STARTED;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (migrationStatus == null) {
            migrationStatus = MigrationStatus.DRAFT;
        }

        if (schemaInitializationStatus == null) {
            schemaInitializationStatus =
                    SchemaInitializationStatus.NOT_STARTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

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

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getShadowDatabase() {
        return shadowDatabase;
    }

    public void setShadowDatabase(String shadowDatabase) {
        this.shadowDatabase = shadowDatabase;
    }

    public String getSourceSchemaVersion() {
        return sourceSchemaVersion;
    }

    public void setSourceSchemaVersion(String sourceSchemaVersion) {
        this.sourceSchemaVersion = sourceSchemaVersion;
    }

    public String getTargetSchemaVersion() {
        return targetSchemaVersion;
    }

    public void setTargetSchemaVersion(String targetSchemaVersion) {
        this.targetSchemaVersion = targetSchemaVersion;
    }

    public MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public SchemaInitializationStatus getSchemaInitializationStatus() {
        return schemaInitializationStatus;
    }

    public void setSchemaInitializationStatus(
            SchemaInitializationStatus schemaInitializationStatus) {
        this.schemaInitializationStatus = schemaInitializationStatus;
    }
}
