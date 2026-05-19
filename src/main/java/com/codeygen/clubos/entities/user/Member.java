package com.codeygen.clubos.entities.user;

import com.codeygen.clubos.entities.Department;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

@Entity
@Data
@PrimaryKeyJoinColumn(name = "user_id")
public class Member extends User {
    private Integer tokensAvailable;
    private Integer cumulativePoints;

    @ManyToOne
    @JoinColumn(name="department_id")
    private Department dept;
}