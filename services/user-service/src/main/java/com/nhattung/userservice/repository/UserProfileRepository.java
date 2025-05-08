package com.nhattung.userservice.repository;

import com.nhattung.userservice.dto.MonthlyRegistrationDto;
import com.nhattung.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile,Long> {
    Optional<UserProfile> findByUserId(String userId);
    @Query(value = "SELECT YEAR(created_at) as year, MONTH(created_at) as month,\n" +
            "               COUNT(*) as count\n" +
            "               FROM ms_user_db.user_profile\n" +
            "               WHERE created_at >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)\n" +
            "               GROUP BY year, month\n" +
            "               ORDER BY year ASC, month ASC",
            nativeQuery = true)
    List<MonthlyRegistrationDto> getMonthlyRegistrations();

    List<UserProfile> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(String likePattern, String likePattern1, String likePattern2);

    List<UserProfile> findByFullNameContainingIgnoreCase(String fullName);

    List<UserProfile> findByEmailContainingIgnoreCase(String email);

    List<UserProfile> findByPhoneContainingIgnoreCase(String phone);

    Optional<UserProfile> findByEmail(String email);
}
