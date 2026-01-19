package com.momentry.BE.global.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


/**
 * API 응답 규격: 클라이언트에게 전달할 정보를 담는 공통 DTO입니다.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final String state;
    private final T data;

    // 중복되는 응답 생성 로직을 하나로 합친 private 메서드
    private static <T> ApiResponse<T> createResponse(int code, String message, String state, T data){
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .state(state)
                .data(data)
                .build();
    }

    // 성공 응답 (데이터가 있는 경우)
    public static <T> ResponseEntity<ApiResponse<T>> ofSuccess(T data){
        return ResponseEntity.ok(createResponse(200, "요청에 성공하였습니다.", "SUCCESS", data));
    }

    // 성공 응답 (데이터가 없는 경우)
    public static <T> ResponseEntity<ApiResponse<T>> ofSuccess(){
        return ofSuccess(null);
    }

    // 성공 응답 (Http 상태 코드를 커스텀하고, 데이터가 있는 경우)
    public static <T> ResponseEntity<ApiResponse<T>> ofSuccess(HttpStatus statusCode, T data) {
        return ResponseEntity.status(statusCode).body(createResponse(statusCode.value(), "요청에 성공하였습니다.", "SUCCESS", data));
    }

    // 성공 응답 (Http 상태 코드를 커스텀하고, 데이터가 없는 경우)
    public static <T> ResponseEntity<ApiResponse<T>> ofSuccess(HttpStatus statusCode) {
        return ofSuccess(statusCode, null);
    }

    // 요청 실패 응답 (4xx 계열)
    public static ResponseEntity<ApiResponse<Void>> ofFail(int statusCode, String message) {
        return ResponseEntity.status(statusCode).body(createResponse(statusCode, message, "FAIL", null));
    }

    // 서버 에러 응답 (5xx 계열)
    public static ResponseEntity<ApiResponse<Void>> ofError(int statusCode, String message){
        return ResponseEntity.status(statusCode).body(createResponse(statusCode, message, "ERROR", null));
    }
}