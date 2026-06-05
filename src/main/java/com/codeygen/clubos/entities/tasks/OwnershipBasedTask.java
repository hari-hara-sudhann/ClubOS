package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name="task_id")
public class OwnershipBasedTask extends Task {
    private LocalDateTime biddingDeadline;
    private Integer numberOfOwners;

    @Enumerated(EnumType.STRING)
    private OwnershipAssignmentStatus assignmentStatus = OwnershipAssignmentStatus.BIDDING_OPEN;
}
