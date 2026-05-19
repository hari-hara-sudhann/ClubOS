package com.codeygen.clubos.entities;

import com.codeygen.clubos.entities.user.Lead;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String departmentId;

    private String name;

    @OneToOne
    @JoinColumn(name = "user_id")
    private Lead deptLead;
}
