package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskId;

    private String name;
    private Integer points;

    private LocalDateTime taskDeadline;

    @Enumerated(EnumType.STRING)
    private TaskTypes taskType;

    @ManyToOne
    @JoinColumn(name="department_id")
    private Department dept;
}
