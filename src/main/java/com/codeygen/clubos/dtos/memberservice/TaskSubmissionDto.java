package com.codeygen.clubos.dtos.memberservice;

import lombok.Data;

@Data
public class TaskSubmissionDto {
    private String memberId;
    private String taskId;
    private String proofOfSubmission;
}