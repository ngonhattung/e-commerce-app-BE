package com.nhattung.authservice.repository;

import com.nhattung.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsernameOrEmailOrPhone(String username, String email, String phone);

    boolean existsByEmail(String email);
}
