package com.codeygen.clubos.dtos.memberservice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload used to create or update a member's bid on an ownership-based task.")
public class BidPlacementDto {
    @Schema(description = "Unique identifier of the member placing the bid.", example = "member-123")
    private String memberId;

    @Schema(description = "Unique identifier of the ownership-based task.", example = "task-ownership-456")
    private String taskId;

    @Schema(description = "Number of tokens the member wants to commit to the bid.", example = "12")
    private Integer tokensBidded;
}
