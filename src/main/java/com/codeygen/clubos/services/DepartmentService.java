package com.codeygen.clubos.services;

import com.codeygen.clubos.dtos.departmentservice.DepartmentDto;
import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.audit.enums.AuditActionType;
import com.codeygen.clubos.entities.user.Lead;
import com.codeygen.clubos.repositories.DepartmentRepository;
import com.codeygen.clubos.repositories.user.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final LeadRepository leadRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public Department createDepartment(DepartmentDto dto) {
        log.info("Creating department: {}", dto.getName());
        Department department = new Department();
        department.setName(dto.getName());
        if (dto.getDeptLeadId() != null) {
            Lead lead = leadRepository.findById(dto.getDeptLeadId())
                    .orElseThrow(() -> new NoSuchElementException("Lead not found"));
            department.setDeptLead(lead);
        }
        Department saved = departmentRepository.save(department);

        auditLogService.record(AuditActionType.ENTITY_CREATED, "DEPARTMENT", saved.getDepartmentId(), 
                "Created department: " + saved.getName());
        
        return saved;
    }

    public List<Department> getAllDepartments() {
        log.info("Fetching all departments");
        List<Department> departments = departmentRepository.findAll();
        auditLogService.record(AuditActionType.ENTITY_READ, "DEPARTMENT", "ALL", "Fetched all departments");
        return departments;
    }

    public Department getDepartmentById(String id) {
        log.info("Fetching department by id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        auditLogService.record(AuditActionType.ENTITY_READ, "DEPARTMENT", id, "Fetched department: " + department.getName());
        return department;
    }

    @Transactional
    public Department updateDepartment(String id, DepartmentDto dto) {
        log.info("Updating department {}: {}", id, dto.getName());
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        department.setName(dto.getName());
        if (dto.getDeptLeadId() != null) {
            Lead lead = leadRepository.findById(dto.getDeptLeadId())
                    .orElseThrow(() -> new NoSuchElementException("Lead not found"));
            department.setDeptLead(lead);
        } else {
            department.setDeptLead(null);
        }
        Department updated = departmentRepository.save(department);

        auditLogService.record(AuditActionType.ENTITY_UPDATED, "DEPARTMENT", id, "Updated department: " + updated.getName());
        return updated;
    }

    @Transactional
    public void deleteDepartment(String id) {
        log.info("Deleting department: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found"));
        departmentRepository.delete(department);
        auditLogService.record(AuditActionType.ENTITY_DELETED, "DEPARTMENT", id, "Deleted department: " + department.getName());
    }
}
