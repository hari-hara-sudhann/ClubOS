package com.codeygen.clubos.entities;

import com.codeygen.clubos.entities.user.Lead;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
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
