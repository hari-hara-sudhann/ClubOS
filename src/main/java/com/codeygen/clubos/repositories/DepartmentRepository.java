package com.codeygen.clubos.repositories;

import com.codeygen.clubos.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}
