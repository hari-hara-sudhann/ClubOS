package com.codeygen.clubos.dtos.userservice;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Request payload used to onboard multiple members into a department in one operation.")
public class BulkMemberImportDto {
    @Schema(description = "Unique identifier of the department into which the members should be imported.", example = "dept-design")
    private String departmentId;

    @ArraySchema(schema = @Schema(implementation = MemberImportDto.class))
    private List<MemberImportDto> members;
}
