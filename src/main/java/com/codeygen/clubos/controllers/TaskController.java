package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.taskservice.GeneralTaskAssignmentDto;
import com.codeygen.clubos.dtos.taskservice.OwnershipTaskAssignmentDto;
import com.codeygen.clubos.entities.tasks.Task;
import com.codeygen.clubos.services.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(
        name = "Task Administration (Internal)",
        description = "Endpoints for managing tasks. WARNING: These endpoints bypass governance creation rules and expose raw Task entities."
)
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Fetch all tasks",
            description = "WARNING: Exposes raw Task entities."
    )
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(
            summary = "Fetch task by ID",
            description = "WARNING: Exposes raw Task entity."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @Parameter(description = "ID of the task to fetch") @PathVariable String id
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(
            summary = "Update general task",
            description = "WARNING: Exposes raw Task entity."
    )
    @PutMapping("/general/{id}")
    public ResponseEntity<Task> updateGeneralTask(
            @Parameter(description = "ID of the task to update") @PathVariable String id,
            @Valid @RequestBody GeneralTaskAssignmentDto dto
    ) {
        return ResponseEntity.ok(taskService.updateGeneralTask(id, dto));
    }

    @Operation(
            summary = "Update ownership task",
            description = "WARNING: Exposes raw Task entity."
    )
    @PutMapping("/ownership-based/{id}")
    public ResponseEntity<Task> updateOwnershipTask(
            @Parameter(description = "ID of the task to update") @PathVariable String id,
            @Valid @RequestBody OwnershipTaskAssignmentDto dto
    ) {
        return ResponseEntity.ok(taskService.updateOwnershipTask(id, dto));
    }

    @Operation(
            summary = "Delete task",
            description = "Permanently deletes a task record."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "ID of the task to delete") @PathVariable String id
    ) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
