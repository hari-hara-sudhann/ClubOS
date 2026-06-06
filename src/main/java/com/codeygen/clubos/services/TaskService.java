package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.tasks.GeneralTask;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final MemberProgressService memberProgressService;

    @Transactional
    public GeneralTask createGeneralTask(GeneralTaskAssignmentDto dto) {
        log.info("Creating general task: {}", dto.getName());
        GeneralTask task = new GeneralTask();
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        task.setTaskType(TaskTypes.GENERAL);
        
        Department dept = departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        task.setDept(dept);
        
        GeneralTask saved = taskRepository.save(task);
        memberProgressService.updateStatusesForDepartment(dept.getDepartmentId());
        
        auditLogService.record(AuditActionType.ENTITY_CREATED, "TASK", saved.getTaskId(), "Created general task: " + saved.getName());
        return saved;
    }

    @Transactional
    public OwnershipBasedTask createOwnershipTask(OwnershipTaskAssignmentDto dto) {
        log.info("Creating ownership task: {}", dto.getName());
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        task.setBiddingDeadline(dto.getBiddingDeadline());
        task.setNumberOfOwners(dto.getNumberOfOwners());
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);

        Department dept = departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        task.setDept(dept);

        OwnershipBasedTask saved = ownershipTaskRepository.save(task);
        memberProgressService.updateStatusesForDepartment(dept.getDepartmentId());

        auditLogService.record(AuditActionType.ENTITY_CREATED, "TASK", saved.getTaskId(), "Created ownership task: " + saved.getName());
        return saved;
    }

    public List<Task> getAllTasks() {
        log.info("Fetching all tasks");
        List<Task> tasks = taskRepository.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK", "ALL", "Fetched all tasks");
        return tasks;
    }

    public Task getTaskById(String id) {
        log.info("Fetching task by id: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK", id, "Fetched task: " + task.getName());
        return task;
    }

    @Transactional
    public Task updateGeneralTask(String id, GeneralTaskAssignmentDto dto) {
        log.info("Updating general task {}: {}", id, dto.getName());
        GeneralTask task = (GeneralTask) taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        
        if (dto.getDeptId() != null) {
            Department dept = departmentRepository.findById(dto.getDeptId())
                    .orElseThrow(() -> new NoSuchElementException("Department not found"));
            task.setDept(dept);
        }

        Task updated = taskRepository.save(task);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "TASK", id, "Updated general task: " + updated.getName());
        return updated;
    }

    @Transactional
    public Task updateOwnershipTask(String id, OwnershipTaskAssignmentDto dto) {
        log.info("Updating ownership task {}: {}", id, dto.getName());
        OwnershipBasedTask task = ownershipTaskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));

        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        task.setBiddingDeadline(dto.getBiddingDeadline());
        task.setNumberOfOwners(dto.getNumberOfOwners());

        if (dto.getDeptId() != null) {
            Department dept = departmentRepository.findById(dto.getDeptId())
                    .orElseThrow(() -> new NoSuchElementException("Department not found"));
            task.setDept(dept);
        }

        Task updated = ownershipTaskRepository.save(task);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "TASK", id, "Updated ownership task: " + updated.getName());
        return updated;
    }

    @Transactional
    public void deleteTask(String id) {
        log.info("Deleting task: {}", id);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        taskRepository.delete(task);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "TASK", id, "Deleted task: " + task.getName());
    }
}
