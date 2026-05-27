package com.codeygen.clubos.dtos.memberservice;

import lombok.Data;

@Data
public class BidPlacementDto {
    private String memberId;
    private String taskId;
    private Integer tokensBidded;
}
