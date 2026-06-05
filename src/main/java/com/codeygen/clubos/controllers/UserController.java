package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.common.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.loginservice.LoginRequestDto;
import com.codeygen.clubos.dtos.loginservice.LoginResponseDto;
import com.codeygen.clubos.services.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@Tag(
        name = "General User APIs",
        description = "Shared endpoints not tied to a single governance role. Authentication is still a placeholder and will later move into dedicated auth controllers."
)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Prototype login endpoint",
            description = "Temporary login endpoint used before the dedicated authentication module is introduced."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login request accepted."),
            @ApiResponse(
                    responseCode = "404",
                    description = "User was not found or credentials were rejected by the current placeholder implementation.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.userLogin(dto));
    }
}
