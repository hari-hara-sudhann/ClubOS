package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.tasks.enums.TaskTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
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
