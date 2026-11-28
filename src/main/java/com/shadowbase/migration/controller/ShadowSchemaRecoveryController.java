
package com.shadowbase.migration.controller;

import com.shadowbase.migration.service.ShadowSchemaInitializationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/migrations")
public class ShadowSchemaRecoveryController {

    private final ShadowSchemaInitializationService initializationService;

    public ShadowSchemaRecoveryController(
            ShadowSchemaInitializationService initializationService) {
        this.initializationService = initializationService;
    }

    @PostMapping("/{migrationId}/recover-shadow-schema")
    public ResponseEntity<Map<String, Object>> recoverShadowSchema(
            @PathVariable Long migrationId,
            Authentication authentication) {

        String username = authentication.getName();

        initializationService.recoverFailedInitialization(
                migrationId,
                username
        );

        return ResponseEntity.ok(Map.of(
                "message", "Shadow database recovery completed successfully",
                "migrationId", migrationId,
                "status", "NOT_STARTED"
        ));
    }
}
