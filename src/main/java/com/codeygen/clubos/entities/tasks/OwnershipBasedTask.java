package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@PrimaryKeyJoinColumn(name="task_id")
public class OwnershipBasedTask extends Task {
    private LocalDateTime biddingDeadline;
    private Integer numberOfOwners;

    @Enumerated(EnumType.STRING)
    private OwnershipAssignmentStatus assignmentStatus = OwnershipAssignmentStatus.BIDDING_OPEN;
}
