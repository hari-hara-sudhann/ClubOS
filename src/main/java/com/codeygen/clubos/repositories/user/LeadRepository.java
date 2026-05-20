package com.codeygen.clubos.repositories.user;

import com.codeygen.clubos.entities.user.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadRepository extends JpaRepository<Lead, String> {
}
