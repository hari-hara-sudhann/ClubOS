package com.codeygen.clubos.dtos.taskservice;

import lombok.Data;

import java.util.List;

@Data
public class GetSubmissionsDto {
    private String taskId;
    private List<SubmissionsDto> submissions;
}
