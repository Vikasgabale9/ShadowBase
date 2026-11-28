
package com.shadowbase.migration.service;

import com.shadowbase.migration.dto.MigrationRequest;
import com.shadowbase.migration.dto.MigrationResponse;

import java.util.List;

public interface MigrationService {

    MigrationResponse createMigration(
            MigrationRequest request,
            String createdBy
    );

    List<MigrationResponse> getAllMigrations(String createdBy);

    MigrationResponse getMigrationById(Long id, String createdBy);

    MigrationResponse updateMigration(
            Long id,
            MigrationRequest request,
            String createdBy
    );

    void deleteMigration(Long id, String createdBy);
}
