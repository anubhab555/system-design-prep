package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class TokenBucketStrategy implements RateLimiterStrategy {
    private final int capacity;
    private final int refillRatePerSecond; // tokens per millisecond
    
    // Map: userId -> [currentTokens, lastRefillTime]
    private final Map<String, TokenBucket> userBuckets = new ConcurrentHashMap<>();

    public TokenBucketStrategy(int capacity, int refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond; // Convert to per-millisecond
    }

    @Override
    public synchronized boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userBuckets.putIfAbsent(userId, new TokenBucket(capacity, refillRatePerSecond, currentTime));
        TokenBucket bucket = userBuckets.get(userId);

        synchronized(bucket){
            bucket.refill(currentTime);
            if(bucket.tokens > 0){
                bucket.tokens--;
                return true;
            }
            else{
                return false;
            }
        }

    }

    private static class TokenBucket {
        int tokens;
        final int capacity;
        int refillRateperSecond;
        long lastRefillTimeStamp;

        public TokenBucket(int capacity, int refillRate, long currentTimeMills) {
            this.capacity = capacity;
            this.refillRateperSecond = refillRate;
            this.tokens = capacity;
            this.lastRefillTimeStamp = currentTimeMills;
        }

        public void refill(long currentTime){
            long elapsedTime = currentTime - lastRefillTimeStamp;
            int tokenToAdd = (int) (elapsedTime * refillRateperSecond / 1000);

            if(tokens>0){
                tokens = Math.min(capacity, tokens+tokenToAdd);
                lastRefillTimeStamp = currentTime;
            }

        }
    }
}
