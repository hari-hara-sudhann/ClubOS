package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskDirectAssignmentDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OwnershipTaskRepository ownershipTaskRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private TaskSubmissionRepository taskSubmissionRepository;

    @Mock
    private TaskOwnershipRepository taskOwnershipRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private MemberProgressService memberProgressService;

    @InjectMocks
    private LeadService leadService;

    @Test
    void shouldAssignOwnershipTaskWithBiddingDeadlineAndOwnerCount() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        when(departmentRepository.findById("dept-1")).thenReturn(Optional.of(department));

        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setName("Build Club Site");
        dto.setPoints(100);
        dto.setDeptId("dept-1");
        dto.setDeadline(LocalDateTime.now().plusDays(3));
        dto.setBiddingDeadline(LocalDateTime.now().plusDays(1));
        dto.setNumberOfOwners(2);

        leadService.assignOwnershipBasedTask(dto);

        ArgumentCaptor<OwnershipBasedTask> taskCaptor = ArgumentCaptor.forClass(OwnershipBasedTask.class);
        verify(ownershipTaskRepository).save(taskCaptor.capture());

        OwnershipBasedTask savedTask = taskCaptor.getValue();
        assertEquals("Build Club Site", savedTask.getName());
        assertEquals(100, savedTask.getPoints());
        assertEquals(2, savedTask.getNumberOfOwners());
        assertEquals(TaskTypes.OWNERSHIP_BASED, savedTask.getTaskType());
        assertEquals(OwnershipAssignmentStatus.BIDDING_OPEN, savedTask.getAssignmentStatus());
        assertEquals(department, savedTask.getDept());

        verify(memberProgressService).updateStatusesForDepartment("dept-1");
    }

    @Test
    void shouldDirectlyAssignOwnersAndReducePointsByFortyPercent() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setDept(department);
        task.setPoints(100);
        task.setNumberOfOwners(2);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(2));
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);

        Member ownerOne = new Member();
        ownerOne.setUserId("member-1");
        ownerOne.setDept(department);

        Member ownerTwo = new Member();
        ownerTwo.setUserId("member-2");
        ownerTwo.setDept(department);

        Bid bid = new Bid();
        bid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(ownerOne));
        when(memberRepository.findById("member-2")).thenReturn(Optional.of(ownerTwo));
        when(bidRepository.findByTask_TaskId("task-1")).thenReturn(List.of(bid));

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1", "member-2"));

        leadService.assignOwnersDirectly(dto);

        ArgumentCaptor<List<TaskOwnership>> ownershipCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskOwnershipRepository).saveAll(ownershipCaptor.capture());

        List<TaskOwnership> savedOwnerships = ownershipCaptor.getValue();
        assertEquals(2, savedOwnerships.size());
        assertTrue(savedOwnerships.stream().allMatch(ownership -> ownership.getAssignedPoints() == 60));
        assertTrue(savedOwnerships.stream().allMatch(
                ownership -> ownership.getAcquisitionType() == OwnershipAcquisitionType.LEAD_ASSIGNMENT
        ));

        assertEquals(60, task.getPoints());
        assertEquals(OwnershipAssignmentStatus.ASSIGNED_BY_LEAD, task.getAssignmentStatus());
        assertEquals(BidStatus.LOST, bid.getStatus());

        verify(memberProgressService).updateStatusesForDepartment("dept-1");
    }

    @Test
    void shouldReviewTaskWithApprovedStatus() {
        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);
        task.setPoints(50);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setStatus(SubmissionStatus.SUBMITTED);

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));

        leadService.reviewTask("task-1", "member-1", SubmissionStatus.APPROVED, "Good work");

        assertEquals(SubmissionStatus.APPROVED, submission.getStatus());
        assertEquals("Good work", submission.getRemarksByLead());
        verify(memberProgressService).refreshMemberProgress("member-1");
    }

    @Test
    void shouldAllowCorrectingSubmissionStatusAfterMistake() {
        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setStatus(SubmissionStatus.APPROVED);

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));

        leadService.reviewTask("task-1", "member-1", SubmissionStatus.REJECTED, "Correction");

        assertEquals(SubmissionStatus.REJECTED, submission.getStatus());
        assertEquals("Correction", submission.getRemarksByLead());
        verify(memberProgressService).refreshMemberProgress("member-1");
    }

    @Test
    void shouldRejectSubmittedStatusAsReviewInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> leadService.reviewTask("task-1", "member-1", SubmissionStatus.SUBMITTED, "Invalid")
        );
    }
}
