
package com.shadowbase.migration.controller;

import com.shadowbase.migration.dto.MigrationRequest;
import com.shadowbase.migration.dto.MigrationResponse;
import com.shadowbase.migration.service.MigrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/migrations")
public class MigrationController {

    private final MigrationService migrationService;

    public MigrationController(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    // Create a new migration draft
    @PostMapping
    public ResponseEntity<MigrationResponse> createMigration(
            @Valid @RequestBody MigrationRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();

        MigrationResponse response =
                migrationService.createMigration(request, createdBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all migrations belonging to the logged-in user
    @GetMapping
    public ResponseEntity<List<MigrationResponse>> getAllMigrations(
            Authentication authentication) {

        String createdBy = authentication.getName();

        return ResponseEntity.ok(
                migrationService.getAllMigrations(createdBy));
    }

    // Get a migration by ID
    @GetMapping("/{id}")
    public ResponseEntity<MigrationResponse> getMigrationById(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy = authentication.getName();

        return ResponseEntity.ok(
                migrationService.getMigrationById(id, createdBy));
    }

    // Update an existing draft
    @PutMapping("/{id}")
    public ResponseEntity<MigrationResponse> updateMigration(
            @PathVariable Long id,
            @Valid @RequestBody MigrationRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();

        return ResponseEntity.ok(
                migrationService.updateMigration(id, request, createdBy));
    }

    // Delete an existing draft
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMigration(
            @PathVariable Long id,
            Authentication authentication) {

        String createdBy = authentication.getName();

        migrationService.deleteMigration(id, createdBy);

        return ResponseEntity.noContent().build();
    }
}
