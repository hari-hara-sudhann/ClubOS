package com.codeygen.clubos.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.codeygen.clubos.dtos.userservice.UserDto;
import com.codeygen.clubos.entities.user.User;
import com.codeygen.clubos.dtos.loginservice.LoginRequestDto;
import com.codeygen.clubos.dtos.loginservice.LoginResponseDto;
import com.codeygen.clubos.services.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "User Administration (Internal)",
        description = "Administrative endpoints for user management. WARNING: These endpoints bypass governance workflows (like panel onboarding) and expose raw entities. Use for data correction only."
)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "User login (Placeholder)",
            description = "Authenticates a user and returns a token. This is a temporary implementation."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.userLogin(dto));
    }

    @Operation(
            summary = "Fetch all users",
            description = "WARNING: Exposes raw User entities, including password hashes. Use with caution."
    )
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(
            summary = "Fetch user by ID",
            description = "WARNING: Exposes raw User entity."
    )
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to fetch") @PathVariable String id
    ) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
            summary = "Update user directly",
            description = "WARNING: Bypasses governance role/department rules. Exposes User entity."
    )
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable String id,
            @Valid @RequestBody UserDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @Operation(
            summary = "Delete user",
            description = "Permanently deletes a user record."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable String id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
