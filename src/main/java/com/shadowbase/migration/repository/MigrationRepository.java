
package com.shadowbase.migration.repository;

import com.shadowbase.migration.entity.Migration;
import com.shadowbase.migration.enums.MigrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MigrationRepository extends JpaRepository<Migration, Long> {

    List<Migration> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<Migration> findByMigrationStatus(MigrationStatus migrationStatus);

    boolean existsByMigrationNameAndCreatedBy(
            String migrationName,
            String createdBy
    );
}
