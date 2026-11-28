
package com.shadowbase.migration.controller;

import com.shadowbase.migration.dto.SchemaComparisonResponse;
import com.shadowbase.migration.service.MigrationService;
import com.shadowbase.migration.service.SchemaComparisonMapper;
import com.shadowbase.migration.service.SchemaComparisonService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/migrations")
public class SchemaComparisonController {

    private final SchemaComparisonService comparisonService;
    private final SchemaComparisonMapper comparisonMapper;
    private final MigrationService migrationService;

    public SchemaComparisonController(
            SchemaComparisonService comparisonService,
            SchemaComparisonMapper comparisonMapper,
            MigrationService migrationService) {

        this.comparisonService = comparisonService;
        this.comparisonMapper = comparisonMapper;
        this.migrationService = migrationService;
    }

    /**
     * Compares source and shadow database schemas.
     */
    @GetMapping("/{migrationId}/compare")
    public ResponseEntity<SchemaComparisonResponse> compareSchema(
            @PathVariable Long migrationId,
            Authentication authentication) throws SQLException {

        if (migrationId == null || migrationId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String createdBy = authentication.getName();

        // Verify migration ownership before accessing databases.
        migrationService.getMigrationById(
                migrationId,
                createdBy
        );

        SchemaComparisonService.SchemaComparisonResult result =
                comparisonService.compare(migrationId);

        SchemaComparisonResponse response =
                comparisonMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
