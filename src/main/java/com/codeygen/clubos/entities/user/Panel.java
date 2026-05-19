package com.codeygen.clubos.entities.user;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

@Entity
@Data
@PrimaryKeyJoinColumn(name = "user_id")
public class Panel extends User {

}
