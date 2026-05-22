package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.BulkMemberImportDto;
import com.codeygen.clubos.dtos.MemberCredentialsDto;
import com.codeygen.clubos.dtos.MemberImportDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.user.Member;
import com.codeygen.clubos.entities.user.enums.Roles;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.user.MemberRepository;
import com.codeygen.clubos.repositories.user.UserRepository;
import com.codeygen.clubos.utils.HashPassword;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final DepartmentRepository departmentRepo;
    private final HashPassword hashPassword;

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
}

