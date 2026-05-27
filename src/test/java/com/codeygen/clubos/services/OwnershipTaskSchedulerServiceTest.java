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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnershipTaskSchedulerServiceTest {

    @Mock
    private OwnershipTaskRepository ownershipTaskRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TaskOwnershipRepository taskOwnershipRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private OwnershipTaskSchedulerService ownershipTaskSchedulerService;

    @Test
    void shouldAssignTopNBiddersWhenBiddingDeadlineHasPassed() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setPoints(80);
        task.setNumberOfOwners(2);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(5));

        Member one = new Member();
        one.setUserId("member-1");
        one.setTokensAvailable(10);

        Member two = new Member();
        two.setUserId("member-2");
        two.setTokensAvailable(20);

        Member three = new Member();
        three.setUserId("member-3");
        three.setTokensAvailable(30);

        Bid highestBid = new Bid();
        highestBid.setMember(one);
        highestBid.setTokensBidded(25);

        Bid secondBid = new Bid();
        secondBid.setMember(two);
        secondBid.setTokensBidded(15);

        Bid thirdBid = new Bid();
        thirdBid.setMember(three);
        thirdBid.setTokensBidded(5);

        when(ownershipTaskRepository.findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
                eq(OwnershipAssignmentStatus.BIDDING_OPEN),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));
        when(bidRepository.rankBids("task-1")).thenReturn(List.of(highestBid, secondBid, thirdBid));

        ownershipTaskSchedulerService.assignOwnersForExpiredTasks();

        assertEquals(BidStatus.WON, highestBid.getStatus());
        assertEquals(BidStatus.WON, secondBid.getStatus());
        assertEquals(BidStatus.LOST, thirdBid.getStatus());
        assertEquals(OwnershipAssignmentStatus.ASSIGNED_BY_BIDDING, task.getAssignmentStatus());
        assertEquals(35, thirdBid.getMember().getTokensAvailable());
        assertTrue(thirdBid.getRefunded());

        ArgumentCaptor<List<TaskOwnership>> ownershipCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskOwnershipRepository).saveAll(ownershipCaptor.capture());

        List<TaskOwnership> savedOwnerships = ownershipCaptor.getValue();
        assertEquals(2, savedOwnerships.size());
        assertTrue(savedOwnerships.stream().allMatch(ownership -> ownership.getAssignedPoints() == 80));
        assertTrue(savedOwnerships.stream().allMatch(
                ownership -> ownership.getAcquisitionType() == OwnershipAcquisitionType.BID
        ));
    }

    @Test
    void shouldMarkTaskAsNoValidBidsWhenRankedListIsEmpty() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(5));

        when(ownershipTaskRepository.findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
                eq(OwnershipAssignmentStatus.BIDDING_OPEN),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));
        when(bidRepository.rankBids("task-1")).thenReturn(List.of());

        ownershipTaskSchedulerService.assignOwnersForExpiredTasks();

        assertEquals(OwnershipAssignmentStatus.NO_VALID_BIDS, task.getAssignmentStatus());
        verify(ownershipTaskRepository).save(task);
        verify(taskOwnershipRepository, never()).saveAll(any());
    }

    @Test
    void shouldNotRefundLosingBidTwice() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setPoints(80);
        task.setNumberOfOwners(1);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(5));

        Member winner = new Member();
        winner.setTokensAvailable(10);
        Member loser = new Member();
        loser.setTokensAvailable(20);

        Bid winningBid = new Bid();
        winningBid.setMember(winner);
        winningBid.setTokensBidded(10);

        Bid losingBid = new Bid();
        losingBid.setMember(loser);
        losingBid.setTokensBidded(5);
        losingBid.setRefunded(true);

        when(ownershipTaskRepository.findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
                eq(OwnershipAssignmentStatus.BIDDING_OPEN),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));
        when(bidRepository.rankBids("task-1")).thenReturn(List.of(winningBid, losingBid));

        ownershipTaskSchedulerService.assignOwnersForExpiredTasks();

        assertEquals(20, loser.getTokensAvailable());
        verify(memberRepository, never()).save(loser);
    }

    @Test
    void shouldTreatNullRefundValuesAsZeroDuringLosingBidRefund() {
        OwnershipBasedTask task = new OwnershipBasedTask();
        task.setTaskId("task-1");
        task.setPoints(80);
        task.setNumberOfOwners(1);
        task.setAssignmentStatus(OwnershipAssignmentStatus.BIDDING_OPEN);
        task.setBiddingDeadline(LocalDateTime.now().minusMinutes(5));

        Member winner = new Member();
        winner.setTokensAvailable(10);
        Member loser = new Member();
        loser.setTokensAvailable(null);

        Bid winningBid = new Bid();
        winningBid.setMember(winner);
        winningBid.setTokensBidded(10);

        Bid losingBid = new Bid();
        losingBid.setMember(loser);
        losingBid.setTokensBidded(null);
        losingBid.setRefunded(false);

        when(ownershipTaskRepository.findByAssignmentStatusAndBiddingDeadlineLessThanEqual(
                eq(OwnershipAssignmentStatus.BIDDING_OPEN),
                any(LocalDateTime.class)
        )).thenReturn(List.of(task));
        when(bidRepository.rankBids("task-1")).thenReturn(List.of(winningBid, losingBid));

        ownershipTaskSchedulerService.assignOwnersForExpiredTasks();

        assertEquals(0, loser.getTokensAvailable());
        assertTrue(losingBid.getRefunded());
        verify(memberRepository).save(loser);
    }
}
