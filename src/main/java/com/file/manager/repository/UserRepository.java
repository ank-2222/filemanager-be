package com.file.manager.repository;

import com.file.manager.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Additional custom methods if needed
    User findByEmail(String email);



}
