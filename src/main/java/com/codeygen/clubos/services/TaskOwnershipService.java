package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.TaskOwnershipDto;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.task.TaskOwnershipRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskOwnershipService {

    private final TaskOwnershipRepository taskOwnershipRepository;
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public TaskOwnership createOwnership(TaskOwnershipDto dto) {
        log.info("Creating ownership for member {} on task {}", dto.getOwnerId(), dto.getTaskId());
        OwnershipBasedTask task = ownershipTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        Member member = memberRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        TaskOwnership ownership = new TaskOwnership();
        ownership.setTask(task);
        ownership.setOwner(member);
        ownership.setAssignedPoints(dto.getAssignedPoints());
        ownership.setAcquisitionType(dto.getAcquisitionType());
        ownership.setOwnershipAssignedAt(LocalDateTime.now());

        TaskOwnership saved = taskOwnershipRepository.save(ownership);
        auditLogService.record(AuditActionType.ENTITY_CREATED, "TASK_OWNERSHIP", saved.getTaskOwnershipId(), "Created ownership for task: " + task.getTaskId());
        return saved;
    }

    public List<TaskOwnership> getAllOwnerships() {
        log.info("Fetching all ownerships");
        List<TaskOwnership> ownerships = taskOwnershipRepository.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK_OWNERSHIP", "ALL", "Fetched all ownerships");
        return ownerships;
    }

    public TaskOwnership getOwnershipById(String id) {
        log.info("Fetching ownership by id: {}", id);
        TaskOwnership ownership = taskOwnershipRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ownership not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK_OWNERSHIP", id, "Fetched ownership: " + id);
        return ownership;
    }

    @Transactional
    public TaskOwnership updateOwnership(String id, TaskOwnershipDto dto) {
        log.info("Updating ownership {}: points {}", id, dto.getAssignedPoints());
        TaskOwnership ownership = taskOwnershipRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ownership not found"));
        
        if (dto.getAssignedPoints() != null) {
            ownership.setAssignedPoints(dto.getAssignedPoints());
        }
        if (dto.getAcquisitionType() != null) {
            ownership.setAcquisitionType(dto.getAcquisitionType());
        }

        TaskOwnership updated = taskOwnershipRepository.save(ownership);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "TASK_OWNERSHIP", id, "Updated ownership: " + id);
        return updated;
    }

    @Transactional
    public void deleteOwnership(String id) {
        log.info("Deleting ownership: {}", id);
        TaskOwnership ownership = taskOwnershipRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ownership not found"));
        taskOwnershipRepository.delete(ownership);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "TASK_OWNERSHIP", id, "Deleted ownership: " + id);
    }
}
