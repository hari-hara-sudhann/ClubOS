package com.codeygen.clubos.dtos.taskservice;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OwnershipTaskAssignmentDto {
    private String name;
    private Integer points;
    private String deptId;
    private LocalDateTime deadline;
    private LocalDateTime biddingDeadline;
    private Integer numberOfOwners;
}
