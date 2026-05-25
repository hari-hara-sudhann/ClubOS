package com.codeygen.clubos.entities.user;

import com.codeygen.clubos.entities.Department;
import com.codeygen.clubos.entities.user.enums.CyclePerformanceStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private CyclePerformanceStatus performanceStatus = CyclePerformanceStatus.NO_TASKS_ASSIGNED;

    @ManyToOne
    @JoinColumn(name="department_id")
    private Department dept;
}
