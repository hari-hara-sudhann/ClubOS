package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.memberservice.BidPlacementDto;
import com.codeygen.clubos.dtos.memberservice.TaskSubmissionDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.BidRepository;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private OwnershipTaskRepository ownershipTaskRepository;

    @Mock
    private TaskSubmissionRepository taskSubmissionRepository;

    @Mock
    private TaskOwnershipRepository taskOwnershipRepository;

    @Mock
    private BidRepository bidRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldReturnRemainingTokensForBidding() {
        Member member = new Member();
        member.setUserId("member-1");
        member.setTokensAvailable(17);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        assertEquals(17, memberService.getRemainingTokensForBidding("member-1"));
    }

    @Test
    void shouldReturnZeroWhenRemainingTokensAreNull() {
        Member member = new Member();
        member.setUserId("member-1");
        member.setTokensAvailable(null);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        assertEquals(0, memberService.getRemainingTokensForBidding("member-1"));
    }

    @Test
    void shouldThrowWhenRemainingTokensRequestedForMissingMember() {
        when(memberRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.getRemainingTokensForBidding("missing"));
    }

    @Test
    void shouldSubmitTaskAndResetSubmissionStatusToSubmitted() {
        Member member = new Member();
        member.setUserId("member-1");

        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);

        TaskSubmission existingSubmission = new TaskSubmission();
        existingSubmission.setSubmissionId("submission-1");
        existingSubmission.setStatus(SubmissionStatus.REJECTED);
        existingSubmission.setRemarksByLead("Fix this");

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingSubmission));

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("https://proof");

        memberService.submitTask(dto);

        assertEquals(SubmissionStatus.SUBMITTED, existingSubmission.getStatus());
        assertEquals("https://proof", existingSubmission.getProofOfSubmission());
        assertNull(existingSubmission.getRemarksByLead());
        verify(taskSubmissionRepository).save(existingSubmission);
    }

    @Test
    void shouldCreateNewSubmissionWhenNoneExists() {
        Member member = new Member();
        member.setUserId("member-1");

        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.GENERAL);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        memberService.submitTask(dto);

        ArgumentCaptor<TaskSubmission> submissionCaptor = ArgumentCaptor.forClass(TaskSubmission.class);
        verify(taskSubmissionRepository).save(submissionCaptor.capture());
        assertEquals(SubmissionStatus.SUBMITTED, submissionCaptor.getValue().getStatus());
    }

    @Test
    void shouldThrowWhenSubmittingTaskForMissingMember() {
        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        when(memberRepository.findById("member-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldThrowWhenSubmittingMissingTask() {
        Member member = new Member();
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.empty());

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        assertThrows(NoSuchElementException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldThrowWhenSubmissionProofIsBlank() {
        Member member = new Member();
        Task task = new Task();

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission(" ");

        assertThrows(IllegalArgumentException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldThrowWhenSubmissionProofIsNull() {
        Member member = new Member();
        Task task = new Task();

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission(null);

        assertThrows(IllegalArgumentException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldThrowWhenApprovedSubmissionIsEdited() {
        Member member = new Member();
        Task task = new Task();
        task.setTaskType(TaskTypes.GENERAL);

        TaskSubmission existingSubmission = new TaskSubmission();
        existingSubmission.setSubmissionId("submission-1");
        existingSubmission.setStatus(SubmissionStatus.APPROVED);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingSubmission));

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        assertThrows(IllegalStateException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldThrowWhenNonOwnerSubmitsOwnershipTask() {
        Member member = new Member();
        member.setUserId("member-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        assertThrows(IllegalStateException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldAllowOwnerToSubmitOwnershipTask() {
        Member member = new Member();
        member.setUserId("member-1");

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");

        TaskOwnership ownership = new TaskOwnership();

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(ownership));
        when(taskSubmissionRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        memberService.submitTask(dto);

        verify(taskSubmissionRepository).save(any(TaskSubmission.class));
    }

    @Test
    void shouldRequireOwnershipLookupWhenTaskTypeIsOwnershipEvenForBaseTask() {
        Member member = new Member();
        member.setUserId("member-1");

        Task task = new Task();
        task.setTaskId("task-1");
        task.setTaskType(TaskTypes.OWNERSHIP_BASED);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        TaskSubmissionDto dto = new TaskSubmissionDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setProofOfSubmission("proof");

        assertThrows(IllegalStateException.class, () -> memberService.submitTask(dto));
    }

    @Test
    void shouldPlaceBidAndDeductTokens() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);
        member.setTokensAvailable(30);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.existsByTask_TaskIdAndMember_UserId("task-1", "member-1")).thenReturn(false);

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(12);

        memberService.placeBidOnOwnershipBasedTask(dto);

        assertEquals(18, member.getTokensAvailable());

        ArgumentCaptor<Bid> bidCaptor = ArgumentCaptor.forClass(Bid.class);
        verify(bidRepository).save(bidCaptor.capture());
        Bid savedBid = bidCaptor.getValue();
        assertEquals(member, savedBid.getMember());
        assertEquals(task, savedBid.getTask());
        assertEquals(12, savedBid.getTokensBidded());
        assertEquals(BidStatus.PENDING, savedBid.getStatus());
    }

    @Test
    void shouldThrowWhenPlacingBidForMissingTask() {
        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenPlacingBidForMissingMember() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.empty());

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(NoSuchElementException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenPlacingBidWithInvalidTokens() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        Member member = new Member();

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(0);

        assertThrows(IllegalArgumentException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenBiddingClosed() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(30);
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.ASSIGNED_BY_BIDDING);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(IllegalStateException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenBiddingDeadlinePassed() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(30);
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(IllegalStateException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenBiddingFromWrongDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Department other = new Department();
        other.setDepartmentId("dept-2");
        Member member = new Member();
        member.setDept(other);
        member.setTokensAvailable(30);
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(IllegalArgumentException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenBiddingWithoutDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Member member = new Member();
        member.setDept(null);
        member.setTokensAvailable(30);
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(IllegalArgumentException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenBidAlreadyExists() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(30);
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.existsByTask_TaskIdAndMember_UserId("task-1", "member-1")).thenReturn(true);

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(IllegalStateException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldOnlyAllowBiddingWithRemainingTokensAcrossMultipleTasks() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);
        member.setTokensAvailable(30);

        OwnershipBasedTask firstTask = new OwnershipBasedTask();
        firstTask.setTaskId("task-1");
        firstTask.setDept(department);
        firstTask.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        firstTask.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        OwnershipBasedTask secondTask = new OwnershipBasedTask();
        secondTask.setTaskId("task-2");
        secondTask.setDept(department);
        secondTask.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        secondTask.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(firstTask));
        when(ownershipTaskRepository.findById("task-2")).thenReturn(Optional.of(secondTask));
        when(bidRepository.existsByTask_TaskIdAndMember_UserId("task-1", "member-1")).thenReturn(false);
        when(bidRepository.existsByTask_TaskIdAndMember_UserId("task-2", "member-1")).thenReturn(false);

        BidPlacementDto firstBid = new BidPlacementDto();
        firstBid.setMemberId("member-1");
        firstBid.setTaskId("task-1");
        firstBid.setTokensBidded(5);

        memberService.placeBidOnOwnershipBasedTask(firstBid);

        assertEquals(25, memberService.getRemainingTokensForBidding("member-1"));

        BidPlacementDto secondBid = new BidPlacementDto();
        secondBid.setMemberId("member-1");
        secondBid.setTaskId("task-2");
        secondBid.setTokensBidded(26);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.placeBidOnOwnershipBasedTask(secondBid)
        );

        assertEquals("Member does not have enough tokens. Remaining tokens: 25", exception.getMessage());
    }

    @Test
    void shouldThrowWhenPlacingBidWithNullTokens() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        Member member = new Member();

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(null);

        assertThrows(IllegalArgumentException.class, () -> memberService.placeBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldTreatNullAvailableTokensAsZeroWhenPlacingBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(null);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.existsByTask_TaskIdAndMember_UserId("task-1", "member-1")).thenReturn(false);

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(1);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.placeBidOnOwnershipBasedTask(dto)
        );

        assertEquals("Member does not have enough tokens. Remaining tokens: 0", exception.getMessage());
    }

    @Test
    void shouldUpdateBidAndAdjustHeldTokens() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTask(task);
        existingBid.setMember(member);
        existingBid.setTokensBidded(8);
        existingBid.setStatus(BidStatus.PENDING);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(14);

        memberService.updateBidOnOwnershipBasedTask(dto);

        assertEquals(14, existingBid.getTokensBidded());
        assertEquals(4, member.getTokensAvailable());
    }

    @Test
    void shouldReturnTokensWhenBidIsLowered() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(5);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(10);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(4);

        memberService.updateBidOnOwnershipBasedTask(dto);

        assertEquals(11, member.getTokensAvailable());
        assertEquals(4, existingBid.getTokensBidded());
    }

    @Test
    void shouldHandleNullPreviousBidTokensWhenUpdatingBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(null);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(4);

        memberService.updateBidOnOwnershipBasedTask(dto);

        assertEquals(6, member.getTokensAvailable());
        assertEquals(4, existingBid.getTokensBidded());
    }

    @Test
    void shouldThrowWhenUpdatingMissingOwnershipTask() {
        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingBidForMissingMember() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.empty());

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(NoSuchElementException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingMissingBid() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        Member member = new Member();
        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1")).thenReturn(Optional.empty());

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(5);

        assertThrows(NoSuchElementException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingBidOutsideDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");
        Department other = new Department();
        other.setDepartmentId("dept-2");

        Member member = new Member();
        member.setDept(other);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(7);

        assertThrows(IllegalArgumentException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingBidWithoutDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(null);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(7);

        assertThrows(IllegalArgumentException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingClosedBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.ASSIGNED_BY_BIDDING);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(7);

        assertThrows(IllegalStateException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingExpiredBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(7);

        assertThrows(IllegalStateException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingNonPendingBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(10);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.WON);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(7);

        assertThrows(IllegalStateException.class, () -> memberService.updateBidOnOwnershipBasedTask(dto));
    }

    @Test
    void shouldThrowWhenUpdatingBidWithoutEnoughAdditionalTokens() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(1);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.updateBidOnOwnershipBasedTask(dto)
        );

        assertEquals(
                "Member does not have enough tokens to increase the bid. Remaining tokens: 1",
                exception.getMessage()
        );
    }

    @Test
    void shouldTreatNullAvailableTokensAsZeroWhenUpdatingBid() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setDept(department);
        member.setTokensAvailable(null);

        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setDept(department);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().plusHours(1));

        Bid existingBid = new Bid();
        existingBid.setTokensBidded(5);
        existingBid.setStatus(BidStatus.PENDING);

        when(ownershipTaskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(bidRepository.findByTask_TaskIdAndMember_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(existingBid));

        BidPlacementDto dto = new BidPlacementDto();
        dto.setMemberId("member-1");
        dto.setTaskId("task-1");
        dto.setTokensBidded(8);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> memberService.updateBidOnOwnershipBasedTask(dto)
        );

        assertEquals(
                "Member does not have enough tokens to increase the bid. Remaining tokens: 0",
                exception.getMessage()
        );
    }
}
