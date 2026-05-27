package com.codeygen.clubos.services;

import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.entities.user.enums.CyclePerformanceStatus;
import com.codeygen.clubos.repositories.task.TaskOwnershipRepository;
import com.codeygen.clubos.repositories.task.TaskRepository;
import com.codeygen.clubos.repositories.task.TaskSubmissionRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberProgressServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSubmissionRepository taskSubmissionRepository;

    @Mock
    private TaskOwnershipRepository taskOwnershipRepository;

    @InjectMocks
    private MemberProgressService memberProgressService;

    @Test
    void shouldMarkMemberPassingWhenApprovedPointsReachSixtyPercent() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);

        Task availableTask = new Task();
        availableTask.setTaskId("task-1");
        availableTask.setPoints(100);

        Task approvedTask = new Task();
        approvedTask.setTaskId("task-1");
        approvedTask.setPoints(60);

        TaskSubmission approvedSubmission = new TaskSubmission();
        approvedSubmission.setTask(approvedTask);
        approvedSubmission.setStatus(SubmissionStatus.APPROVED);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(availableTask));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                "member-1",
                SubmissionStatus.APPROVED,
                "dept-1"
        )).thenReturn(List.of(approvedSubmission));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus(
                "member-1",
                SubmissionStatus.APPROVED
        )).thenReturn(List.of(approvedSubmission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        memberProgressService.updateStatusForMember("member-1");

        assertEquals(CyclePerformanceStatus.PASSING, member.getPerformanceStatus());
        verify(memberRepository).save(member);
    }

    @Test
    void shouldThrowWhenRefreshingMissingMember() {
        when(memberRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> memberProgressService.refreshMemberProgress("missing"));
    }

    @Test
    void shouldMarkNoTasksAssignedWhenMemberHasNoDepartment() {
        Member member = new Member();
        member.setUserId("member-1");

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of());

        memberProgressService.refreshMemberProgress("member-1");

        assertEquals(CyclePerformanceStatus.NO_TASKS_ASSIGNED, member.getPerformanceStatus());
        assertEquals(0, member.getCumulativePoints());
        verify(memberRepository).save(member);
    }

    @Test
    void shouldMarkNoTasksAssignedWhenDepartmentHasNoTaskPoints() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);

        Task task = new Task();
        task.setTaskId("task-1");
        task.setPoints(null);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(task));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of());
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                "member-1", SubmissionStatus.APPROVED, "dept-1"
        )).thenReturn(List.of());

        memberProgressService.refreshMemberProgress("member-1");

        assertEquals(CyclePerformanceStatus.NO_TASKS_ASSIGNED, member.getPerformanceStatus());
    }

    @Test
    void shouldMarkMemberFailingWhenBelowThreshold() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);

        Task availableTask = new Task();
        availableTask.setTaskId("task-1");
        availableTask.setPoints(100);

        Task approvedTask = new Task();
        approvedTask.setTaskId("task-1");
        approvedTask.setPoints(20);

        TaskSubmission approvedSubmission = new TaskSubmission();
        approvedSubmission.setTask(approvedTask);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(availableTask));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of(approvedSubmission));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                "member-1", SubmissionStatus.APPROVED, "dept-1"
        )).thenReturn(List.of(approvedSubmission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.empty());

        memberProgressService.refreshMemberProgress("member-1");

        assertEquals(CyclePerformanceStatus.FAILING, member.getPerformanceStatus());
        assertEquals(20, member.getCumulativePoints());
    }

    @Test
    void shouldUseOwnershipAssignedPointsWhenPresent() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);

        Task availableTask = new Task();
        availableTask.setTaskId("task-1");
        availableTask.setPoints(100);

        Task approvedTask = new Task();
        approvedTask.setTaskId("task-1");
        approvedTask.setPoints(100);

        TaskSubmission approvedSubmission = new TaskSubmission();
        approvedSubmission.setTask(approvedTask);

        TaskOwnership ownership = new TaskOwnership();
        ownership.setAssignedPoints(60);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(availableTask));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of(approvedSubmission));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                "member-1", SubmissionStatus.APPROVED, "dept-1"
        )).thenReturn(List.of(approvedSubmission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-1", "member-1"))
                .thenReturn(Optional.of(ownership));

        memberProgressService.refreshMemberProgress("member-1");

        assertEquals(60, member.getCumulativePoints());
        assertEquals(CyclePerformanceStatus.PASSING, member.getPerformanceStatus());
    }

    @Test
    void shouldTreatNullOwnershipPointsAndNullTaskPointsAsZero() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(department);

        Task availableTask = new Task();
        availableTask.setTaskId("task-available");
        availableTask.setPoints(100);

        Task approvedTask = new Task();
        approvedTask.setTaskId("task-approved");
        approvedTask.setPoints(null);

        TaskSubmission approvedSubmission = new TaskSubmission();
        approvedSubmission.setTask(approvedTask);

        TaskOwnership ownership = new TaskOwnership();
        ownership.setAssignedPoints(null);

        when(memberRepository.findById("member-1")).thenReturn(Optional.of(member));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(availableTask));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of(approvedSubmission));
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                "member-1", SubmissionStatus.APPROVED, "dept-1"
        )).thenReturn(List.of(approvedSubmission));
        when(taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId("task-approved", "member-1"))
                .thenReturn(Optional.of(ownership));

        memberProgressService.refreshMemberProgress("member-1");

        assertEquals(0, member.getCumulativePoints());
        assertEquals(CyclePerformanceStatus.FAILING, member.getPerformanceStatus());
    }

    @Test
    void shouldRefreshAllMembersInDepartment() {
        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member first = new Member();
        first.setUserId("member-1");
        first.setDept(department);

        Member second = new Member();
        second.setUserId("member-2");
        second.setDept(department);

        when(memberRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of(first, second));
        when(memberRepository.findById("member-1")).thenReturn(Optional.of(first));
        when(memberRepository.findById("member-2")).thenReturn(Optional.of(second));
        when(taskRepository.findByDept_DepartmentId("dept-1")).thenReturn(List.of());
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-1", SubmissionStatus.APPROVED))
                .thenReturn(List.of());
        when(taskSubmissionRepository.findAllByMember_UserIdAndStatus("member-2", SubmissionStatus.APPROVED))
                .thenReturn(List.of());

        memberProgressService.updateStatusesForDepartment("dept-1");

        verify(memberRepository, times(2)).save(any(Member.class));
    }
}
