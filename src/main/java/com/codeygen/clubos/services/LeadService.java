package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GetSubmissionsDto;
import com.codeygen.clubos.dtos.taskservice.SubmissionsDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.tasks.GeneralTask;
import com.codeygen.clubos.entities.tasks.TaskSubmission;
import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.task.TaskRepository;
import com.codeygen.clubos.repositories.task.TaskSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final TaskRepository taskRepository;
    private final DepartmentRepository departmentRepository;
    private final TaskSubmissionRepository taskSubmissionRepository;

    public void assignGeneralTask(@NonNull GeneralTaskAssignmentDto dto) {
        GeneralTask task = new GeneralTask();
        task.setName(dto.getName());
        task.setPoints(dto.getPoints());
        task.setTaskDeadline(dto.getDeadline());
        Department dept = departmentRepository.findById(dto.getDeptId())
                        .orElseThrow(() -> new NoSuchElementException("Department Does not exist!"));
        task.setDept(dept);
        taskRepository.save(task);
    }

    public GetSubmissionsDto getSubmissions(String taskId) {
        taskRepository.findById(taskId).orElseThrow(() -> new NoSuchElementException("Task not found!"));
        List<TaskSubmission> submissions = taskSubmissionRepository.findAllByTask_TaskId(taskId);
        List<SubmissionsDto> dtos = new ArrayList<>();

        for (TaskSubmission submission: submissions) {
            SubmissionsDto submissionsDto = new SubmissionsDto();
            submissionsDto.setMemberId(submission.getMember().getUserId());
            submissionsDto.setProofOfSubmission(submission.getProofOfSubmission());
            dtos.add(submissionsDto);
        }
        GetSubmissionsDto dto = new GetSubmissionsDto();
        dto.setSubmissions(dtos);
        dto.setTaskId(taskId);
        return dto;
    }

    public void approveTask(String taskId, String userId) {
        TaskSubmission submission = taskSubmissionRepository.findByTask_TaskIdAndMember_UserId(taskId, userId)
                .orElseThrow(() -> new NoSuchElementException("submission not found!"));

        submission.setStatus(SubmissionStatus.APPROVED);
        taskSubmissionRepository.save(submission);
    }

    public void rejectTask(String taskId, String userId) {
        TaskSubmission submission = taskSubmissionRepository.findByTask_TaskIdAndMember_UserId(taskId, userId)
                .orElseThrow(() -> new NoSuchElementException("submission not found!"));

        submission.setStatus(SubmissionStatus.REJECTED);
        taskSubmissionRepository.save(submission);
    }
}
