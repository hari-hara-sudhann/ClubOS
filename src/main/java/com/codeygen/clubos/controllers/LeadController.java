package com.codeygen.clubos.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.GetSubmissionsDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskDirectAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.TaskReviewDto;
import com.codeygen.clubos.services.LeadService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/lead")
@RequiredArgsConstructor
public class LeadController {
    private final LeadService leadService;

    @PostMapping("/assign/general-task")
    public ResponseEntity<String> generalTaskAssignment(@RequestBody GeneralTaskAssignmentDto dto) {
        leadService.assignGeneralTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("General Task created.");
    }
    
    @PostMapping("/assign/ownership-task")
    public ResponseEntity<String> ownershipTaskAssignment(@RequestBody OwnershipTaskAssignmentDto dto) {
        leadService.assignOwnershipBasedTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Ownership based task created!");
    }
    
    @GetMapping("/tasks/{taskId}/submissions")
    public ResponseEntity<GetSubmissionsDto> getSubmissions(@PathVariable String taskId) {
        GetSubmissionsDto dto = leadService.getSubmissions(taskId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/review-submission")
    public ResponseEntity<String> reviewTask(@RequestBody TaskReviewDto dto) {
        leadService.reviewTask(dto);
        return ResponseEntity.ok("Review Successful.");
    }

    @PatchMapping("/assign-owners")
    public ResponseEntity<String> assignOwnersDirectly(@RequestBody OwnershipTaskDirectAssignmentDto dto) {
        leadService.assignOwnersDirectly(dto);
        return ResponseEntity.ok("Owners assigned directly");
    }
}