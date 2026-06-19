package com.codeygen.clubos.entities.user;

import com.codeygen.clubos.entities.user.enums.Roles;
import com.codeygen.clubos.utils.CollegeMail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String userId;

    @Column(unique = true)
    @CollegeMail
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
