package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskDirectAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GetSubmissionsDto;
import com.codeygen.clubos.dtos.taskservice.TaskReviewDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void shouldAssignGeneralTaskSuccessfully() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        GeneralTaskAssignmentDto dto = new GeneralTaskAssignmentDto();
        dto.setName("Poster Design");
        dto.setPoints(20);
        dto.setDeptId("dept-1");
        dto.setDeadline(LocalDateTime.now().plusDays(2));

        when(departmentRepository.findById("dept-1")).thenReturn(Optional.of(department));

        leadService.assignGeneralTask(dto);

        ArgumentCaptor<GeneralTask> taskCaptor = ArgumentCaptor.forClass(GeneralTask.class);
        verify(taskRepository).save(taskCaptor.capture());
        GeneralTask savedTask = taskCaptor.getValue();
        assertEquals("Poster Design", savedTask.getName());
        assertEquals(20, savedTask.getPoints());
        assertEquals(TaskTypes.GENERAL, savedTask.getTaskType());
        assertEquals(department, savedTask.getDept());
        verify(memberProgressService).updateStatusesForDepartment("dept-1");
    }

    @Test
    void shouldRejectGeneralTaskWithNonPositivePoints() {
        GeneralTaskAssignmentDto dto = new GeneralTaskAssignmentDto();
        dto.setPoints(0);

        assertThrows(IllegalArgumentException.class, () -> leadService.assignGeneralTask(dto));
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldRejectGeneralTaskWithNullPoints() {
        GeneralTaskAssignmentDto dto = new GeneralTaskAssignmentDto();
        dto.setPoints(null);

        assertThrows(IllegalArgumentException.class, () -> leadService.assignGeneralTask(dto));
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldThrowWhenGeneralTaskDepartmentMissing() {
        GeneralTaskAssignmentDto dto = new GeneralTaskAssignmentDto();
        dto.setName("Poster Design");
        dto.setPoints(20);
        dto.setDeptId("dept-1");

        when(departmentRepository.findById("dept-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leadService.assignGeneralTask(dto));
    }

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
    void shouldRejectOwnershipTaskWithInvalidOwnerCount() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(0);
        dto.setBiddingDeadline(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnershipBasedTask(dto));
    }

    @Test
    void shouldRejectOwnershipTaskWithNullOwnerCount() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(null);
        dto.setBiddingDeadline(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnershipBasedTask(dto));
    }

    @Test
    void shouldRejectOwnershipTaskWithPastBiddingDeadline() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(1);
        dto.setBiddingDeadline(LocalDateTime.now().minusMinutes(1));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnershipBasedTask(dto));
    }

    @Test
    void shouldRejectOwnershipTaskWithNullBiddingDeadline() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(1);
        dto.setBiddingDeadline(null);

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnershipBasedTask(dto));
    }

    @Test
    void shouldRejectOwnershipTaskWhenDeadlineIsBeforeBiddingDeadline() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(1);
        dto.setBiddingDeadline(LocalDateTime.now().plusDays(2));
        dto.setDeadline(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenOwnershipTaskDepartmentMissing() {
        OwnershipTaskAssignmentDto dto = new OwnershipTaskAssignmentDto();
        dto.setPoints(10);
        dto.setNumberOfOwners(1);
        dto.setDeptId("dept-1");
        dto.setBiddingDeadline(LocalDateTime.now().plusDays(1));

        when(departmentRepository.findById("dept-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leadService.assignOwnershipBasedTask(dto));
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

        Member bidder = new Member();
        bidder.setUserId("member-3");
        bidder.setTokensAvailable(10);

        Bid bid = new Bid();
        bid.setMember(bidder);
        bid.setStatus(BidStatus.PENDING);
        bid.setTokensBidded(15);
        bid.setRefunded(false);

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
        assertEquals(25, bidder.getTokensAvailable());
        assertTrue(bid.getRefunded());

        verify(memberProgressService).updateStatusesForDepartment("dept-1");
    }

    @Test
    void shouldNotRefundBidTwiceWhenAlreadyRefundedInDirectAssignment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setDept(department);
        task.setPoints(100);
        task.setNumberOfOwners(1);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(2));
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);

        Member owner = new Member();
        owner.setUserId("member-1");
        owner.setDept(department);

        Member bidder = new Member();
        bidder.setUserId("member-2");
        bidder.setTokensAvailable(10);

        Bid bid = new Bid();
        bid.setMember(bidder);
        bid.setStatus(BidStatus.PENDING);
        bid.setTokensBidded(15);
        bid.setRefunded(true);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(owner));
        when(bidRepository.findByTask_TaskId("task-1")).thenReturn(List.of(bid));

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        leadService.assignOwnersDirectly(dto);

        assertEquals(10, bidder.getTokensAvailable());
        verify(memberRepository, never()).save(bidder);
    }

    @Test
    void shouldThrowWhenDirectAssignmentTaskNotFound() {
        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("missing");

        when(ownershipTaskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentAlreadyClosed() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.ASSIGNED_BY_BIDDING);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalStateException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentDeadlinePassed() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(1));

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalStateException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentOwnerCountMismatch() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(2);
        task.setPoints(50);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentOwnersAreNull() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(50);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(null);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentHasDuplicateOwners() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(2);
        task.setPoints(50);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1", "member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentTaskPointsInvalid() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(0);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalStateException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentTaskPointsAreNull() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(null);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));

        assertThrows(IllegalStateException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentMemberNotFound() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(50);
        task.setDept(department);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentMemberFromWrongDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Department otherDepartment = new Department();
        otherDepartment.setDepartmentId("dept-2");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(50);
        task.setDept(department);

        Member owner = new Member();
        owner.setUserId("member-1");
        owner.setDept(otherDepartment);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldThrowWhenDirectAssignmentMemberHasNoDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));
        task.setNumberOfOwners(1);
        task.setPoints(50);
        task.setDept(department);

        Member owner = new Member();
        owner.setUserId("member-1");
        owner.setDept(null);

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () -> leadService.assignOwnersDirectly(dto));
    }

    @Test
    void shouldGetEmptySubmissionsForTask() {
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(new Task()));
        when(taskSubmissionRepository.findAllByTask_TaskId("task-1")).thenReturn(Collections.emptyList());

        GetSubmissionsDto result = leadService.getSubmissions("task-1");

        assertEquals("task-1", result.getTaskId());
        assertTrue(result.getSubmissions().isEmpty());
    }

    @Test
    void shouldMapTaskSubmissionsToDtos() {
        Task task = new Task();
        task.setTaskId("task-1");

        Member member = new Member();
        member.setUserId("member-1");

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setMember(member);
        submission.setProofOfSubmission("proof-link");

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskSubmissionRepository.findAllByTask_TaskId("task-1")).thenReturn(List.of(submission));

        GetSubmissionsDto result = leadService.getSubmissions("task-1");

        assertEquals(1, result.getSubmissions().size());
        assertEquals("member-1", result.getSubmissions().get(0).getMemberId());
        assertEquals("proof-link", result.getSubmissions().get(0).getProofOfSubmission());
    }

    @Test
    void shouldThrowWhenGettingSubmissionsForMissingTask() {
        when(taskRepository.findById("task-1")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> leadService.getSubmissions("task-1"));
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

        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.APPROVED);
        dto.setRemarks("Good work");

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));

        leadService.reviewTask(dto);

        assertEquals(SubmissionStatus.APPROVED, submission.getStatus());
        assertEquals("Good work", submission.getRemarksByLead());
        verify(memberProgressService).refreshMemberProgress("member-1");
    }

    @Test
    void shouldThrowWhenReviewStatusIsNull() {
        TaskReviewDto dto = new TaskReviewDto();
        dto.setStatus(null);

        assertThrows(IllegalArgumentException.class, () -> leadService.reviewTask(dto));
    }

    @Test
    void shouldAllowCorrectingSubmissionStatusAfterMistake() {
        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);
        submission.setStatus(SubmissionStatus.APPROVED);

        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.REJECTED);
        dto.setRemarks("Correction");

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));

        leadService.reviewTask(dto);

        assertEquals(SubmissionStatus.REJECTED, submission.getStatus());
        assertEquals("Correction", submission.getRemarksByLead());
        verify(memberProgressService).refreshMemberProgress("member-1");
    }

    @Test
    void shouldThrowWhenReviewSubmissionNotFound() {
        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.APPROVED);

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> leadService.reviewTask(dto));
    }

    @Test
    void shouldThrowWhenApprovingOwnershipSubmissionForNonOwner() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);

        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.APPROVED);

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> leadService.reviewTask(dto));
    }

    @Test
    void shouldAllowRejectingOwnershipSubmissionWithoutOwnerLookup() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);

        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.REJECTED);
        dto.setRemarks("No");

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));

        leadService.reviewTask(dto);

        verify(taskOwnershipRepository, never()).findByTask_TaskIdAndOwner_UserId(any(), any());
        assertEquals(SubmissionStatus.REJECTED, submission.getStatus());
    }

    @Test
    void shouldValidateOwnershipSubmissionUsingInstanceTypeEvenWhenTaskTypeIsNotOwnership() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);

        TaskSubmission submission = new TaskSubmission();
        submission.setTask(task);

        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.APPROVED);

        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(submission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> leadService.reviewTask(dto));
    }

    @Test
    void shouldRefundZeroTokensWhenBidAndBalanceAreNullDuringDirectAssignment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setDept(department);
        task.setPoints(100);
        task.setNumberOfOwners(1);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(2));
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);

        Member owner = new Member();
        owner.setUserId("member-1");
        owner.setDept(department);

        Member bidder = new Member();
        bidder.setUserId("member-2");
        bidder.setTokensAvailable(null);

        Bid bid = new Bid();
        bid.setMember(bidder);
        bid.setStatus(BidStatus.PENDING);
        bid.setTokensBidded(null);
        bid.setRefunded(false);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(owner));
        when(bidRepository.findByTask_TaskId("task-1")).thenReturn(List.of(bid));

        OwnershipTaskDirectAssignmentDto dto = new OwnershipTaskDirectAssignmentDto();
        dto.setTaskId("task-1");
        dto.setOwnerIds(List.of("member-1"));

        leadService.assignOwnersDirectly(dto);

        assertEquals(0, bidder.getTokensAvailable());
        assertTrue(bid.getRefunded());
        verify(memberRepository).save(bidder);
    }

    @Test
    void shouldRejectSubmittedStatusAsReviewInput() {
        TaskReviewDto dto = new TaskReviewDto();
        dto.setTaskId("task-1");
        dto.setMemberId("member-1");
        dto.setStatus(SubmissionStatus.SUBMITTED);
        dto.setRemarks("Invalid");

        assertThrows(
                IllegalArgumentException.class,
                () -> leadService.reviewTask(dto)
        );
    }
}
