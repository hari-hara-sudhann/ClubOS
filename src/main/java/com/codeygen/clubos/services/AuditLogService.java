package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.auditservice.AuditLogRequestDto;
import com.codeygen.clubos.entities.audit.AuditLog;
import com.codeygen.clubos.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void recordAction(AuditLogRequestDto dto) {
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
}
