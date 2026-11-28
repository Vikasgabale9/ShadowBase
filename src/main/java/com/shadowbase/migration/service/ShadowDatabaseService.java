
package com.shadowbase.migration.service;

import com.shadowbase.migration.entity.Migration;
import com.shadowbase.migration.repository.MigrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShadowDatabaseService {

    private final MigrationRepository migrationRepository;
    private final ShadowDatabaseManager shadowDatabaseManager;

    public ShadowDatabaseService(
            MigrationRepository migrationRepository,
            ShadowDatabaseManager shadowDatabaseManager) {

        this.migrationRepository = migrationRepository;
        this.shadowDatabaseManager = shadowDatabaseManager;
    }

    /**
     * Creates and associates a shadow database with a migration.
     */
    @Transactional
    public Migration createShadowDatabase(
            Long migrationId,
            String createdBy) {

        Migration migration = getOwnedMigration(
                migrationId,
                createdBy
        );

        if (migration.getShadowDatabase() != null
                && !migration.getShadowDatabase().isBlank()) {

            throw new IllegalStateException(
                    "A shadow database is already assigned to this migration"
            );
        }

        if (migration.getMigrationStatus()
                != com.shadowbase.migration.enums.MigrationStatus.DRAFT) {

            throw new IllegalStateException(
                    "Shadow database can only be created for DRAFT migrations"
            );
        }

        String databaseName =
                shadowDatabaseManager.createShadowDatabase(migrationId);

        try {
            migration.setShadowDatabase(databaseName);

            return migrationRepository.save(migration);

        } catch (RuntimeException exception) {

            // Compensate if metadata persistence fails.
            try {
                shadowDatabaseManager.deleteShadowDatabase(migrationId);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    /**
     * Checks whether the migration has an existing shadow database.
     */
    @Transactional(readOnly = true)
    public boolean shadowDatabaseExists(
            Long migrationId,
            String createdBy) {

        Migration migration = getOwnedMigration(
                migrationId,
                createdBy
        );

        if (migration.getShadowDatabase() == null
                || migration.getShadowDatabase().isBlank()) {
            return false;
        }

        return shadowDatabaseManager.shadowDatabaseExists(migrationId);
    }

    /**
     * Finds a migration and verifies its ownership.
     */
    private Migration getOwnedMigration(
            Long migrationId,
            String createdBy) {

        Migration migration = migrationRepository.findById(migrationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Migration not found"
                ));

        if (!migration.getCreatedBy().equals(createdBy)) {
            throw new IllegalArgumentException(
                    "Migration not found"
            );
        }

        return migration;
    }
}
