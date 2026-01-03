package com.file.manager.repository;

import com.file.manager.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Additional custom methods if needed
}
