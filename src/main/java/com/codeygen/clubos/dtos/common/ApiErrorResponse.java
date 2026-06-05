package com.codeygen.clubos.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Standard error response returned when a request cannot be completed.")
public class ApiErrorResponse {

    @Schema(description = "HTTP status code returned by the API.", example = "404")
    private int status;

    @Schema(description = "Short HTTP error name.", example = "Not Found")
    private String error;

    @Schema(description = "Human-readable explanation of the failure.", example = "Task not found!")
    private String message;

    @Schema(description = "Request path that produced the error.", example = "/api/lead/tasks/task-123/submissions")
    private String path;

    @Schema(description = "Timestamp when the error response was generated.", example = "2026-06-05T10:15:30")
    private LocalDateTime timestamp;
}
