package com.dcaguard.exception;

public class PlanLimitExceededException extends BusinessException {
    public PlanLimitExceededException(String resource, int limit) {
        super("PLAN_LIMIT_EXCEEDED",
              String.format("You have reached the maximum of %d %s for your plan. Upgrade to Pro for unlimited access.", limit, resource));
    }
}
