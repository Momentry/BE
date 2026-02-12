package com.momentry.BE.domain.user.repository;

import com.momentry.BE.domain.user.entity.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
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
            AND u.id = :userId
            """)
    Optional<User> findActiveUserById(Long userId);

    @Query("""
            SELECT u
            FROM User u
            WHERE u.isActive = true
            AND u.id IN :userIds
            """)
    List<User> findAllActiveUserById(List<Long> userIds);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.isActive = true
          AND (
               LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY u.username ASC, u.email ASC, u.id ASC
    """)
    List<User> findActiveUsersByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT u
        FROM User u
        WHERE u.isActive = true
          AND (
               LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (
                u.username > :cursorUsername
             OR (u.username = :cursorUsername AND u.email > :cursorEmail)
             OR (u.username = :cursorUsername AND u.email = :cursorEmail AND u.id > :cursorId)
          )
        ORDER BY u.username ASC, u.email ASC, u.id ASC
    """)
    List<User> findActiveUsersByKeywordWithCursor(@Param("keyword") String keyword,
                                                  @Param("cursorUsername") String cursorUsername,
                                                  @Param("cursorEmail") String cursorEmail,
                                                  @Param("cursorId") Long cursorId,
                                                  Pageable pageable);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
