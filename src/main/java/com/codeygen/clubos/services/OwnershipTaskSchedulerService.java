package com.codeygen.clubos.services;

import com.codeygen.clubos.entities.TaskOwnership;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAcquisitionType;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.BidRepository;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.task.TaskOwnershipRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnershipTaskSchedulerService {
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final BidRepository bidRepository;
    private final TaskOwnershipRepository taskOwnershipRepository;
    private final MemberRepository memberRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void assignOwnersForExpiredTasks() {
        List<OwnershipBasedTask> expiredTasks =
                ownershipTaskRepository.findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
                        OwnershipAssignmentStatus.BIDDING_OPEN,
                        LocalDateTime.now()
                );

        for (OwnershipBasedTask task : expiredTasks) {
            List<Bid> rankedBids = bidRepository.rankBids(task.getTaskId());

            if (rankedBids.isEmpty()) {
                task.setAssignmentStatus(OwnershipAssignmentStatus.NO_VALID_BIDS);
                ownershipTaskRepository.save(task);
                continue;
            }

            int ownerLimit = Math.min(task.getNumberOfOwners(), rankedBids.size());
            List<TaskOwnership> ownerships = new ArrayList<>();

            for (int index = 0; index < rankedBids.size(); index++) {
                Bid bid = rankedBids.get(index);
                if (index < ownerLimit) {
                    bid.setStatus(BidStatus.WON);
                    TaskOwnership ownership = new TaskOwnership();
                    ownership.setTask(task);
                    ownership.setOwner(bid.getMember());
                    ownership.setOwnershipAssignedAt(LocalDateTime.now());
                    ownership.setAssignedPoints(task.getPoints());
                    ownership.setAcquisitionType(OwnershipAcquisitionType.BID);
                    ownerships.add(ownership);
                } else {
                    bid.setStatus(BidStatus.LOST);
                    refundBidIfNeeded(bid);
                }
            }

            taskOwnershipRepository.saveAll(ownerships);
            bidRepository.saveAll(rankedBids);
            task.setAssignmentStatus(OwnershipAssignmentStatus.ASSIGNED_BY_BIDDING);
            ownershipTaskRepository.save(task);
        }
    }

    private void refundBidIfNeeded(Bid bid) {
        if (Boolean.TRUE.equals(bid.getRefunded())) {
            return;
        }

        Member bidder = bid.getMember();
        int currentTokens = bidder.getTokensAvailable() == null ? 0 : bidder.getTokensAvailable();
        int tokensToRefund = bid.getTokensBidded() == null ? 0 : bid.getTokensBidded();
        bidder.setTokensAvailable(currentTokens + tokensToRefund);
        bid.setRefunded(true);
        memberRepository.save(bidder);
    }
}
