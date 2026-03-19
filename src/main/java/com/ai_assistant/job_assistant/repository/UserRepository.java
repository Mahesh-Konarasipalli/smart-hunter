package com.ai_assistant.job_assistant.repository;

import com.ai_assistant.job_assistant.entity.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

   @Modifying
    @Transactional
    @Query("UPDATE AppUser u SET u.verified = true, u.otpCode = null WHERE u.email = :email")
    int verifyUserEmail(@Param("email") String email);
}
