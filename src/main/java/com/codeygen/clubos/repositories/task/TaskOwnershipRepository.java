package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.TaskOwnership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TaskOwnershipRepository extends JpaRepository<TaskOwnership, String> {
    List<TaskOwnership> findByTask_TaskId(String taskId);
    List<TaskOwnership> findByOwner_UserId(String userId);
    Optional<TaskOwnership> findByTask_TaskIdAndOwner_UserId(String taskId, String userId);
}
