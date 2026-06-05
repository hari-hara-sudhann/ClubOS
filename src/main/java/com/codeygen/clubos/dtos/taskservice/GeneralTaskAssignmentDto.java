package com.codeygen.clubos.dtos.taskservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request payload used by a lead to assign a general department task.")
public class GeneralTaskAssignmentDto {
    @Schema(description = "Human-readable task title.", example = "Design poster for orientation week")
    private String name;

    @Schema(description = "Total points assigned to the task.", example = "20")
    private Integer points;

    @Schema(description = "Unique identifier of the department receiving the task.", example = "dept-design")
    private String deptId;

    @Schema(description = "Optional deadline by which the task should be completed.", example = "2026-06-20T18:00:00")
    private LocalDateTime deadline;
}
