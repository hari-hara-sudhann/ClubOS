package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String taskId;

    private String name;
    private Integer points;

    private LocalDateTime taskDeadline;

    private TaskTypes taskType;
}
