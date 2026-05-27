package com.codeygen.clubos.dtos.loginservice;

import com.codeygen.clubos.entities.user.enums.Roles;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String rawPassword;
    private Roles role;
}
