package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Leaky Bucket Algorithm
 * 
 * Concept: Imagine a bucket with a leak at the bottom. Requests are added to the bucket.
 * The bucket leaks (processes) requests at a constant rate. If bucket is full, new requests are rejected.
 * 
 * Pros:
 * - Provides smooth, constant outflow rate
 * - Can be used to shape/smooth traffic
 * - Useful for protecting backend systems with fixed processing rate
 * - Bounds memory usage (queue size is limited)
 * 
 * Cons:
 * - More complex to implement than token bucket
 * - Requires tracking request timestamps in queue
 * - Less flexible for handling bursts compared to token bucket
 * - Needs background thread to process leaked requests in real scenario
 * 
 * Time: O(n) where n is number of requests in queue (in worst case)
 * Space: O(capacity + number of users)
 * 
 * Note: This is a simplified implementation that processes requests on-demand.
 * In production, you'd typically have a background thread processing the leak.
 * 
 * Similar to: Token bucket but with different semantics
 * Difference: Token bucket adds tokens; leaky bucket removes requests at constant rate
 * 
 * Best for: Traffic shaping, smoothing bursty requests into constant rate
 */
public class LeakyBucket implements RateLimiter {
    private final int capacity;
    private final double leakRate; // requests per millisecond
    
    // Map: userId -> [Queue of request timestamps, last leak time]
    private final Map<String, Object[]> userBuckets = new HashMap<>();

    /**
     * @param capacity Maximum requests the bucket can hold
     * @param leakRatePerSecond How many requests to leak per second
     */
    public LeakyBucket(int capacity, double leakRatePerSecond) {
        this.capacity = capacity;
        this.leakRate = leakRatePerSecond / 1000.0; // Convert to per-millisecond
    }

    @Override
    public synchronized boolean allowRequest(String userId, long currentTime) {
        Object[] bucket = userBuckets.computeIfAbsent(userId, k -> new Object[]{new LinkedList<Long>(), currentTime});
        
        @SuppressWarnings("unchecked")
        Queue<Long> queue = (Queue<Long>) bucket[0];
        long lastLeakTime = (long) bucket[1];

        // Calculate how many requests should leak
        long timePassed = currentTime - lastLeakTime;
        double requestsToLeak = timePassed * leakRate;
        
        // Remove leaked requests
        int leakCount = (int) requestsToLeak;
        for (int i = 0; i < leakCount && !queue.isEmpty(); i++) {
            queue.poll();
        }
        
        bucket[1] = currentTime;

        // Check if bucket has space
        if (queue.size() < capacity) {
            queue.add(currentTime);
            return true;
        }

        return false;
    }

    @Override
    public synchronized void reset(String userId) {
        userBuckets.remove(userId);
    }

    public synchronized int getQueueSize(String userId) {
        Object[] bucket = userBuckets.get(userId);
        if (bucket == null) {
            return 0;
        }
        
        @SuppressWarnings("unchecked")
        Queue<Long> queue = (Queue<Long>) bucket[0];
        return queue.size();
    }
}
