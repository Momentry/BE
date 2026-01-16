package com.momentry.BE.domain.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String username;

    // OAuth
    private String provider;
    private String providerId;

    // Profile
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String fcmToken;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean isActive = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_plan_id", referencedColumnName = "id", nullable = false)
    private AccountPlan accountPlan;

    // 양방향 일대일 매핑
    // mappedBy : 연관관계의 주인이 아닌 쪽에 작성 (상대 엔티티인 Alert의 필드명(User user) 적기)
    // 일대일 양방향 매핑의 주인이 아닌 쪽은 프록시 객체의 한계때문에 LAZY로 설정해도 무시되고 즉시 로딩 된다
    // 그럼 이걸 제외하는게 좋을지? 아니면 감수해야할지 고민
    // 만약 양방향으로 간다면, 순환참조 문제가 없게끔 조심해야 한다
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private AlertPreference alert;

    @Builder
    public User(String email, String username, String provider, String providerId,
                String profileImageUrl, AccountPlan accountPlan) {
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.hasText(email, "이메일은 필수 값입니다.");
        Assert.hasText(username, "사용자 이름은 필수 값입니다.");
        Assert.notNull(accountPlan, "구독 플랜은 필수 값입니다.");

        this.email = email;
        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.accountPlan = accountPlan;
    }
}