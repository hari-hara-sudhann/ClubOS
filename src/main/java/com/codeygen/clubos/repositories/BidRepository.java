package com.codeygen.clubos.repositories;

import com.codeygen.clubos.entities.bid.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, String> {
    @Query("""
    SELECT b
    FROM Bid b
    JOIN b.member m
    WHERE b.task.taskId = :taskId
    ORDER BY b.tokensBidded DESC,
             m.cumulativePoints ASC
    """)
    List<Bid> rankBids(String taskId);

    List<Bid> findByTask_TaskId(String taskId);

    boolean existsByTask_TaskIdAndMember_UserId(String taskId, String userId);

    Optional<Bid> findByTask_TaskIdAndMember_UserId(String taskId, String userId);
}
