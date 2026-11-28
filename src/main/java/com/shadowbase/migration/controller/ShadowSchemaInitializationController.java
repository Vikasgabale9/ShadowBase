
package com.shadowbase.migration.controller;

import com.shadowbase.migration.service.ShadowSchemaInitializationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/migrations")
public class ShadowSchemaInitializationController {

    private final ShadowSchemaInitializationService initializationService;

    public ShadowSchemaInitializationController(
            ShadowSchemaInitializationService initializationService
    ) {
        this.initializationService = initializationService;
    }

    @PostMapping("/{migrationId}/initialize-shadow-schema")
    public ResponseEntity<Map<String, String>> initializeShadowSchema(
            @PathVariable Long migrationId,
            Authentication authentication
    ) {

        initializationService.initializeShadowSchema(
                migrationId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Shadow schema initialized successfully",
                        "migrationId",
                        migrationId.toString()
                )
        );
    }
}
