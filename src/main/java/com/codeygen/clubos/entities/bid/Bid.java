package com.codeygen.clubos.entities.bid;

import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.user.Member;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bidId;

    @ManyToOne
    @JoinColumn(name="task_id")
    private OwnershipBasedTask task;

    @ManyToOne
    @JoinColumn(name="user_id")
    private Member member;

    private Integer tokensBidded;

    @Enumerated(EnumType.STRING)
    private BidStatus status;
}
