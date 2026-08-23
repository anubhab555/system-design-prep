package com.systemdesign.developer_tools.ratelimiter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Demonstration of all rate limiting algorithms with comparative examples
 */
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("=== Rate Limiter Algorithms Demo ===");

        // Common settings: 5 requests per 10 seconds
        int maxRequests = 5;
        long windowSizeMs = 10000; // 10 seconds

        logger.info("Scenario: 5 requests allowed per 10 seconds");
        logger.info("We'll simulate 8 rapid requests to see how each algorithm handles it");

        demoFixedWindowCounter(maxRequests, windowSizeMs);
        logger.info("=".repeat(70));

        demoSlidingWindowLog(maxRequests, windowSizeMs);
        logger.info("=".repeat(70));

        demoSlidingWindowCounter(maxRequests, windowSizeMs);
        logger.info("=".repeat(70));

        demoTokenBucket(5, 1);
        logger.info("=".repeat(70));

        demoLeakyBucket(maxRequests, windowSizeMs);
    }

    private static void demoFixedWindowCounter(int maxRequests, long windowSizeMs) {
        logger.info("1. FIXED WINDOW COUNTER");
        logger.info("- Divides time into fixed intervals");
        logger.info("- Simple but has boundary spike issue");

        RateLimiterStrategy fixedWindowLimiter = new FixedWindowStrategy(maxRequests, windowSizeMs);
        RateLimiterService service = RateLimiterService.getInstance();
        service.setRateLimiterStrategy(fixedWindowLimiter);
        String userId = "user1";

        logger.info("Sending 10 requests");
        for (int i = 1; i <= 10; i++) {
            service.handleRequest(userId);
        }
    }

    private static void demoSlidingWindowLog(int maxRequests, long windowSizeMs) {
        logger.info("2. SLIDING WINDOW LOG");
        logger.info("- Tracks exact timestamps of each request");
        logger.info("- Most accurate but higher memory usage");

        SlidingWindowLogStrategy limiter = new SlidingWindowLogStrategy(maxRequests, windowSizeMs);
        String userId = "user1";

        logger.info("Sending 8 requests:");
        for (int i = 1; i <= 8; i++) {
            logger.log(Level.INFO, "  Request {0}: {1}", new Object[]{i, limiter.allowRequest(userId) ? "ALLOWED" : "DENIED"});
        }
    }

    private static void demoSlidingWindowCounter(int maxRequests, long windowSizeMs) {
        logger.info("3. SLIDING WINDOW COUNTER");
        logger.info("- Hybrid approach with weighted previous window");
        logger.info("- Better accuracy than fixed window, less memory than log");

        SlidingWindowStrategy limiter = new SlidingWindowStrategy(maxRequests, windowSizeMs);
        String userId = "user1";

        logger.info("Sending 8 requests:");
        for (int i = 1; i <= 8; i++) {
            logger.info(String.format("  Request %d: %s", i, limiter.allowRequest(userId) ? "ALLOWED" : "DENIED"));
        }
    }

    private static void demoTokenBucket(int capacity, int refillRate) {
        logger.info("4. TOKEN BUCKET");
        logger.info("- Tokens added at constant rate, burst allowed up to capacity");
        logger.info("- Industry standard for APIs");

        String userId = "user1";

        RateLimiterStrategy tokenBucketStrategy = new TokenBucketStrategy(capacity, refillRate);
        RateLimiterService service = RateLimiterService.getInstance();
        service.setRateLimiterStrategy(tokenBucketStrategy);

        for (int i = 0; i < 10; i++) {
            service.handleRequest(userId);
        }

    }

    private static void demoLeakyBucket(int maxRequests, long windowSizeMs) {
        logger.info("5. LEAKY BUCKET");
        logger.info("- Requests leak out at constant rate");
        logger.info("- Smooth traffic, useful for shaping");

        int leakRatePerSecond = maxRequests;
        LeakyBucketStrategy limiter = new LeakyBucketStrategy(maxRequests, leakRatePerSecond);
        String userId = "user1";

        logger.info("Sending 8 requests immediately:");
        for (int i = 1; i <= 8; i++) {
            logger.log(Level.INFO, "  Request {0}: {1}", new Object[]{i, limiter.allowRequest(userId) ? "ALLOWED" : "DENIED"});
        }
    }
}
