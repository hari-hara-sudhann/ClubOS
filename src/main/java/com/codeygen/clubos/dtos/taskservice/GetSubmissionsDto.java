package com.codeygen.clubos.dtos.taskservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Response payload containing all recorded submissions for a task.")
public class GetSubmissionsDto {
    @Schema(description = "Unique identifier of the task whose submissions were fetched.", example = "task-123")
    private String taskId;

    @Schema(description = "List of submissions currently recorded for the task.")
    private List<SubmissionsDto> submissions;
}
