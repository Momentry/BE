package com.momentry.BE.domain.user.repository;

import com.momentry.BE.domain.user.entity.AlertPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertPreferenceRepository extends JpaRepository<AlertPreference, Long> {
}
