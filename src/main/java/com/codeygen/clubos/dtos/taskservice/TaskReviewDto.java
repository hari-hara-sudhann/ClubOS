package com.codeygen.clubos.dtos.taskservice;

import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import lombok.Data;

@Data
public class TaskReviewDto {
    private String taskId;
    private String memberId;
    private SubmissionStatus status;
    private String remarks;
}
