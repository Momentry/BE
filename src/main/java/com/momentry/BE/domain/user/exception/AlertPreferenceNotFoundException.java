package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AlertPreferenceNotFoundException extends BusinessException {
    public AlertPreferenceNotFoundException() {
        super("존재하지 않는 알람 설정입니다.", 400);
    }
}
