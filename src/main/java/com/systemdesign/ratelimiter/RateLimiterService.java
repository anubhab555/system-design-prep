package com.systemdesign.ratelimiter;

import java.util.logging.Logger;

/**
 * Orchestrates rate limiting by delegating to a pluggable RateLimiter strategy.
 * Equivalent to LoadBalancer in the load balancer package.
 *
 * Clients depend only on this class, never on concrete algorithm implementations.
 * The strategy can be swapped at runtime without changing client code.
 */
public class RateLimiterService {

    private static final Logger logger = Logger.getLogger(RateLimiterService.class.getName());

    private static RateLimiterService instance;

    private static RateLimiterStrategy rateLimiterStrategy;

    private RateLimiterService() {}

    public static synchronized RateLimiterService getInstance() {
        if (instance == null) {
            instance = new RateLimiterService(); // Default strategy
        }
        return instance;
    }

    public void setRateLimiterStrategy(RateLimiterStrategy strategy) {
        rateLimiterStrategy = strategy;
    }

    public void handleRequest(String userId) {
        if (rateLimiterStrategy.allowRequest(userId)) {
            logger.info("Request Allowed for user: " + userId);
        } else {
            logger.info("Request Denied for user: " + userId);
        }
    }
}
