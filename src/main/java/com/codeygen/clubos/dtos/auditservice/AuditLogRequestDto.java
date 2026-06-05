package com.codeygen.clubos.dtos.auditservice;

import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import lombok.Data;

@Data
public class AuditLogRequestDto {
    private AuditActionType actionType;
    private String actorUserId;
    private String actorName;
    private String actorRole;
    private String departmentId;
    private String targetId;
    private String targetType;
    private String summary;
    private String details;
}
