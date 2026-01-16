package com.momentry.BE.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert {

    @Id
    private Long userId;

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
    public Alert(User user, Boolean albumCreated, Boolean invited, Boolean fileUploaded){
        this.user = user;
        this.albumCreated = (albumCreated != null) ? albumCreated : true;
        this.invited = (invited != null) ? invited : true;
        this.fileUploaded = (fileUploaded != null) ? fileUploaded : true;
    }
}