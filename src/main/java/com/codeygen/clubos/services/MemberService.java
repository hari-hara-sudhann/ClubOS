package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.memberservice.BidPlacementDto;
import com.codeygen.clubos.dtos.memberservice.TaskSubmissionDto;
import com.codeygen.clubos.entities.bid.Bid;
import com.codeygen.clubos.entities.bid.enums.BidStatus;
import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.OwnershipAssignmentStatus;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.repositories.BidRepository;
import com.codeygen.clubos.repositories.task.OwnershipTaskRepository;
import com.codeygen.clubos.repositories.task.TaskOwnershipRepository;
import com.codeygen.clubos.repositories.task.TaskRepository;
import com.codeygen.clubos.repositories.task.TaskSubmissionRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final TaskRepository taskRepository;
    private final OwnershipTaskRepository ownershipTaskRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskOwnershipRepository taskOwnershipRepository;
    private final BidRepository bidRepository;

    public int getRemainingTokensForBidding(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));
        return getAvailableTokens(member);
    }

    @Transactional
    public void submitTask(@NonNull TaskSubmissionDto dto) {
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Task not found!"));

        validateSubmission(dto);
        validateOwnershipSubmission(member, task);

        TaskSubmission submission = taskSubmissionRepository
                .findByTask_TaskIdAndMember_UserId(dto.getTaskId(), dto.getMemberId())
                .orElseGet(TaskSubmission::new);

        if (submission.getSubmissionId() != null && submission.getStatus() == SubmissionStatus.APPROVED) {
            throw new IllegalStateException("Approved submissions cannot be edited.");
        }

        submission.setTask(task);
        submission.setMember(member);
        submission.setProofOfSubmission(dto.getProofOfSubmission());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setRemarksByLead(null);

        taskSubmissionRepository.save(submission);
    }

    @Transactional
    public void placeBidOnOwnershipBasedTask(@NonNull BidPlacementDto dto) {
        OwnershipBasedTask task = ownershipTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Ownership task not found!"));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));

        validateBidPlacement(dto, task, member);

        Bid bid = new Bid();
        bid.setTask(task);
        bid.setMember(member);
        bid.setTokensBidded(dto.getTokensBidded());
        bid.setStatus(BidStatus.PENDING);
        bid.setRefunded(false);

        int availableTokens = getAvailableTokens(member);
        member.setTokensAvailable(availableTokens - dto.getTokensBidded());

        memberRepository.save(member);
        bidRepository.save(bid);
    }

    @Transactional
    public void updateBidOnOwnershipBasedTask(@NonNull BidPlacementDto dto) {
        OwnershipBasedTask task = ownershipTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new NoSuchElementException("Ownership task not found!"));
        Member member = memberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));
        Bid existingBid = bidRepository.findByTask_TaskIdAndMember_UserId(dto.getTaskId(), dto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("Existing bid not found!"));

        validateBidUpdate(dto, task, member, existingBid);

        int currentTokens = getAvailableTokens(member);
        int previousBid = existingBid.getTokensBidded() == null ? 0 : existingBid.getTokensBidded();
        int delta = dto.getTokensBidded() - previousBid;

        member.setTokensAvailable(currentTokens - delta);
        existingBid.setTokensBidded(dto.getTokensBidded());
        existingBid.setStatus(BidStatus.PENDING);
        existingBid.setRefunded(false);

        memberRepository.save(member);
        bidRepository.save(existingBid);
    }

    private void validateSubmission(TaskSubmissionDto dto) {
        if (dto.getProofOfSubmission() == null || dto.getProofOfSubmission().isBlank()) {
            throw new IllegalArgumentException("Proof of submission is required.");
        }
    }

    private void validateOwnershipSubmission(Member member, Task task) {
        if (task.getTaskType() != TaskTypes.OWNERSHIP_BASED && !(task instanceof OwnershipBasedTask)) {
            return;
        }

        taskOwnershipRepository.findByTask_TaskIdAndOwner_UserId(task.getTaskId(), member.getUserId())
                .orElseThrow(() -> new IllegalStateException("Only assigned owners can submit ownership tasks."));
    }

    private void validateBidPlacement(BidPlacementDto dto, OwnershipBasedTask task, Member member) {
        validateBidTokens(dto.getTokensBidded());

        if (task.getAssignmentStatus() != OwnershipAssignmentStatus.BIDDING_OPEN) {
            throw new IllegalStateException("Bidding is no longer open for this task.");
        }

        if (!task.getBiddingDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Bidding deadline has already passed.");
        }

        if (member.getDept() == null || !member.getDept().getDepartmentId().equals(task.getDept().getDepartmentId())) {
            throw new IllegalArgumentException("Member must belong to the same department as the task.");
        }

        if (bidRepository.existsByTask_TaskIdAndMember_UserId(dto.getTaskId(), dto.getMemberId())) {
            throw new IllegalStateException("Member has already placed a bid for this task.");
        }

        int availableTokens = getAvailableTokens(member);
        if (availableTokens < dto.getTokensBidded()) {
            throw new IllegalStateException("Member does not have enough tokens. Remaining tokens: " + availableTokens);
        }
    }

    private void validateBidUpdate(BidPlacementDto dto, OwnershipBasedTask task, Member member, Bid existingBid) {
        validateBidTokens(dto.getTokensBidded());

        if (task.getAssignmentStatus() != OwnershipAssignmentStatus.BIDDING_OPEN) {
            throw new IllegalStateException("Bidding is no longer open for this task.");
        }

        if (!task.getBiddingDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Bidding deadline has already passed.");
        }

        if (member.getDept() == null || !member.getDept().getDepartmentId().equals(task.getDept().getDepartmentId())) {
            throw new IllegalArgumentException("Member must belong to the same department as the task.");
        }

        if (existingBid.getStatus() != BidStatus.PENDING) {
            throw new IllegalStateException("Only pending bids can be updated.");
        }

        int currentTokens = getAvailableTokens(member);
        int previousBid = existingBid.getTokensBidded() == null ? 0 : existingBid.getTokensBidded();
        int additionalTokensRequired = dto.getTokensBidded() - previousBid;
        if (additionalTokensRequired > currentTokens) {
            throw new IllegalStateException(
                    "Member does not have enough tokens to increase the bid. Remaining tokens: " + currentTokens
            );
        }
    }

    private void validateBidTokens(Integer tokensBidded) {
        if (tokensBidded == null || tokensBidded <= 0) {
            throw new IllegalArgumentException("Bid tokens must be positive.");
        }
    }

    private int getAvailableTokens(Member member) {
        return member.getTokensAvailable() == null ? 0 : member.getTokensAvailable();
    }
}
