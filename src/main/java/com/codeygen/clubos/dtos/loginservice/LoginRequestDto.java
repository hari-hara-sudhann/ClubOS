package com.codeygen.clubos.dtos.loginservice;

import com.codeygen.clubos.entities.user.enums.Roles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Temporary login request payload.")
public class LoginRequestDto {
    @Schema(description = "Email address used by the user to log in.", example = "hari@example.com")
    private String email;

    @Schema(description = "Raw password provided by the user.", example = "AB12CD34")
    private String rawPassword;

    @Schema(description = "Declared role of the user. This field is currently informational in the placeholder login flow.", example = "MEMBER")
    private Roles role;
}
