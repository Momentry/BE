package com.momentry.BE.global.handler;

import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.global.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기: 프로젝트 전역에서 발생하는 예외를 붙잡아 일관된 응답(ErrorResponse)을 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     * BusinessException을 상속받은 모든 커스텀 예외는 여기서 처리됩니다.
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException - Message: {}, Status: {}", e.getMessage(), e.getStatus());
        return ApiResponse.ofFail(e.getStatus(), e.getMessage());
    }

    /**
     * 시스템 예외 처리
     * 위에서 걸러지지 않은 예상치 못한 런타임 예외(500 에러)를 처리합니다.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return ApiResponse.ofError(500, "서버 내부에서 알 수 없는 에러가 발생했습니다.");
    }
}