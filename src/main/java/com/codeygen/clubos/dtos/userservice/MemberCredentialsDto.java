package com.codeygen.clubos.dtos.userservice;

import lombok.Data;

@Data
public class MemberCredentialsDto {
    private String registerNumber;
    private String email;
    private String password;
}
