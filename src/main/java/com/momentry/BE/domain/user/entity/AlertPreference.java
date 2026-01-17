package com.momentry.BE.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Entity
@Table(name = "alerts")
@Getter
//@AllArgsConstructor(access = AccessLevel.PRIVATE) // 직접 만든 생성자와 충돌나서 주석처리 했습니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertPreference {

    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // users 테이블의 PK를 alerts 테이블의 PK로 사용
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Boolean albumCreated;

    @Column(nullable = false)
    private Boolean invited;

    @Column(nullable = false)
    private Boolean fileUploaded;

    @Builder
    public AlertPreference(User user, Boolean albumCreated, Boolean invited, Boolean fileUploaded){
        // 유효성 체크: 필수 값이 없으면 객체 생성 자체를 막음
        Assert.notNull(user, "사용자는 필수 값입니다.");

        this.user = user;
        this.albumCreated = (albumCreated != null) ? albumCreated : true;
        this.invited = (invited != null) ? invited : true;
        this.fileUploaded = (fileUploaded != null) ? fileUploaded : true;
    }
}