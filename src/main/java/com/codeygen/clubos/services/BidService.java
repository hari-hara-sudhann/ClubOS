package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.memberservice.BidPlacementDto;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.BidRepository;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final MemberRepository memberRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Bid createBid(BidPlacementDto dto) {
        log.info("Creating bid for member {} on task {}", dto.getMemberId(), dto.getTaskId());
        OwnershipBasedTask task = ownershipTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found"));

        Bid bid = new Bid();
        bid.setTask(task);
        bid.setMember(member);
        bid.setTokensBidded(dto.getTokensBidded());
        bid.setStatus(BidStatus.PENDING);
        bid.setRefunded(false);

        Bid saved = bidRepository.save(bid);
        auditLogService.record(AuditActionType.ENTITY_CREATED, "BID", saved.getBidId(), "Created bid for member: " + member.getUserId());
        return saved;
    }

    public List<Bid> getAllBids() {
        log.info("Fetching all bids");
        List<Bid> bids = bidRepository.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "BID", "ALL", "Fetched all bids");
        return bids;
    }

    public Bid getBidById(String id) {
        log.info("Fetching bid by id: {}", id);
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bid not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "BID", id, "Fetched bid: " + id);
        return bid;
    }

    @Transactional
    public Bid updateBid(String id, BidPlacementDto dto) {
        log.info("Updating bid {}: tokens {}", id, dto.getTokensBidded());
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bid not found"));
        
        if (dto.getTokensBidded() != null) {
            bid.setTokensBidded(dto.getTokensBidded());
        }
        // Other fields from dto can be updated if needed, but BidPlacementDto is limited.

        Bid updated = bidRepository.save(bid);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "BID", id, "Updated bid: " + id);
        return updated;
    }

    @Transactional
    public void deleteBid(String id) {
        log.info("Deleting bid: {}", id);
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bid not found"));
        bidRepository.delete(bid);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "BID", id, "Deleted bid: " + id);
    }
}
