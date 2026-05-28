package com.codeygen.clubos.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.userservice.BulkMemberImportDto;
import com.codeygen.clubos.dtos.userservice.MemberCredentialsDto;
import com.codeygen.clubos.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController("/api/panel")
@RequiredArgsConstructor
public class PanelController {
    private UserService userService;

    @PostMapping("/bulk_onboard")
    public ResponseEntity<List<MemberCredentialsDto>> importMembersInBulk(@RequestBody BulkMemberImportDto dto) {
        return ResponseEntity.ok(userService.bulkImportMembers(dto));
    }
}
