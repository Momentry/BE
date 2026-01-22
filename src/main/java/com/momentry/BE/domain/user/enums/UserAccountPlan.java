package com.momentry.BE.domain.user.enums;

public enum UserAccountPlan {
    FREE_USER("freeUser"),
    PREMIUM_USER("premiumUser");

    private final String plan;

    UserAccountPlan(String plan) {
        this.plan = plan;
    }

    public String getPlan() { return plan; }
    public boolean isFree() { return this.equals(FREE_USER); }
}
