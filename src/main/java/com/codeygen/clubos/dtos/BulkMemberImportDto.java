package com.codeygen.clubos.dtos;

import lombok.Data;

import java.util.List;

@Data
public class BulkMemberImportDto {
    private String departmentId;
    private List<MemberImportDto> members;
}
