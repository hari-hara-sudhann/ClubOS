package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GetSubmissionsDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskDirectAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.SubmissionsDto;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.tasks.GeneralTask;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAcquisitionType;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.BidRepository;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.task.TaskOwnershipRepository;
import com.codeygen.clubos.repositories.task.TaskRepository;
import com.codeygen.clubos.repositories.task.TaskSubmissionRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final TaskRepository taskRepository;
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final DepartmentRepository departmentRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskOwnershipRepository taskOwnershipRepository;
    private final MemberRepository memberRepository;
    private final BidRepository bidRepository;
    private final MemberProgressService memberProgressService;

    @Transactional
    public void assignGeneralTask(@NonNull GeneralTaskAssignmentDto dto) {
        validateTaskPoints(dto.getPoints());

        GeneralTask task = new GeneralTask();
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        task.setTaskType(TaskTypes.GENERAL);
        Department dept = departmentRepository.findById(dto.getDeptId())
                        .orElseThrow(() -> new NoSuchElementException("Department Does not exist!"));
        task.setDept(dept);
        taskRepository.save(task);
        memberProgressService.updateStatusesForDepartment(dept.getDepartmentId());
    }

    @Transactional
    public void assignOwnershipBasedTask(@NonNull OwnershipTaskAssignmentDto dto) {
        validateTaskPoints(dto.getPoints());

        if (dto.getNumberOfOwners() == null || dto.getNumberOfOwners() <= 0) {
            throw new IllegalArgumentException("Ownership task must have at least one owner.");
        }

        if (dto.getBiddingDeadline() == null || !dto.getBiddingDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Bidding deadline must be in the future.");
        }

        if (dto.getDeadline() != null && dto.getDeadline().isBefore(dto.getBiddingDeadline())) {
            throw new IllegalArgumentException("Task deadline cannot be before the bidding deadline.");
        }

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        task.setBiddingDeadline(dto.getBiddingDeadline());
        task.setNumberOfOwners(dto.getNumberOfOwners());
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);

        Department dept = departmentRepository.findById(dto.getDeptId())
                .orElseThrow(() -> new NoSuchElementException("Department Does not exist!"));
        task.setDept(dept);

        ownershipTaskRepository.save(task);
        memberProgressService.updateStatusesForDepartment(dept.getDepartmentId());
    }

    @Transactional
    public void assignOwnersDirectly(@NonNull OwnershipTaskDirectAssignmentDto dto) {
        OwnershipBasedTask task = ownershipTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Ownership task not found!"));

        if (task.getAssignmentStatus() != OwnershipAssignmentStatus.BIDDING_OPEN) {
            throw new IllegalStateException("Owners have already been assigned for this task.");
        }

        if (!task.getBiddingDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Bidding deadline is already over.");
        }

        List<String> ownerIds = dto.getOwnerIds();
        if (ownerIds == null || ownerIds.size() != task.getNumberOfOwners()) {
            throw new IllegalArgumentException("Lead must assign exactly " + task.getNumberOfOwners() + " owners.");
        }

        Set<String> uniqueOwnerIds = new HashSet<>(ownerIds);
        if (uniqueOwnerIds.size() != ownerIds.size()) {
            throw new IllegalArgumentException("Duplicate owners are not allowed.");
        }

        if (task.getPoints() == null || task.getPoints() <= 0) {
            throw new IllegalStateException("Ownership task must have positive points.");
        }

        int reducedPoints = (int) Math.ceil(task.getPoints() * 0.60);
        List<TaskOwnership> ownerships = new ArrayList<>();

        for (String ownerId : ownerIds) {
            Member owner = memberRepository.findById(ownerId)
                    .orElseThrow(() -> new NoSuchElementException("Member not found: " + ownerId));

            if (owner.getDept() == null || !owner.getDept().getDepartmentId().equals(task.getDept().getDepartmentId())) {
                throw new IllegalArgumentException("Owner must belong to the same department as the task.");
            }

            TaskOwnership ownership = new TaskOwnership();
            ownership.setTask(task);
            ownership.setOwner(owner);
            ownership.setAssignedPoints(reducedPoints);
            ownership.setOwnershipAssignedAt(LocalDateTime.now());
            ownership.setAcquisitionType(OwnershipAcquisitionType.LEAD_ASSIGNMENT);
            ownerships.add(ownership);
        }

        List<Bid> bids = bidRepository.findByTask_TaskId(task.getTaskId());
        for (Bid bid : bids) {
            bid.setStatus(BidStatus.LOST);
        }

        task.setPoints(reducedPoints);
        task.setAssignmentStatus(OwnershipAssignmentStatus.ASSIGNED_BY_LEAD);

        taskOwnershipRepository.saveAll(ownerships);
        bidRepository.saveAll(bids);
        ownershipTaskRepository.save(task);
        memberProgressService.updateStatusesForDepartment(task.getDept().getDepartmentId());
    }

    public GetSubmissionsDto getSubmissions(String taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new NoSuchElementException("Task not found!"));
        List<TaskSubmission> submissions = taskSubmissionRepository.findAllByTask_TaskId(taskId);
        List<SubmissionsDto> dtos = new ArrayList<>();

        for (TaskSubmission submission: submissions) {
            SubmissionsDto submissionsDto = new SubmissionsDto();
            submissionsDto.setMemberId(submission.getMember().getUserId());
            submissionsDto.setProofOfSubmission(submission.getProofOfSubmission());
            dtos.add(submissionsDto);
        }
        GetSubmissionsDto dto = new GetSubmissionsDto();
        dto.setSubmissions(dtos);
        dto.setTaskId(taskId);
        return dto;
    }

    @Transactional
    public void reviewTask(String taskId, String userId, SubmissionStatus status, String remarks)
    throws IllegalStateException {
        if (status == null || status == SubmissionStatus.SUBMITTED) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED.");
        }

        TaskSubmission submission = taskSubmissionRepository.findByTask_TaskIdAndMember_UserId(taskId, userId)
                .orElseThrow(() -> new NoSuchElementException("submission not found!"));

        validateOwnershipSubmissionIfNeeded(submission.getTask(), userId, status);
        submission.setStatus(status);
        submission.setRemarksByLead(remarks);
        taskSubmissionRepository.save(submission);
        memberProgressService.refreshMemberProgress(userId);
    }

    private void validateOwnershipSubmissionIfNeeded(Task task, String userId, SubmissionStatus status) {
        if (status != SubmissionStatus.APPROVED) {
            return;
        }

        if (task.getTaskType() == TaskTypes.OWNERSHIP_BASED || task instanceof OwnershipBasedTask) {
            taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId(task.getTaskId(), userId)
                    .orElseThrow(() -> new IllegalStateException("Submission member is not an assigned owner."));
        }
    }

    private void validateTaskPoints(Integer points) {
        if (points == null || points <= 0) {
            throw new IllegalArgumentException("Task points must be positive.");
        }
    }
}
