package com.codeygen.clubos.repositories.task;

import com.codeygen.clubos.entities.tasks.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByDept_DepartmentId(String departmentId);
}
