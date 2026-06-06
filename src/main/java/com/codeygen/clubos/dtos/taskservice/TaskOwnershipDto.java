package com.codeygen.clubos.dtos.taskservice;

import com.codeygen.clubos.entities.tasks.enums.OwnershipAcquisitionType;
import lombok.Data;

@Data
public class TaskOwnershipDto {
    private String taskId;
    private String ownerId;
    private Integer assignedPoints;
    private OwnershipAcquisitionType acquisitionType;
}
