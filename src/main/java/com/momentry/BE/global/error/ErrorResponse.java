package com.momentry.BE.global.error;

import com.momentry.BE.global.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;

/**
 * 에러 응답 규격: 클라이언트에게 전달할 에러 정보를 담는 공통 DTO입니다.
 */
@Getter
@Builder
public class ErrorResponse {
    private final int status;       // HTTP 상태 코드 (예: 400, 404, 500)
    private final String message;   // 에러의 원인을 설명하는 메시지

    public ErrorResponse(BusinessException e){
        this.status = e.getStatus();
        this.message = e.getMessage();
    }
}