package com.momentry.BE.domain.user.repository;

import com.momentry.BE.domain.user.entity.AlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertPreferenceRepository extends JpaRepository<AlertPreference, Long> {
    Optional<AlertPreference> findByUserId(Long userId);
}
