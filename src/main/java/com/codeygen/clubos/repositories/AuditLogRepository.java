package com.codeygen.clubos.repositories;

import com.codeygen.clubos.entities.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}
