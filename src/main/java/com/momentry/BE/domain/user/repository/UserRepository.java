package com.momentry.BE.domain.user.repository;

import com.momentry.BE.domain.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        SELECT u
        FROM User u
        WHERE u.isActive = true
          AND (
               LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY u.username ASC, u.email ASC
    """)
    List<User> findActiveUsersByKeyword(@Param("keyword") String keyword);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
