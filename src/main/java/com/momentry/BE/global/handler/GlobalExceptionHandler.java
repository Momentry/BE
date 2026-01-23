package com.momentry.BE.global.handler;

import com.momentry.BE.global.dto.ApiResponse;
import com.momentry.BE.global.exception.BusinessException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
     * RequestBody DTO(@Valid) 바인딩/검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("요청 값이 올바르지 않습니다.");

        log.debug("Validation failed(MethodArgumentNotValidException): {}", message);
        return ApiResponse.ofFail(400, message);
    }

    /**
     * @RequestParam, @PathVariable 등에 대한 제약조건(@NotBlank 등) 위반 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse("요청 값이 올바르지 않습니다.");

        log.debug("Validation failed(ConstraintViolationException): {}", message);
        return ApiResponse.ofFail(400, message);
    }

    /**
     * 필수 RequestParam 누락(MissingServletRequestParameterException) 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        String message = String.format("필수 요청 파라미터(%s)가 누락되었습니다.", e.getParameterName());
        log.debug("Missing request parameter: {}", e.getParameterName());
        return ApiResponse.ofFail(400, message);
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