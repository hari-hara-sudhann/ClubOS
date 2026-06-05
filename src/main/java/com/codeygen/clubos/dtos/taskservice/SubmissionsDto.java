package com.codeygen.clubos.dtos.taskservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "A single recorded submission for a task.")
public class SubmissionsDto {
    @Schema(description = "Unique identifier of the member who submitted the work.", example = "member-123")
    private String memberId;

    @Schema(description = "Proof submitted by the member.", example = "https://drive.google.com/file/d/abc123/view")
    private String proofOfSubmission;
}
