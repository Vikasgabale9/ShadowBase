
package com.shadowbase.migration.controller;

import com.shadowbase.migration.entity.Migration;
import com.shadowbase.migration.service.ShadowDatabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/migrations")
public class ShadowDatabaseController {

    private final ShadowDatabaseService shadowDatabaseService;

    public ShadowDatabaseController(
            ShadowDatabaseService shadowDatabaseService) {
        this.shadowDatabaseService = shadowDatabaseService;
    }

    /**
     * Creates a shadow database for a migration.
     */
    @PostMapping("/{migrationId}/shadow-database")
    public ResponseEntity<Map<String, Object>> createShadowDatabase(
            @PathVariable Long migrationId,
            Authentication authentication) {

        Migration migration =
                shadowDatabaseService.createShadowDatabase(
                        migrationId,
                        authentication.getName()
                );

        Map<String, Object> response = Map.of(
                "migrationId", migration.getId(),
                "shadowDatabase", migration.getShadowDatabase(),
                "migrationStatus", migration.getMigrationStatus(),
                "message", "Shadow database created successfully"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Checks whether a shadow database exists.
     */
    @GetMapping("/{migrationId}/shadow-database")
    public ResponseEntity<Map<String, Object>> checkShadowDatabase(
            @PathVariable Long migrationId,
            Authentication authentication) {

        boolean exists =
                shadowDatabaseService.shadowDatabaseExists(
                        migrationId,
                        authentication.getName()
                );

        return ResponseEntity.ok(Map.of(
                "migrationId", migrationId,
                "exists", exists
        ));
    }
}
