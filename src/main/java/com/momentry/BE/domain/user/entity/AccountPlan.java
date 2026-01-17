package com.momentry.BE.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Table(name = "account_plan")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String plan;

    @Builder
    public AccountPlan(String plan) {
        // 유효성 체크 추가
        Assert.hasText(plan, "플랜 이름은 필수입니다.");

        this.plan = plan;
    }
}
