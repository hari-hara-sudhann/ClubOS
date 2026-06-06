package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.auditservice.AuditLogRequestDto;
import com.codeygen.clubos.dtos.loginservice.LoginRequestDto;
import com.codeygen.clubos.dtos.loginservice.LoginResponseDto;
import com.codeygen.clubos.dtos.userservice.BulkMemberImportDto;
import com.codeygen.clubos.dtos.userservice.MemberCredentialsDto;
import com.codeygen.clubos.dtos.userservice.MemberImportDto;
import com.codeygen.clubos.dtos.userservice.UserDto;
import com.codeygen.clubos.dtos.userservice.MemberDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.user.Lead;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.entities.user.Panel;
import com.codeygen.clubos.entities.user.User;
import com.codeygen.clubos.entities.user.enums.Roles;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.user.LeadRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import com.codeygen.clubos.repositories.user.UserRepository;
import com.codeygen.clubos.utils.HashPassword;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final DepartmentRepository departmentRepo;
    private final HashPassword hashPassword;
    private final LeadRepository leadRepository;
    private final MemberProgressService memberProgressService;
    private final AuditLogService auditLogService;

    @Transactional
    public User createUser(UserDto dto) {
        log.info("Creating user: {}", dto.getEmail());
        User user;
        if (dto.getRole() == Roles.MEMBER) {
            user = new Member();
            if (dto instanceof MemberDto memberDto) {
                ((Member) user).setTokensAvailable(memberDto.getTokensAvailable() != null ? memberDto.getTokensAvailable() : 30);
                ((Member) user).setCumulativePoints(memberDto.getCumulativePoints() != null ? memberDto.getCumulativePoints() : 0);
                if (memberDto.getDepartmentId() != null) {
                    Department dept = departmentRepo.findById(memberDto.getDepartmentId())
                            .orElseThrow(() -> new NoSuchElementException("Department not found"));
                    ((Member) user).setDept(dept);
                }
            }
        } else if (dto.getRole() == Roles.LEAD) {
            user = new Lead();
        } else if (dto.getRole() == Roles.PANEL) {
            user = new Panel();
        } else {
            user = new User();
        }

        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setRegisterNumber(dto.getRegisterNumber());
        user.setRole(dto.getRole());
        user.setCreatedAt(LocalDateTime.now());
        user.setMustChangePassword(true);
        if (dto.getPassword() != null) {
            user.setPasswordHash(hashPassword.hashPassword(dto.getPassword()));
        }

        User saved = userRepo.save(user);
        auditLogService.record(AuditActionType.ENTITY_CREATED, "USER", saved.getUserId(), "Created user: " + saved.getEmail());
        return saved;
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepo.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "USER", "ALL", "Fetched all users");
        return users;
    }

    public User getUserById(String id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "USER", id, "Fetched user: " + user.getEmail());
        return user;
    }

    @Transactional
    public User updateUser(String id, UserDto dto) {
        log.info("Updating user {}: {}", id, dto.getEmail());
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setRegisterNumber(dto.getRegisterNumber());
        if (dto.getPassword() != null) {
            user.setPasswordHash(hashPassword.hashPassword(dto.getPassword()));
        }

        if (user instanceof Member member && dto instanceof MemberDto memberDto) {
            if (memberDto.getDepartmentId() != null) {
                Department dept = departmentRepo.findById(memberDto.getDepartmentId())
                        .orElseThrow(() -> new NoSuchElementException("Department not found"));
                member.setDept(dept);
            }
            if (memberDto.getTokensAvailable() != null) {
                member.setTokensAvailable(memberDto.getTokensAvailable());
            }
            if (memberDto.getCumulativePoints() != null) {
                member.setCumulativePoints(memberDto.getCumulativePoints());
            }
        }

        User updated = userRepo.save(user);
        auditLogService.record(AuditActionType.ENTITY_UPDATED, "USER", id, "Updated user: " + updated.getEmail());
        return updated;
    }

    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        userRepo.delete(user);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "USER", id, "Deleted user: " + user.getEmail());
    }

    @Transactional
    public List<MemberCredentialsDto> bulkImportMembers(BulkMemberImportDto dto) {
        List<MemberCredentialsDto> created = new ArrayList<>();

        if (dto == null || dto.getMembers() == null || dto.getMembers().isEmpty()) {
            return created;
        }

        Department dept = departmentRepo
                .findById(dto.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + dto.getDepartmentId()));

        for (MemberImportDto memDto : dto.getMembers()) {
            if (memDto == null) {
                continue;
            }

            String registerNumber = memDto.getRegisterNumber();
            String email = memDto.getEmail();
            String name = memDto.getName();
            if (registerNumber == null || email == null) {
                continue;
            }

            boolean userExists = userRepo.findByEmail(email).isPresent()
                    || userRepo.findByRegisterNumber(registerNumber).isPresent();
            if (userExists) {
                continue;
            }

            String rawPassword = generatePassword();
            String passwordHash = hashPassword.hashPassword(rawPassword);

            Member member = new Member();
            member.setDept(dept);

            member.setRegisterNumber(registerNumber);
            member.setEmail(email);
            member.setName(name);
            member.setPasswordHash(passwordHash);

            member.setRole(Roles.MEMBER);
            member.setCreatedAt(LocalDateTime.now());
            member.setMustChangePassword(true);
            member.setTokensAvailable(30);
            member.setCumulativePoints(0);

            memberRepo.save(member);

            MemberCredentialsDto credentialsDto = new MemberCredentialsDto();
            credentialsDto.setRegisterNumber(registerNumber);
            credentialsDto.setEmail(email);
            credentialsDto.setPassword(rawPassword);
            created.add(credentialsDto);
        }

        AuditLogRequestDto audit = new AuditLogRequestDto();
        audit.setActionType(AuditActionType.MEMBERS_BULK_IMPORTED);
        audit.setActorName("PANEL_API_CALLER");
        audit.setActorRole("PANEL");
        audit.setDepartmentId(dept.getDepartmentId());
        audit.setTargetType("MEMBER_BATCH");
        audit.setTargetId(dept.getDepartmentId());
        audit.setSummary("Panel bulk-imported " + created.size() + " members into department " + dept.getDepartmentId() + ".");
        audit.setDetails("Requested members: " + dto.getMembers().size() + ", created members: " + created.size() + ".");
        auditLogService.recordAction(audit);

        return created;
    }

    private String generatePassword() {
        SecureRandom pwd = new SecureRandom();
        StringBuilder password = new StringBuilder("");
        String alphaNumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890123456789012345";
        for (int i = 0; i < 8; i++)
            password.append(alphaNumeric.charAt(pwd.nextInt(alphaNumeric.length())));
        return password.toString();
    }

    @Transactional
    public void promoteToLead(String memberId) throws NoSuchElementException {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));

        Lead promotedMember = new Lead();
        promotedMember.setUserId(member.getUserId());
        promotedMember.setName(member.getName());
        promotedMember.setEmail(member.getEmail());
        promotedMember.setMustChangePassword(member.getMustChangePassword());
        promotedMember.setPasswordHash(member.getPasswordHash());
        promotedMember.setRegisterNumber(member.getRegisterNumber());
        promotedMember.setCreatedAt(LocalDateTime.now());

        promotedMember.setRole(Roles.LEAD);

        Department dept = departmentRepo.findById(member.getDept().getDepartmentId())
                .orElseThrow(() -> new NoSuchElementException("No such Department"));
        Lead currentLead = dept.getDeptLead();
        Member demotedMember = new Member();
        demotedMember.setUserId(currentLead.getUserId());
        demotedMember.setName(currentLead.getName());
        demotedMember.setEmail(currentLead.getEmail());
        demotedMember.setMustChangePassword(currentLead.getMustChangePassword());
        demotedMember.setPasswordHash(currentLead.getPasswordHash());
        demotedMember.setDept(dept);
        demotedMember.setTokensAvailable(30);
        demotedMember.setCumulativePoints(0);
        demotedMember.setRegisterNumber(currentLead.getRegisterNumber());
        demotedMember.setCreatedAt(LocalDateTime.now());

        leadRepository.deleteById(currentLead.getUserId());
        leadRepository.save(promotedMember);
        memberRepo.deleteById(member.getUserId());
        memberRepo.save(demotedMember);

        AuditLogRequestDto audit = new AuditLogRequestDto();
        audit.setActionType(AuditActionType.MEMBER_PROMOTED_TO_LEAD);
        audit.setActorName("PANEL_API_CALLER");
        audit.setActorRole("PANEL");
        audit.setDepartmentId(dept.getDepartmentId());
        audit.setTargetType("MEMBER");
        audit.setTargetId(member.getUserId());
        audit.setSummary("Panel promoted member " + member.getUserId() + " to lead for department " + dept.getDepartmentId() + ".");
        audit.setDetails("Previous lead " + currentLead.getUserId() + " was demoted to member.");
        auditLogService.recordAction(audit);
    }

    @Transactional
    public void changeMemberDepartment(String memberId, String toDepartmentId) throws NoSuchElementException, IllegalArgumentException {
        // All responsibilities of members in the previous department still holds.
        // New tasks are assigned by the later departments.
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("Member not found!"));
        String fromDepartmentId = member.getDept() == null ? null : member.getDept().getDepartmentId();

        Department toDepartment = departmentRepo.findById(toDepartmentId)
                .orElseThrow(() -> new NoSuchElementException("Department not found"));

        if (member.getDept() != null && member.getDept().equals(toDepartment)) {
            throw new IllegalArgumentException("Cannot be transferred to the same department");
        }

        member.setDept(toDepartment);
        memberRepo.save(member);
        memberProgressService.updateStatusForMember(member.getUserId());

        AuditLogRequestDto audit = new AuditLogRequestDto();
        audit.setActionType(AuditActionType.MEMBER_DEPARTMENT_CHANGED);
        audit.setActorName("PANEL_API_CALLER");
        audit.setActorRole("PANEL");
        audit.setDepartmentId(toDepartment.getDepartmentId());
        audit.setTargetType("MEMBER");
        audit.setTargetId(member.getUserId());
        audit.setSummary("Panel moved member " + member.getUserId() + " from department " + fromDepartmentId + " to " + toDepartment.getDepartmentId() + ".");
        audit.setDetails("Historical work remains unchanged; future tasks now belong to department " + toDepartment.getDepartmentId() + ".");
        auditLogService.recordAction(audit);
    }

    public LoginResponseDto userLogin(@NonNull LoginRequestDto dto) {
        User user = userRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NoSuchElementException("User not found!"));
        boolean correctCredentials = hashPassword.verifyPassword(dto.getRawPassword(), user.getPasswordHash());

        if (!correctCredentials) throw new NoSuchElementException("Invalid Password");

        LoginResponseDto response = new LoginResponseDto();
        response.setJwt("eyJthisisasamplejwt");
        response.setRole(user.getRole());
        return response;
    }
}
