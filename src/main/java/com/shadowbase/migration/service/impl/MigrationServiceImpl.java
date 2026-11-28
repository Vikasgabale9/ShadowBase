
package com.shadowbase.migration.service.impl;

import com.shadowbase.migration.dto.MigrationRequest;
import com.shadowbase.migration.dto.MigrationResponse;
import com.shadowbase.migration.entity.Migration;
import com.shadowbase.migration.enums.MigrationStatus;
import com.shadowbase.migration.repository.MigrationRepository;
import com.shadowbase.migration.service.MigrationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MigrationServiceImpl implements MigrationService {

    private final MigrationRepository migrationRepository;

    public MigrationServiceImpl(MigrationRepository migrationRepository) {
        this.migrationRepository = migrationRepository;
    }

    @Override
    public MigrationResponse createMigration(
            MigrationRequest request,
            String createdBy) {

        if (migrationRepository.existsByMigrationNameAndCreatedBy(
                request.getMigrationName(), createdBy)) {
            throw new IllegalArgumentException(
                    "A migration with this name already exists.");
        }

        Migration migration = new Migration();

        migration.setMigrationName(request.getMigrationName().trim());
        migration.setDescription(request.getDescription());
        migration.setSourceDatabase("PRIMARY");
        migration.setTargetSchemaVersion(request.getTargetSchemaVersion());
        migration.setMigrationStatus(MigrationStatus.DRAFT);
        migration.setCreatedBy(createdBy);

        Migration savedMigration = migrationRepository.save(migration);

        return mapToResponse(savedMigration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MigrationResponse> getAllMigrations(String createdBy) {

        return migrationRepository
                .findByCreatedByOrderByCreatedAtDesc(createdBy)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MigrationResponse getMigrationById(
            Long id,
            String createdBy) {

        Migration migration = getOwnedMigration(id, createdBy);

        return mapToResponse(migration);
    }

    @Override
    public MigrationResponse updateMigration(
            Long id,
            MigrationRequest request,
            String createdBy) {

        Migration migration = getOwnedMigration(id, createdBy);

        if (migration.getMigrationStatus() != MigrationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft migrations can be updated.");
        }

        boolean duplicateName =
                migrationRepository.existsByMigrationNameAndCreatedBy(
                        request.getMigrationName(), createdBy);

        if (duplicateName &&
                !migration.getMigrationName().equalsIgnoreCase(
                        request.getMigrationName().trim())) {
            throw new IllegalArgumentException(
                    "A migration with this name already exists.");
        }

        migration.setMigrationName(request.getMigrationName().trim());
        migration.setDescription(request.getDescription());
        migration.setTargetSchemaVersion(request.getTargetSchemaVersion());

        Migration updatedMigration = migrationRepository.save(migration);

        return mapToResponse(updatedMigration);
    }

    @Override
    public void deleteMigration(Long id, String createdBy) {

        Migration migration = getOwnedMigration(id, createdBy);

        if (migration.getMigrationStatus() != MigrationStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft migrations can be deleted.");
        }

        migrationRepository.delete(migration);
    }

    private Migration getOwnedMigration(Long id, String createdBy) {

        Migration migration = migrationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Migration not found with ID: " + id));

        if (!migration.getCreatedBy().equals(createdBy)) {
            throw new IllegalArgumentException(
                    "You are not authorized to access this migration.");
        }

        return migration;
    }

    private MigrationResponse mapToResponse(Migration migration) {

        return new MigrationResponse(
                migration.getId(),
                migration.getMigrationName(),
                migration.getDescription(),
                migration.getSourceDatabase(),
                migration.getShadowDatabase(),
                migration.getSourceSchemaVersion(),
                migration.getTargetSchemaVersion(),
                migration.getMigrationStatus(),
                migration.getCreatedBy(),
                migration.getCreatedAt(),
                migration.getUpdatedAt()
        );
    }
}
