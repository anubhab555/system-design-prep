package com.systemdesign.developer_tools.ratelimiter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Leaky Bucket Algorithm
 *
 * Concept: Imagine a bucket with a leak at the bottom. Requests are added to the bucket.
 * The bucket leaks (processes) requests at a constant rate. If bucket is full, new requests are rejected.
 *
 * Pros:
 * - Provides smooth, constant outflow rate
 * - Useful for protecting backend systems with fixed processing rate
 * - Bounds memory usage (queue size is limited)
 *
 * Cons:
 * - Less flexible for handling bursts compared to token bucket
 * - Needs background thread to process leaked requests in real scenario
 *
 * Time: O(1)
 * Space: O(capacity + number of users)
 *
 * Best for: Traffic shaping, smoothing bursty requests into constant rate
 */
public class LeakyBucketStrategy implements RateLimiterStrategy {
    private final int capacity;
    private final int leakRatePerSecond;

    private final Map<String, LeakyBucket> userBuckets = new ConcurrentHashMap<>();

    public LeakyBucketStrategy(int capacity, int leakRatePerSecond) {
        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userBuckets.putIfAbsent(userId, new LeakyBucket(capacity, leakRatePerSecond, currentTime));
        LeakyBucket bucket = userBuckets.get(userId);

        synchronized (bucket) {
            bucket.leak(currentTime);
            if (bucket.queueSize < capacity) {
                bucket.queueSize++;
                return true;
            }
            return false;
        }
    }

    private static class LeakyBucket {
        int queueSize;
        final int capacity;
        final int leakRatePerSecond;
        long lastLeakTimestamp;

        public LeakyBucket(int capacity, int leakRatePerSecond, long currentTime) {
            this.capacity = capacity;
            this.leakRatePerSecond = leakRatePerSecond;
            this.queueSize = 0;
            this.lastLeakTimestamp = currentTime;
        }

        public void leak(long currentTime) {
            long elapsedTime = currentTime - lastLeakTimestamp;
            int requestsToLeak = (int) (elapsedTime * leakRatePerSecond / 1000);
            if (requestsToLeak > 0) {
                queueSize = Math.max(0, queueSize - requestsToLeak);
                lastLeakTimestamp = currentTime;
            }
        }
    }
}
