package com.codeygen.clubos.dtos.userservice;

import com.codeygen.clubos.utils.CollegeMail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Single member entry used in a bulk onboarding request.")
public class MemberImportDto {
    @Schema(description = "Full name of the member.", example = "Harihara Sudhan")
    private String name;

    @Schema(description = "Register number used as a unique academic identifier.", example = "23CSE104")
    private String registerNumber;

    @Schema(description = "Email address used for onboarding and login.", example = "hari@example.com")
    @CollegeMail
    private String email;
}
