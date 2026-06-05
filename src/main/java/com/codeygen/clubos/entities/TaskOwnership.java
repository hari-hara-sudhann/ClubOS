package com.codeygen.clubos.entities;

import com.codeygen.clubos.entities.tasks.enums.OwnershipAcquisitionType;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.user.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskOwnership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskOwnershipId;

    private LocalDateTime ownershipAssignedAt;
    private Integer assignedPoints;

    @Enumerated(EnumType.STRING)
    private OwnershipAcquisitionType acquisitionType;

    @ManyToOne
    @JoinColumn(name="transferred_to_user_id")
    private Member ownershipTransferredTo;

    @ManyToOne
    @JoinColumn(name="task_id")
    private OwnershipBasedTask task;

    @ManyToOne
    @JoinColumn(name="owner_user_id")
    private Member owner;
}
