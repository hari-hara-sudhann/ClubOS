package com.codeygen.clubos.entities.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String userId;

    @Column(unique = true)
    private String email;

    private String passwordHash;

    private String registerNumber;

    private LocalDateTime createdAt;

    private Role role;
}
