package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.tasks.TaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission, String> {
    List<TaskSubmission> findAllByTask_TaskId(String taskId);
    Optional<TaskSubmission> findByTask_TaskIdAndMember_UserId(String taskId, String userId);
}
