package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.memberservice.TaskSubmissionDto;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.task.TaskRepository;
import com.codeygen.clubos.repositories.task.TaskSubmissionRepository;
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
public class TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public TaskSubmission createSubmission(TaskSubmissionDto dto) {
        log.info("Creating submission for member {} on task {}", dto.getMemberId(), dto.getTaskId());
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setMember(member);
        submission.setProofOfSubmission(dto.getProofOfSubmission());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        TaskSubmission saved = taskSubmissionRepository.save(submission);
        auditLogService.record(AuditActionType.ENTITY_CREATED, "TASK_SUBMISSION", saved.getSubmissionId(), "Created submission for task: " + task.getTaskId());
        return saved;
    }

    public List<TaskSubmission> getAllSubmissions() {
        log.info("Fetching all submissions");
        List<TaskSubmission> submissions = taskSubmissionRepository.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK_SUBMISSION", "ALL", "Fetched all submissions");
        return submissions;
    }

    public TaskSubmission getSubmissionById(String id) {
        log.info("Fetching submission by id: {}", id);
        TaskSubmission submission = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Submission not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "TASK_SUBMISSION", id, "Fetched submission: " + id);
        return submission;
    }

    @Transactional
    public TaskSubmission updateSubmission(String id, TaskSubmissionDto dto) {
        log.info("Updating submission {}: proof {}", id, dto.getProofOfSubmission());
        TaskSubmission submission = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Submission not found"));
        
        if (dto.getProofOfSubmission() != null) {
            submission.setProofOfSubmission(dto.getProofOfSubmission());
        }
        submission.setSubmittedAt(LocalDateTime.now());

        TaskSubmission updated = taskSubmissionRepository.save(submission);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "TASK_SUBMISSION", id, "Updated submission: " + id);
        return updated;
    }

    @Transactional
    public void deleteSubmission(String id) {
        log.info("Deleting submission: {}", id);
        TaskSubmission submission = taskSubmissionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Submission not found"));
        taskSubmissionRepository.delete(submission);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "TASK_SUBMISSION", id, "Deleted submission: " + id);
    }
}
