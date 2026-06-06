package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.common.ApiErrorResponse;
import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GetSubmissionsDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskDirectAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.TaskReviewDto;
import com.codeygen.clubos.services.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lead")
@RequiredArgsConstructor
@Tag(
        name = "Lead APIs",
        description = "Endpoints used by department leads to assign tasks, review submissions, and resolve ownership assignments."
)
public class LeadController {
    private final LeadService leadService;

    @Operation(
            summary = "Create a general department task",
            description = "Assigns a standard task to a department with the specified point value and optional deadline."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "General task created successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task points or payload values are invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/tasks/general")
    public ResponseEntity<Void> generalTaskAssignment(@Valid @RequestBody GeneralTaskAssignmentDto dto) {
        leadService.assignGeneralTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Create an ownership-based task",
            description = "Assigns an ownership-based task to a department, including the number of owners to choose and the bidding deadline."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ownership-based task created successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task points, number of owners, or deadlines are invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/tasks/ownership-based")
    public ResponseEntity<Void> ownershipTaskAssignment(@Valid @RequestBody OwnershipTaskAssignmentDto dto) {
        leadService.assignOwnershipBasedTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Fetch submissions for a task",
            description = "Returns the list of member submissions that have been made for the specified task."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Submissions fetched successfully."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/tasks/{taskId}/submissions")
    public ResponseEntity<GetSubmissionsDto> getSubmissions(
            @Parameter(description = "Unique identifier of the task.", example = "task-123")
            @PathVariable String taskId
    ) {
        return ResponseEntity.ok(leadService.getSubmissions(taskId));
    }

    @Operation(
            summary = "Review a member submission",
            description = "Approves or rejects a submitted task. The same endpoint is also used to correct an earlier review decision."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Submission review recorded successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Review status or payload is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Submission was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The review cannot be completed in the current state, such as approving an ownership task for a non-owner.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PatchMapping("/tasks/submissions/review")
    public ResponseEntity<Void> reviewTask(@Valid @RequestBody TaskReviewDto dto) {
        leadService.reviewTask(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Directly assign owners before bidding closes",
            description = "Lets a lead bypass open bidding and assign the owners explicitly before the bidding deadline. The task points are automatically reduced to 60 percent of the original points."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Owners assigned directly and bidding closed for the task."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Owner list or task data is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ownership task or one of the requested members was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The ownership assignment cannot be changed because bidding is already closed or the deadline has passed.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PatchMapping("/tasks/ownership-based/direct-owner-assignment")
    public ResponseEntity<Void> assignOwnersDirectly(@Valid @RequestBody OwnershipTaskDirectAssignmentDto dto) {
        leadService.assignOwnersDirectly(dto);
        return ResponseEntity.ok().build();
    }
}
