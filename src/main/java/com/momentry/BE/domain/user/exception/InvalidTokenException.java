package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class InvalidTokenException extends BusinessException {
    
    public InvalidTokenException() {
        super("Invalid token", 400);
    }
    
    public InvalidTokenException(String message) {
        super(message, 400);
    }
}