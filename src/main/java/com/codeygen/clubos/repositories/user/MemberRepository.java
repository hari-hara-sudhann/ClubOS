package com.codeygen.clubos.repositories.user;

import com.codeygen.clubos.entities.user.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String> {
    List<Member> findByDept_DepartmentId(String departmentId);
}
