package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.tasks.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, String> {
}
