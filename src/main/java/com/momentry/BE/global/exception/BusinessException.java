package com.momentry.BE.global.exception;

import lombok.Getter;

/**
 * 프로젝트 최상위 예외 클래스: 모든 비즈니스 예외는 이 클래스를 상속받아 구현합니다.
 *
 * [ 커스텀 예외 클래스 생성 예시 ]
 * public class UserNotFoundException extends BusinessException {
 *     public UserNotFoundException() {
 *         super("해당 사용자를 찾을 수 없습니다.", 404);
 *     }
 * }
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int status;

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }
}