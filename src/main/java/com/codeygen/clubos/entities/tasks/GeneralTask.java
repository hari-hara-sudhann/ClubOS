package com.codeygen.clubos.entities.tasks;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table
@PrimaryKeyJoinColumn(name="task_id")
public class GeneralTask extends Task {
}
