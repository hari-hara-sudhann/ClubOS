package com.codeygen.clubos.dtos.memberservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload used by a member to submit proof of work for a task.")
public class TaskSubmissionDto {
    @Schema(description = "Unique identifier of the submitting member.", example = "member-123")
    private String memberId;

    @Schema(description = "Unique identifier of the task being submitted.", example = "task-123")
    private String taskId;

    @Schema(
            description = "Link, note, or textual proof showing the work that was completed.",
            example = "https://drive.google.com/file/d/abc123/view"
    )
    private String proofOfSubmission;
}
