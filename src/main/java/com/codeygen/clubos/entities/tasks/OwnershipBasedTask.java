package com.codeygen.clubos.entities.tasks;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
@PrimaryKeyJoinColumn(name="task_id")
public class OwnershipBasedTask extends Task {
    private LocalDateTime biddingDeadline;
}
