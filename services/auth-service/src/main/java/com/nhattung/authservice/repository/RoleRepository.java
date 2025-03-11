package com.nhattung.authservice.repository;

import com.nhattung.authservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(String userRole);

    boolean existsByName(String name);
}
