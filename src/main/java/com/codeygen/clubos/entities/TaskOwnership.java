package com.codeygen.clubos.entities;

import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.user.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class TaskOwnership {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskOwnershipId;

    private LocalDateTime ownershipAssignedAt;
    @ManyToOne
    @JoinColumn(name="user_id")
    private Member ownershipTransferredTo;

    @ManyToOne
    @JoinColumn(name="task_id")
    public OwnershipBasedTask task;

    @ManyToOne
    @JoinColumn(name="user_id")
    public Member owner;
}
