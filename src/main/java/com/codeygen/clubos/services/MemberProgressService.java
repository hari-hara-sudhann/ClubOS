package com.codeygen.clubos.services;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemberProgressService {
    private static final double PASSING_THRESHOLD = 0.60;

    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskOwnershipRepository taskOwnershipRepository;

    public void refreshMemberProgress(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));

        member.setCumulativePoints(getTotalApprovedPointsForMember(member));

        if (member.getDept() == null) {
            member.setPerformanceStatus(CyclePerformanceStatus.NO_TASKS_ASSIGNED);
            memberRepository.save(member);
            return;
        }

        int totalPointsGiven = getTotalDepartmentPoints(member.getDept().getDepartmentId());
        int earnedPoints = getApprovedPointsForMember(member);

        if (totalPointsGiven <= 0) {
            member.setPerformanceStatus(CyclePerformanceStatus.NO_TASKS_ASSIGNED);
        } else if (earnedPoints >= Math.ceil(totalPointsGiven * PASSING_THRESHOLD)) {
            member.setPerformanceStatus(CyclePerformanceStatus.PASSING);
        } else {
            member.setPerformanceStatus(CyclePerformanceStatus.FAILING);
        }

        memberRepository.save(member);
    }

    public void updateStatusesForDepartment(String departmentId) {
        List<Member> members = memberRepository.findByDept_DepartmentId(departmentId);
        for (Member member : members) {
            refreshMemberProgress(member.getUserId());
        }
    }

    public void updateStatusForMember(String memberId) {
        refreshMemberProgress(memberId);
    }

    private int getTotalDepartmentPoints(String departmentId) {
        int totalPoints = 0;
        List<Task> tasks = taskRepository.findByDept_DepartmentId(departmentId);
        for (Task task : tasks) {
            if (task.getPoints() != null) {
                totalPoints += task.getPoints();
            }
        }
        return totalPoints;
    }

    private int getApprovedPointsForMember(Member member) {
        int earnedPoints = 0;
        List<TaskSubmission> approvedSubmissions =
                taskSubmissionRepository.findAllByMember_UserIdAndStatusAndTask_Dept_DepartmentId(
                        member.getUserId(),
                        SubmissionStatus.APPROVED,
                        member.getDept().getDepartmentId()
                );

        for (TaskSubmission submission : approvedSubmissions) {
            Task task = submission.getTask();
            TaskOwnership ownership = taskOwnershipRepository
                    .findByTask_TaskIdAndOwner_UserId(task.getTaskId(), member.getUserId())
                    .orElse(null);

            if (ownership != null && ownership.getAssignedPoints() != null) {
                earnedPoints += ownership.getAssignedPoints();
                continue;
            }

            if (task.getPoints() != null) {
                earnedPoints += task.getPoints();
            }
        }

        return earnedPoints;
    }

    private int getTotalApprovedPointsForMember(Member member) {
        int earnedPoints = 0;
        List<TaskSubmission> approvedSubmissions =
                taskSubmissionRepository.findAllByMember_UserIdAndStatus(
                        member.getUserId(),
                        SubmissionStatus.APPROVED
                );

        for (TaskSubmission submission : approvedSubmissions) {
            earnedPoints += getPointsFromSubmission(submission, member.getUserId());
        }

        return earnedPoints;
    }

    private int getPointsFromSubmission(TaskSubmission submission, String userId) {
        Task task = submission.getTask();
        TaskOwnership ownership = taskOwnershipRepository
                .findByTask_TaskIdAndOwner_UserId(task.getTaskId(), userId)
                .orElse(null);

        if (ownership != null && ownership.getAssignedPoints() != null) {
            return ownership.getAssignedPoints();
        }

        if (task.getPoints() != null) {
            return task.getPoints();
        }

        return 0;
    }
}
