package com.codeygen.clubos.dtos.userservice;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemberDto extends UserDto {
    private String departmentId;
    private Integer tokensAvailable;
    private Integer cumulativePoints;
}
