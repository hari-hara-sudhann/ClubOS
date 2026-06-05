package com.codeygen.clubos.dtos.taskservice;

import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload used by a lead to approve or reject a submission.")
public class TaskReviewDto {
    @Schema(description = "Unique identifier of the task being reviewed.", example = "task-123")
    private String taskId;

    @Schema(description = "Unique identifier of the member whose submission is being reviewed.", example = "member-123")
    private String memberId;

    @Schema(
            description = "Review outcome. Only APPROVED or REJECTED are valid review values.",
            example = "APPROVED"
    )
    private SubmissionStatus status;

    @Schema(description = "Optional remarks left by the lead during review.", example = "Good work. Please tighten the final copy next time.")
    private String remarks;
}
