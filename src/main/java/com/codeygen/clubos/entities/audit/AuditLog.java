package com.codeygen.clubos.entities.audit;

import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String auditLogId;

    @Enumerated(EnumType.STRING)
    private AuditActionType actionType;

    private String actorUserId;

    private String actorName;

    private String actorRole;

    private String departmentId;

    private String targetId;

    private String targetType;

    @Column(length = 500)
    private String summary;

    @Column(length = 4000)
    private String details;

    private LocalDateTime occurredAt;
}
