package com.momentry.BE.domain.user.exception;

import com.momentry.BE.global.exception.BusinessException;

public class AccountPlanNotFoundException extends BusinessException {
    public AccountPlanNotFoundException() {
        super("존재하지 않는 구독 플랜입니다.", 400);
    }
}
