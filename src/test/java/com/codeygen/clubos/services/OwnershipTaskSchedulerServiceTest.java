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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipTaskSchedulerServiceTest {

    @Mock
    private OwnershipTaskRepository ownershipTaskRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TaskOwnershipRepository taskOwnershipRepository;

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

        Member two = new Member();
        two.setUserId("member-2");

        Member three = new Member();
        three.setUserId("member-3");

        Bid highestBid = new Bid();
        highestBid.setMember(one);

        Bid secondBid = new Bid();
        secondBid.setMember(two);

        Bid thirdBid = new Bid();
        thirdBid.setMember(three);

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

        ArgumentCaptor<List<TaskOwnership>> ownershipCaptor = ArgumentCaptor.forClass(List.class);
        verify(taskOwnershipRepository).saveAll(ownershipCaptor.capture());

        List<TaskOwnership> savedOwnerships = ownershipCaptor.getValue();
        assertEquals(2, savedOwnerships.size());
        assertTrue(savedOwnerships.stream().allMatch(ownership -> ownership.getAssignedPoints() == 80));
        assertTrue(savedOwnerships.stream().allMatch(
                ownership -> ownership.getAcquisitionType() == OwnershipAcquisitionType.BID
        ));
    }
}
