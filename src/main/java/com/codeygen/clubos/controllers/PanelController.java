package com.codeygen.clubos.controllers;

import com.codeygen.clubos.dtos.common.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codeygen.clubos.dtos.userservice.BulkMemberImportDto;
import com.codeygen.clubos.dtos.userservice.MemberCredentialsDto;
import com.codeygen.clubos.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@Tag(
        name = "Panel APIs",
        description = "Governance endpoints used by panel members for onboarding, promotions, and department-transfer workflows."
)
@RequestMapping("/api/panel")
@RequiredArgsConstructor
public class PanelController {
    private final UserService userService;

    @Operation(
            summary = "Bulk onboard members into a department",
            description = "Imports a batch of members into the given department, generates their initial passwords, and returns the credentials that must be distributed securely."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk import processed successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "The request payload is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Department was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/members/bulk-onboard")
    public ResponseEntity<List<MemberCredentialsDto>> importMembersInBulk(@RequestBody BulkMemberImportDto dto) {
        return ResponseEntity.ok(userService.bulkImportMembers(dto));
    }

    @Operation(
            summary = "Promote a member to lead",
            description = "Promotes a member to department lead and demotes the current lead back to member within the same department."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion completed successfully."),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or department was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PatchMapping("/members/{memberId}/promote-to-lead")
    public ResponseEntity<Void> promoteToLead(
            @Parameter(description = "Unique identifier of the member to promote.", example = "member-123")
            @PathVariable String memberId
    ) {
        userService.promoteToLead(memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Transfer a member to another department",
            description = "Moves a member to a new department without rewriting historical task or ownership records from earlier departments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department transfer completed successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Transfer request is invalid, such as requesting the same department.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or target department was not found.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PatchMapping("/members/{memberId}/department/{departmentId}")
    public ResponseEntity<Void> changeDept(
            @Parameter(description = "Unique identifier of the member to transfer.", example = "member-123")
            @PathVariable String memberId,
            @Parameter(description = "Unique identifier of the destination department.", example = "dept-design")
            @PathVariable String departmentId
    ) {
        userService.changeMemberDepartment(memberId, departmentId);
        return ResponseEntity.ok().build();
    }
}
