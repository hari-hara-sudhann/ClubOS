package com.codeygen.clubos.dtos.loginservice;

import com.codeygen.clubos.entities.user.enums.Roles;

import lombok.Data;

@Data
public class LoginResponseDto {
    private String jwt;
    private Roles role;
}
