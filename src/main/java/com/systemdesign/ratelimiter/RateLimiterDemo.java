package com.systemdesign.ratelimiter;

/**
 * Demonstration of all rate limiting algorithms with comparative examples
 */
public class RateLimiterDemo {

    public static void main(String[] args) {
        System.out.println("=== Rate Limiter Algorithms Demo ===\n");

        // Common settings: 5 requests per 10 seconds
        int maxRequests = 5;
        long windowSizeMs = 10000; // 10 seconds

        System.out.println("Scenario: 5 requests allowed per 10 seconds");
        System.out.println("We'll simulate 8 rapid requests to see how each algorithm handles it\n");

        demoFixedWindowCounter(maxRequests, windowSizeMs);
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoSlidingWindowLog(maxRequests, windowSizeMs);
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoSlidingWindowCounter(maxRequests, windowSizeMs);
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoTokenBucket(maxRequests, windowSizeMs);
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoLeakyBucket(maxRequests, windowSizeMs);
    }

    private static void demoFixedWindowCounter(int maxRequests, long windowSizeMs) {
        System.out.println("1. FIXED WINDOW COUNTER");
        System.out.println("- Divides time into fixed intervals");
        System.out.println("- Simple but has boundary spike issue\n");

        FixedWindowCounter limiter = new FixedWindowCounter(maxRequests, windowSizeMs);
        String userId = "user1";
        long startTime = System.currentTimeMillis();

        System.out.println("Sending 8 requests at t=0ms:");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime);
            System.out.printf("  Request %d: %s (count: %d)\n", i, allowed ? "ALLOWED" : "DENIED", 
                limiter.getRequestCount(userId, startTime));
        }

        System.out.println("\nAfter window passes (t=10000ms):");
        for (int i = 1; i <= 3; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime + windowSizeMs + 100);
            System.out.printf("  Request %d: %s\n", i, allowed ? "ALLOWED" : "DENIED");
        }
    }

    private static void demoSlidingWindowLog(int maxRequests, long windowSizeMs) {
        System.out.println("2. SLIDING WINDOW LOG");
        System.out.println("- Tracks exact timestamps of each request");
        System.out.println("- Most accurate but higher memory usage\n");

        SlidingWindowLog limiter = new SlidingWindowLog(maxRequests, windowSizeMs);
        String userId = "user1";
        long startTime = System.currentTimeMillis();

        System.out.println("Sending 8 requests at t=0ms:");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime);
            System.out.printf("  Request %d: %s (count: %d)\n", i, allowed ? "ALLOWED" : "DENIED",
                limiter.getRequestCount(userId, startTime));
        }

        System.out.println("\nRequests spread over 5 seconds:");
        for (int i = 0; i < 5; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime + (i + 1) * 1000);
            System.out.printf("  Request at t=%dms: %s\n", (i + 1) * 1000, allowed ? "ALLOWED" : "DENIED");
        }
    }

    private static void demoSlidingWindowCounter(int maxRequests, long windowSizeMs) {
        System.out.println("3. SLIDING WINDOW COUNTER");
        System.out.println("- Hybrid approach with weighted previous window");
        System.out.println("- Better accuracy than fixed window, less memory than log\n");

        SlidingWindowCounter limiter = new SlidingWindowCounter(maxRequests, windowSizeMs);
        String userId = "user1";
        long startTime = System.currentTimeMillis();

        System.out.println("Sending 8 requests at t=0ms:");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime);
            System.out.printf("  Request %d: %s (count: %.0f)\n", i, allowed ? "ALLOWED" : "DENIED",
                limiter.getRequestCount(userId, startTime));
        }

        System.out.println("\nAfter 5 seconds, send more requests:");
        for (int i = 1; i <= 3; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime + 5000 + i * 100);
            System.out.printf("  Request at t=%dms: %s (count: %.0f)\n", 5000 + i * 100, 
                allowed ? "ALLOWED" : "DENIED", limiter.getRequestCount(userId, startTime + 5000 + i * 100));
        }
    }

    private static void demoTokenBucket(int maxRequests, long windowSizeMs) {
        System.out.println("4. TOKEN BUCKET");
        System.out.println("- Tokens added at constant rate, burst allowed up to capacity");
        System.out.println("- Industry standard for APIs\n");

        double tokensPerSecond = (double) maxRequests / (windowSizeMs / 1000.0);
        TokenBucket limiter = new TokenBucket(maxRequests, tokensPerSecond);
        String userId = "user1";
        long startTime = System.currentTimeMillis();

        System.out.printf("Capacity: %d tokens, Refill rate: %.2f tokens/second\n\n", maxRequests, tokensPerSecond);

        System.out.println("Sending 8 requests immediately:");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime);
            System.out.printf("  Request %d: %s (tokens available: %.2f)\n", i, allowed ? "ALLOWED" : "DENIED",
                limiter.getAvailableTokens(userId, startTime));
        }

        System.out.println("\nWait 3 seconds, then send 3 requests:");
        for (int i = 1; i <= 3; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime + 3000 + i * 100);
            System.out.printf("  Request at t=%dms: %s (tokens: %.2f)\n", 3000 + i * 100,
                allowed ? "ALLOWED" : "DENIED", limiter.getAvailableTokens(userId, startTime + 3000 + i * 100));
        }
    }

    private static void demoLeakyBucket(int maxRequests, long windowSizeMs) {
        System.out.println("5. LEAKY BUCKET");
        System.out.println("- Requests leak out at constant rate");
        System.out.println("- Smooth traffic, useful for shaping\n");

        double leakRatePerSecond = (double) maxRequests / (windowSizeMs / 1000.0);
        LeakyBucket limiter = new LeakyBucket(maxRequests, leakRatePerSecond);
        String userId = "user1";
        long startTime = System.currentTimeMillis();

        System.out.printf("Capacity: %d requests, Leak rate: %.2f requests/second\n\n", maxRequests, leakRatePerSecond);

        System.out.println("Sending 8 requests immediately:");
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime);
            System.out.printf("  Request %d: %s (queue size: %d)\n", i, allowed ? "ALLOWED" : "DENIED",
                limiter.getQueueSize(userId));
        }

        System.out.println("\nWait 3 seconds, then send 3 more requests:");
        for (int i = 1; i <= 3; i++) {
            boolean allowed = limiter.allowRequest(userId, startTime + 3000 + i * 100);
            System.out.printf("  Request at t=%dms: %s (queue size: %d)\n", 3000 + i * 100,
                allowed ? "ALLOWED" : "DENIED", limiter.getQueueSize(userId));
        }
    }
}
