package com.systemdesign.ratelimiter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for all rate limiting algorithms
 * Tests basic functionality, edge cases, and boundary conditions
 */
public class RateLimiterTest {

    @Test
    public void testFixedWindowCounter() {
        FixedWindowCounter limiter = new FixedWindowCounter(3, 1000);
        long time = System.currentTimeMillis();

        // First 3 requests should be allowed
        assertTrue(limiter.allowRequest("user1", time));
        assertTrue(limiter.allowRequest("user1", time + 100));
        assertTrue(limiter.allowRequest("user1", time + 200));

        // 4th request should be denied
        assertFalse(limiter.allowRequest("user1", time + 300));

        // After window expires, should allow more
        assertTrue(limiter.allowRequest("user1", time + 1001));
    }

    @Test
    public void testSlidingWindowLog() {
        SlidingWindowLog limiter = new SlidingWindowLog(2, 1000);
        long time = System.currentTimeMillis();

        // First 2 requests allowed
        assertTrue(limiter.allowRequest("user1", time));
        assertTrue(limiter.allowRequest("user1", time + 100));

        // 3rd denied
        assertFalse(limiter.allowRequest("user1", time + 200));

        // After 1 second, previous requests expire
        assertTrue(limiter.allowRequest("user1", time + 1001));
    }

    @Test
    public void testSlidingWindowCounter() {
        SlidingWindowCounter limiter = new SlidingWindowCounter(3, 1000);
        long time = System.currentTimeMillis();

        // First 3 requests
        assertTrue(limiter.allowRequest("user1", time));
        assertTrue(limiter.allowRequest("user1", time + 100));
        assertTrue(limiter.allowRequest("user1", time + 200));

        // Should handle window transitions with weighted counting
        assertFalse(limiter.allowRequest("user1", time + 300));
    }

    @Test
    public void testTokenBucket() {
        // 5 tokens per second = 1 token per 200ms
        TokenBucket limiter = new TokenBucket(5, 5.0);
        long time = System.currentTimeMillis();

        // Burst: 5 requests immediately
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("user1", time), "Should allow burst up to capacity");
        }

        // 6th request immediately after should be denied
        assertFalse(limiter.allowRequest("user1", time), "Should deny when no tokens");

        // After 200ms, should have 1 more token
        assertTrue(limiter.allowRequest("user1", time + 200), "Should allow after refill");

        // After 1 second (full refill window)
        limiter.reset("user1");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("user1", time + 1000), "Should have full capacity");
        }
    }

    @Test
    public void testLeakyBucket() {
        // 2 requests/sec leak rate, capacity 4
        LeakyBucket limiter = new LeakyBucket(4, 2.0);
        long time = System.currentTimeMillis();

        // Fill the bucket
        for (int i = 0; i < 4; i++) {
            assertTrue(limiter.allowRequest("user1", time), "Should allow up to capacity");
        }

        // Bucket full, should deny
        assertFalse(limiter.allowRequest("user1", time), "Should deny when full");

        // After 500ms, 1 request leaks
        assertTrue(limiter.allowRequest("user1", time + 500), "Should allow after leak");
    }

    @Test
    public void testMultipleUsers() {
        TokenBucket limiter = new TokenBucket(2, 2.0);
        long time = System.currentTimeMillis();

        // Each user gets independent limit
        assertTrue(limiter.allowRequest("user1", time));
        assertTrue(limiter.allowRequest("user1", time + 100));
        assertFalse(limiter.allowRequest("user1", time + 200));

        // user2 should have separate limit
        assertTrue(limiter.allowRequest("user2", time + 200));
        assertTrue(limiter.allowRequest("user2", time + 300));
        assertFalse(limiter.allowRequest("user2", time + 400));
    }

    @Test
    public void testReset() {
        FixedWindowCounter limiter = new FixedWindowCounter(2, 1000);
        long time = System.currentTimeMillis();

        assertTrue(limiter.allowRequest("user1", time));
        assertTrue(limiter.allowRequest("user1", time + 100));
        assertFalse(limiter.allowRequest("user1", time + 200));

        // After reset, should allow again
        limiter.reset("user1");
        assertTrue(limiter.allowRequest("user1", time + 200));
    }

    @Test
    public void testBoundaryCase_FixedWindowSpike() {
        /*
         * This demonstrates the boundary spike issue with fixed window:
         * Window 1: t=0-999, allows 3 requests
         * At t=999: spike of requests just before window ends
         * Window 2: t=1000+, allows 3 more requests immediately
         * Result: 6 requests in 1ms (violates rate limit)
         */
        FixedWindowCounter limiter = new FixedWindowCounter(3, 1000);
        long time = System.currentTimeMillis();

        // 3 requests in first window
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allowRequest("user1", time + i * 10));
        }

        // 3 more requests in second window (boundary)
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allowRequest("user1", time + 1000 + i * 10),
                "Fixed window allows spike at boundary - this is the limitation");
        }
    }
}
