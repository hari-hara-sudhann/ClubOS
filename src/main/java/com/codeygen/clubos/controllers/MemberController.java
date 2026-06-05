package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.common.ApiErrorResponse;
import com.codeygen.clubos.dtos.memberservice.BidPlacementDto;
import com.codeygen.clubos.dtos.memberservice.TaskSubmissionDto;
import com.codeygen.clubos.services.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(
        name = "Member APIs",
        description = "Endpoints used by club members to submit work, place bids on ownership-based tasks, and inspect their remaining bidding capacity."
)
public class MemberController {

    private final MemberService memberService;

    @Operation(
            summary = "Submit or resubmit task work",
            description = "Creates a new submission for a task or replaces an existing non-approved submission. Ownership-based tasks can only be submitted by assigned owners."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task submission recorded successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Submission payload is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or task was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Submission is not allowed in the current state, such as editing an approved submission or submitting an ownership task without ownership.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/tasks/submissions")
    public ResponseEntity<Void> submitTask(@RequestBody TaskSubmissionDto dto) {
        memberService.submitTask(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Place a bid on an ownership-based task",
            description = "Creates a pending bid for a member. The bid immediately reserves tokens from the member's remaining bidding balance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bid created successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bid payload is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or ownership task was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bidding is closed, the member already has a bid, or there are not enough remaining tokens.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/ownership-tasks/bids")
    public ResponseEntity<Void> placeBid(@RequestBody BidPlacementDto dto) {
        memberService.placeBidOnOwnershipBasedTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Update an existing ownership-task bid",
            description = "Replaces the token amount for an existing pending bid. Increasing a bid consumes additional remaining tokens; lowering a bid releases tokens back to the member."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bid updated successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bid payload is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member, ownership task, or existing bid was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Bid cannot be updated in the current state, such as when bidding is closed or the member lacks enough remaining tokens.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PatchMapping("/ownership-tasks/bids")
    public ResponseEntity<Void> updateBid(@RequestBody BidPlacementDto dto) {
        memberService.updateBidOnOwnershipBasedTask(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get remaining bid tokens for a member",
            description = "Returns the number of tokens the member can still commit across currently unresolved ownership-task bids."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Remaining bidding balance fetched successfully.",
                    content = @Content(schema = @Schema(implementation = Integer.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{memberId}/bidding/remaining-tokens")
    public ResponseEntity<Integer> getRemainingTokens(
            @Parameter(description = "Unique identifier of the member.", example = "member-123")
            @PathVariable String memberId
    ) {
        return ResponseEntity.ok(memberService.getRemainingTokensForBidding(memberId));
    }
}
