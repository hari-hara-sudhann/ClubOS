package com.codeygen.clubos.dtos.taskservice;

import lombok.Data;

import java.util.List;

@Data
public class OwnershipTaskDirectAssignmentDto {
    private String taskId;
    private List<String> ownerIds;
}
