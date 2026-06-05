package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.auditservice.AuditLogRequestDto;
import com.codeygen.clubos.entities.audit.AuditLog;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.repositories.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void shouldPersistAuditLogFromRequestDto() {
        AuditLogRequestDto dto = new AuditLogRequestDto();
        dto.setActionType(AuditActionType.GENERAL_TASK_ASSIGNED);
        dto.setActorUserId("lead-1");
        dto.setActorName("Lead Caller");
        dto.setActorRole("LEAD");
        dto.setDepartmentId("dept-1");
        dto.setTargetId("task-1");
        dto.setTargetType("TASK");
        dto.setSummary("Lead assigned task.");
        dto.setDetails("Points: 10");

        auditLogService.recordAction(dto);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(AuditActionType.GENERAL_TASK_ASSIGNED, saved.getActionType());
        assertEquals("lead-1", saved.getActorUserId());
        assertEquals("Lead Caller", saved.getActorName());
        assertEquals("LEAD", saved.getActorRole());
        assertEquals("dept-1", saved.getDepartmentId());
        assertEquals("task-1", saved.getTargetId());
        assertEquals("TASK", saved.getTargetType());
        assertEquals("Lead assigned task.", saved.getSummary());
        assertEquals("Points: 10", saved.getDetails());
        assertNotNull(saved.getOccurredAt());
    }
}
