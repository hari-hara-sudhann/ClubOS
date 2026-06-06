package com.codeygen.clubos.dtos.userservice;

import com.codeygen.clubos.entities.user.enums.Roles;
import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String name;
    private String registerNumber;
    private Roles role;
    private String password;
}
