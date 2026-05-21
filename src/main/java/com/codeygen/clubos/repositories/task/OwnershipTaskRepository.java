package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.tasks.OwnershipBasedTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnershipTaskRepository extends JpaRepository<OwnershipBasedTask, String> {

}
