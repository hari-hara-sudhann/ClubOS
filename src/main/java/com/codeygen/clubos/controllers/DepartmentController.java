package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.departmentservice.DepartmentDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.services.DepartmentService;
import com.codeygen.clubos.dtos.common.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(
        name = "Department Administration (Internal)",
        description = "Endpoints for managing departments. WARNING: These endpoints expose raw Department entities."
)
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(
            summary = "Create a department",
            description = "Creates a new department. WARNING: Exposes Department entity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid payload.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody DepartmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    @Operation(
            summary = "Fetch all departments",
            description = "WARNING: Exposes raw Department entities."
    )
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @Operation(
            summary = "Fetch department by ID",
            description = "WARNING: Exposes raw Department entity."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(
            @Parameter(description = "ID of the department to fetch") @PathVariable String id
    ) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @Operation(
            summary = "Update department",
            description = "Updates department metadata. WARNING: Exposes Department entity."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @Parameter(description = "ID of the department to update") @PathVariable String id,
            @Valid @RequestBody DepartmentDto dto
    ) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @Operation(
            summary = "Delete department",
            description = "Permanently deletes a department record."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "ID of the department to delete") @PathVariable String id
    ) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
