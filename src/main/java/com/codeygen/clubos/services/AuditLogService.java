package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.auditservice.AuditLogRequestDto;
import com.codeygen.clubos.entities.audit.AuditLog;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void recordAction(AuditLogRequestDto dto) {
        log.info("Recording audit log: action={}, targetType={}, targetId={}", 
                dto.getActionType(), dto.getTargetType(), dto.getTargetId());
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(dto.getActionType());
        auditLog.setActorUserId(dto.getActorUserId());
        auditLog.setActorName(dto.getActorName());
        auditLog.setActorRole(dto.getActorRole());
        auditLog.setDepartmentId(dto.getDepartmentId());
        auditLog.setTargetId(dto.getTargetId());
        auditLog.setTargetType(dto.getTargetType());
        auditLog.setSummary(dto.getSummary());
        auditLog.setDetails(dto.getDetails());
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        log.info("Fetching all audit logs");
        return auditLogRepository.findAll();
    }

    public AuditLog getAuditLogById(String id) {
        log.info("Fetching audit log by id: {}", id);
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Audit log not found"));
    }

    public void record(AuditActionType actionType, String targetType, String targetId, String summary) {
        AuditLogRequestDto audit = new AuditLogRequestDto();
        audit.setActionType(actionType);
        audit.setActorName("SYSTEM");
        audit.setActorRole("ADMIN");
        audit.setTargetType(targetType);
        audit.setTargetId(targetId);
        audit.setSummary(summary);
        recordAction(audit);
    }
}
