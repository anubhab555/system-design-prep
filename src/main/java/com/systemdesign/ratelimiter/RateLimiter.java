package com.systemdesign.ratelimiter;

/**
 * Abstract interface for rate limiting algorithms.
 * All rate limiter implementations should follow this contract.
 */
public interface RateLimiter {
    /**
     * Checks if a request is allowed based on the rate limiting algorithm.
     * @param userId Unique identifier for the user making the request
     * @param currentTime Current timestamp in milliseconds
     * @return true if request is allowed, false if request should be denied
     */
    boolean allowRequest(String userId, long currentTime);

    /**
     * Resets the state for a specific user (useful for testing)
     * @param userId User identifier to reset
     */
    void reset(String userId);
}
