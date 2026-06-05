package com.codeygen.clubos.dtos.taskservice;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload used by a lead to directly assign owners before bidding closes.")
public class OwnershipTaskDirectAssignmentDto {
    @Schema(description = "Unique identifier of the ownership-based task.", example = "task-ownership-456")
    private String taskId;

    @ArraySchema(
            schema = @Schema(description = "Unique identifier of a member who should become an owner.", example = "member-123"),
            arraySchema = @Schema(description = "Ordered list of member identifiers to assign as owners.")
    )
    private List<String> ownerIds;
}
