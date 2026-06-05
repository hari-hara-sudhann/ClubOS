package com.codeygen.clubos.dtos.loginservice;

import com.codeygen.clubos.entities.user.enums.Roles;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Temporary login response payload.")
public class LoginResponseDto {
    @Schema(description = "Placeholder JWT returned by the current login implementation.", example = "eyJthisisasamplejwt")
    private String jwt;

    @Schema(description = "Resolved role of the authenticated user.", example = "MEMBER")
    private Roles role;
}
