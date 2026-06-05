package com.codeygen.clubos.dtos.taskservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request payload used by a lead to create an ownership-based task.")
public class OwnershipTaskAssignmentDto {
    @Schema(description = "Human-readable task title.", example = "Build the club landing page")
    private String name;

    @Schema(description = "Base points available for the task before any direct-assignment reduction.", example = "100")
    private Integer points;

    @Schema(description = "Unique identifier of the department receiving the task.", example = "dept-tech")
    private String deptId;

    @Schema(description = "Optional final completion deadline for the task.", example = "2026-06-30T18:00:00")
    private LocalDateTime deadline;

    @Schema(description = "Date-time after which automatic bid resolution should run.", example = "2026-06-15T20:00:00")
    private LocalDateTime biddingDeadline;

    @Schema(description = "Number of owners that should be selected for this task.", example = "3")
    private Integer numberOfOwners;
}
