package com.codeygen.clubos.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.codeygen.clubos.entities.audit.AuditLog;
import com.codeygen.clubos.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(
        name = "Audit Log Administration (Internal)",
        description = "Endpoints for inspecting system audit logs. WARNING: These endpoints expose raw AuditLog entities."
)
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Operation(
            summary = "Fetch all audit logs",
            description = "WARNING: Exposes raw AuditLog entities."
    )
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @Operation(
            summary = "Fetch audit log by ID",
            description = "WARNING: Exposes raw AuditLog entity."
    )
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(
            @Parameter(description = "ID of the audit log to fetch") @PathVariable String id
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogById(id));
    }
}
