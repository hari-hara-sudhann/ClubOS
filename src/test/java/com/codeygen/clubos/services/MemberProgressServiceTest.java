package com.codeygen.clubos.services;

import com.codeygen.clubos.entities.Department;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
