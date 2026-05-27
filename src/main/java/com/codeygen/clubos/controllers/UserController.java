package com.codeygen.clubos.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.loginservice.LoginRequestDto;
import com.codeygen.clubos.dtos.loginservice.LoginResponseDto;
import com.codeygen.clubos.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController("/api/user")
@RequiredArgsConstructor
public class UserController {

    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(userService.userLogin(dto));
    }
}
