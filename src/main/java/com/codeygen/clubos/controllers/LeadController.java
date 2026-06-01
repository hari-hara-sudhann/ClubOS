package com.codeygen.clubos.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.services.LeadService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
}
