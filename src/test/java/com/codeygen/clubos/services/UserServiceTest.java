package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.BulkMemberImportDto;
import com.codeygen.clubos.dtos.MemberCredentialsDto;
import com.codeygen.clubos.dtos.MemberImportDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.user.Lead;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.entities.user.enums.Roles;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.user.LeadRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import com.codeygen.clubos.repositories.user.UserRepository;
import com.codeygen.clubos.utils.HashPassword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private MemberRepository memberRepo;

    @Mock
    private DepartmentRepository departmentRepo;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private HashPassword hashPassword;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldBulkImportMembersSuccessfully() {

        Department department = new Department();
        department.setDepartmentId("dept-1");
        department.setName("Web Dev");

        when(departmentRepo.findById("dept-1"))
                .thenReturn(Optional.of(department));

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.empty());

        when(userRepo.findByRegisterNumber(any()))
                .thenReturn(Optional.empty());

        when(hashPassword.hashPassword(any()))
                .thenReturn("hashed-password");

        MemberImportDto memberDto = new MemberImportDto();
        memberDto.setName("Hari");
        memberDto.setEmail("hari@example.com");
        memberDto.setRegisterNumber("21CS101");

        BulkMemberImportDto bulkDto = new BulkMemberImportDto();
        bulkDto.setDepartmentId("dept-1");
        bulkDto.setMembers(List.of(memberDto));

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(bulkDto);

        assertEquals(1, result.size());

        MemberCredentialsDto credentials = result.get(0);

        assertEquals("hari@example.com", credentials.getEmail());
        assertEquals("21CS101", credentials.getRegisterNumber());

        verify(memberRepo, times(1))
                .save(any(Member.class));

        ArgumentCaptor<Member> memberCaptor =
                ArgumentCaptor.forClass(Member.class);

        verify(memberRepo).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();

        assertEquals("Hari", savedMember.getName());

        assertEquals("hari@example.com",
                savedMember.getEmail());

        assertEquals("21CS101",
                savedMember.getRegisterNumber());

        assertEquals(Roles.MEMBER,
                savedMember.getRole());

        assertEquals(30,
                savedMember.getTokensAvailable());

        assertEquals(0,
                savedMember.getCumulativePoints());

        assertTrue(savedMember.getMustChangePassword());

        assertEquals("hashed-password",
                savedMember.getPasswordHash());

        assertEquals(department,
                savedMember.getDept());
    }

    @Test
    void shouldReturnEmptyListWhenDtoIsNull() {

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(null);

        assertTrue(result.isEmpty());

        verifyNoInteractions(memberRepo);
    }

    @Test
    void shouldReturnEmptyListWhenMembersListIsEmpty() {

        BulkMemberImportDto dto =
                new BulkMemberImportDto();

        dto.setDepartmentId("dept-1");
        dto.setMembers(List.of());

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(dto);

        assertTrue(result.isEmpty());

        verifyNoInteractions(memberRepo);
    }

    @Test
    void shouldThrowExceptionWhenDepartmentNotFoundInMember() {

        MemberImportDto memberDto = new MemberImportDto();
        memberDto.setName("Hari");
        memberDto.setEmail("hari@example.com");
        memberDto.setRegisterNumber("21CS101");

        BulkMemberImportDto dto =
                new BulkMemberImportDto();

        dto.setDepartmentId("invalid-dept");
        dto.setMembers(List.of(memberDto));

        when(departmentRepo.findById("invalid-dept"))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.bulkImportMembers(dto)
        );
    }

    @Test
    void shouldSkipDuplicateUsers() {

        Department department = new Department();

        when(departmentRepo.findById(any()))
                .thenReturn(Optional.of(department));

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(new Member()));

        MemberImportDto memberDto =
                new MemberImportDto();

        memberDto.setName("Hari");
        memberDto.setEmail("duplicate@example.com");
        memberDto.setRegisterNumber("21CS101");

        BulkMemberImportDto bulkDto =
                new BulkMemberImportDto();

        bulkDto.setDepartmentId("dept-1");
        bulkDto.setMembers(List.of(memberDto));

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(bulkDto);

        assertTrue(result.isEmpty());

        verify(memberRepo, never()).save(any());
    }

    @Test
    void shouldSkipMemberWhenEmailIsNull() {

        Department department = new Department();

        when(departmentRepo.findById(any()))
                .thenReturn(Optional.of(department));

        MemberImportDto memberDto =
                new MemberImportDto();

        memberDto.setName("Hari");
        memberDto.setEmail(null);
        memberDto.setRegisterNumber("21CS101");

        BulkMemberImportDto bulkDto =
                new BulkMemberImportDto();

        bulkDto.setDepartmentId("dept-1");
        bulkDto.setMembers(List.of(memberDto));

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(bulkDto);

        assertTrue(result.isEmpty());

        verify(memberRepo, never()).save(any());
    }

    @Test
    void shouldSkipNullMemberDto() {

        Department department = new Department();

        when(departmentRepo.findById(any()))
                .thenReturn(Optional.of(department));

        BulkMemberImportDto bulkDto =
                new BulkMemberImportDto();

        bulkDto.setDepartmentId("dept-1");
        bulkDto.setMembers(Collections.singletonList(null));

        List<MemberCredentialsDto> result =
                userService.bulkImportMembers(bulkDto);

        assertTrue(result.isEmpty());

        verify(memberRepo, never()).save(any());
    }
    @Test
    void shouldPromoteMemberToLeadSuccessfully() {

        // Arrange

        String memberId = "member-1";

        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member member = new Member();
        member.setUserId(memberId);
        member.setName("Hari");
        member.setEmail("hari@example.com");
        member.setPasswordHash("hashed-password");
        member.setMustChangePassword(true);
        member.setDept(department);

        Lead currentLead = new Lead();
        currentLead.setUserId("lead-1");
        currentLead.setName("Arjun");
        currentLead.setEmail("arjun@example.com");
        currentLead.setPasswordHash("lead-password");
        currentLead.setMustChangePassword(false);

        department.setDeptLead(currentLead);

        when(memberRepo.findById(memberId))
                .thenReturn(Optional.of(member));

        when(departmentRepo.findById("dept-1"))
                .thenReturn(Optional.of(department));

        // Act

        userService.promoteToLead(memberId);

        // Assert

        verify(leadRepository, times(1))
                .save(any(Lead.class));

        verify(memberRepo, times(1))
                .save(any(Member.class));

        verify(leadRepository, times(1))
                .deleteById("lead-1");

        verify(memberRepo, times(1))
                .deleteById(memberId);

        ArgumentCaptor<Lead> leadCaptor =
                ArgumentCaptor.forClass(Lead.class);

        verify(leadRepository).save(leadCaptor.capture());

        Lead savedLead = leadCaptor.getValue();

        assertEquals(memberId, savedLead.getUserId());
        assertEquals("Hari", savedLead.getName());
        assertEquals("hari@example.com", savedLead.getEmail());
        assertEquals(Roles.LEAD, savedLead.getRole());
    }

    @Test
    void shouldThrowExceptionWhenMemberNotFound() {

        when(memberRepo.findById("invalid-member"))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> userService.promoteToLead("invalid-member")
        );

        verifyNoInteractions(leadRepository);
    }


    @Test
    void shouldCreateDemotedMemberFromCurrentLead() {

        Department department = new Department();
        department.setDepartmentId("dept-1");

        Member promotedMember = new Member();
        promotedMember.setUserId("member-1");
        promotedMember.setDept(department);

        Lead currentLead = new Lead();
        currentLead.setUserId("lead-1");
        currentLead.setName("Current Lead");
        currentLead.setEmail("lead@example.com");
        currentLead.setPasswordHash("hashed");
        currentLead.setMustChangePassword(false);

        department.setDeptLead(currentLead);

        when(memberRepo.findById("member-1"))
                .thenReturn(Optional.of(promotedMember));

        when(departmentRepo.findById("dept-1"))
                .thenReturn(Optional.of(department));

        userService.promoteToLead("member-1");

        ArgumentCaptor<Member> memberCaptor =
                ArgumentCaptor.forClass(Member.class);

        verify(memberRepo).save(memberCaptor.capture());

        Member demotedMember = memberCaptor.getValue();

        assertEquals("lead-1", demotedMember.getUserId());
        assertEquals("Current Lead", demotedMember.getName());
        assertEquals("lead@example.com", demotedMember.getEmail());

        assertEquals(30,
                demotedMember.getTokensAvailable());

        assertEquals(0,
                demotedMember.getCumulativePoints());
    }
    @Test
    void shouldChangeMemberDepartmentSuccessfully() {

        // Arrange

        Department oldDepartment = new Department();
        oldDepartment.setDepartmentId("dept-old");

        Department newDepartment = new Department();
        newDepartment.setDepartmentId("dept-new");

        Member member = new Member();
        member.setUserId("member-1");
        member.setDept(oldDepartment);

        when(memberRepo.findById("member-1"))
                .thenReturn(Optional.of(member));

        when(departmentRepo.findById("dept-new"))
                .thenReturn(Optional.of(newDepartment));

        // Act

        userService.changeMemberDepartment(
                "member-1",
                "dept-new"
        );

        // Assert

        assertEquals(
                newDepartment,
                member.getDept()
        );

        verify(memberRepo, times(1))
                .save(member);
    }

    @Test
    void shouldThrowExceptionWhenMemberNotFoundWhileChangingDepartment() {

        // Arrange

        when(memberRepo.findById("invalid-member"))
                .thenReturn(Optional.empty());

        // Act + Assert

        assertThrows(
                NoSuchElementException.class,
                () -> userService.changeMemberDepartment(
                        "invalid-member",
                        "dept-new"
                )
        );

        verify(memberRepo, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDepartmentNotFoundWhileChangingDepartment() {

        // Arrange

        Member member = new Member();
        member.setUserId("member-1");

        when(memberRepo.findById("member-1"))
                .thenReturn(Optional.of(member));

        when(departmentRepo.findById("invalid-dept"))
                .thenReturn(Optional.empty());

        // Act + Assert

        assertThrows(
                NoSuchElementException.class,
                () -> userService.changeMemberDepartment(
                        "member-1",
                        "invalid-dept"
                )
        );

        verify(memberRepo, never()).save(any());
    }
}