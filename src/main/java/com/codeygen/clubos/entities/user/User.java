package com.codeygen.clubos.entities.user;

import com.codeygen.clubos.entities.user.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    private String name;

    private String passwordHash;

    @Column(unique = true)
    private String registerNumber;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Roles role;

    private Boolean mustChangePassword = true;
}
