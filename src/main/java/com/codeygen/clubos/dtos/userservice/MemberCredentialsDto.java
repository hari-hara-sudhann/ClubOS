package com.codeygen.clubos.dtos.userservice;

import com.codeygen.clubos.utils.CollegeMail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Credential bundle returned after successful member onboarding.")
public class MemberCredentialsDto {
    @Schema(description = "Member register number.", example = "23CSE104")
    private String registerNumber;

    @Schema(description = "Member email address.", example = "hari@example.com")
    @CollegeMail
    private String email;

    @Schema(description = "Generated initial password that the member should change on first login.", example = "AB12CD34")
    private String password;
}
