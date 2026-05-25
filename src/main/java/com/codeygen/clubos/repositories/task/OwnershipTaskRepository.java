package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OwnershipTaskRepository extends JpaRepository<OwnershipBasedTask, String> {
    List<OwnershipBasedTask> findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
            OwnershipAssignmentStatus assignmentStatus,
            LocalDateTime biddingDeadline
    );
}
