package com.momentry.BE.domain.user.repository;

import com.momentry.BE.domain.user.entity.AccountPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountPlanRepository extends JpaRepository<AccountPlan, Long> {
    Optional<AccountPlan> findByPlan(String plan);
}
