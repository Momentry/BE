package com.momentry.BE.domain.user.controller;

import com.momentry.BE.global.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Object>> healthCheck(){
        return ApiResponse.ofSuccess();
    }
}

