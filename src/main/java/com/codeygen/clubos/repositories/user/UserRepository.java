package com.codeygen.clubos.repositories.user;

import com.codeygen.clubos.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRegisterNumber(String registerNumber);
}
