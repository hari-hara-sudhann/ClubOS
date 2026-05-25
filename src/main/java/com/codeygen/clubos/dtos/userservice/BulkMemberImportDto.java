package com.codeygen.clubos.dtos.userservice;

import lombok.Data;

import java.util.List;

@Data
public class BulkMemberImportDto {
    private String departmentId;
    private List<MemberImportDto> members;
}
