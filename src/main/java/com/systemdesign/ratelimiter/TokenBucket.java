package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.Map;

/**
 * Token Bucket Algorithm
 * 
 * Concept: Imagine a bucket that holds tokens. Tokens are added at a constant rate.
 * Each request requires 1 token. If bucket has tokens, allow request and remove 1 token.
 * If bucket is empty, deny request. Bucket has a maximum capacity.
 * 
 * Parameters:
 * - capacity: Maximum tokens the bucket can hold (burst size)
 * - refillRate: Tokens added per unit time (e.g., per second)
 * 
 * Pros:
 * - Allows for burst traffic (up to capacity)
 * - Smooth rate limiting with predictable behavior
 * - Can accommodate variable request rates
 * - Fair to different users
 * - Industry standard (used by major cloud providers)
 * 
 * Cons:
 * - Slightly more complex than fixed window
 * - Needs to track floating point time
 * 
 * Time: O(1)
 * Space: O(number of unique users)
 * 
 * Example: capacity=10, refillRate=2/sec
 * - Bucket starts with 10 tokens
 * - Each second, 2 tokens are added
 * - Each request consumes 1 token
 * - Can handle bursts up to 10 requests
 * 
 * Best for: Most production systems, APIs, etc.
 */
public class TokenBucket implements RateLimiter {
    private final int capacity;
    private final double refillRate; // tokens per millisecond
    
    // Map: userId -> [currentTokens, lastRefillTime]
    private final Map<String, double[]> userBuckets = new HashMap<>();

    /**
     * @param capacity Maximum tokens in bucket
     * @param refillRatePerSecond How many tokens to add per second
     */
    public TokenBucket(int capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRate = refillRatePerSecond / 1000.0; // Convert to per-millisecond
    }

    @Override
    public synchronized boolean allowRequest(String userId, long currentTime) {
        double[] bucket = userBuckets.computeIfAbsent(userId, k -> new double[]{capacity, currentTime});

        double currentTokens = bucket[0];
        long lastRefillTime = (long) bucket[1];

        // Calculate how many tokens to add based on time passed
        long timePassed = currentTime - lastRefillTime;
        double tokensToAdd = timePassed * refillRate;
        
        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        bucket[1] = currentTime;

        if (currentTokens >= 1) {
            bucket[0] = currentTokens - 1;
            return true;
        }

        bucket[0] = currentTokens;
        return false;
    }

    @Override
    public synchronized void reset(String userId) {
        userBuckets.remove(userId);
    }

    public synchronized double getAvailableTokens(String userId, long currentTime) {
        double[] bucket = userBuckets.get(userId);
        if (bucket == null) {
            return capacity;
        }

        long timePassed = currentTime - (long) bucket[1];
        double tokensToAdd = timePassed * refillRate;
        return Math.min(capacity, bucket[0] + tokensToAdd);
    }
}
