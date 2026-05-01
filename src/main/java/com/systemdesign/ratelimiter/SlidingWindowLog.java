package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Sliding Window Log Algorithm
 * 
 * Concept: Stores the timestamp of each request for each user in a queue.
 * When a new request arrives, remove timestamps older than the window.
 * If queue size < limit, allow request and add timestamp; otherwise deny.
 * 
 * Pros:
 * - Very accurate, no boundary issues
 * - Precise rate limiting at any point in time
 * - Easy to understand and implement
 * 
 * Cons:
 * - Higher memory usage (stores all request timestamps)
 * - O(n) time complexity where n = number of requests in window
 * - Can be memory-intensive with high request rates
 * 
 * Time: O(n) where n is requests in current window
 * Space: O(n * m) where m is number of unique users
 * 
 * Best for: Scenarios where accuracy is critical and traffic is moderate
 */
public class SlidingWindowLog implements RateLimiter {
    private final int maxRequests;
    private final long windowSizeMs;
    
    // Map: userId -> Queue of request timestamps
    private final Map<String, Queue<Long>> userLogs = new HashMap<>();

    public SlidingWindowLog(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public synchronized boolean allowRequest(String userId, long currentTime) {
        userLogs.putIfAbsent(userId, new LinkedList<>());
        Queue<Long> timestamps = userLogs.get(userId);

        // Remove timestamps outside the current window
        while (!timestamps.isEmpty() && currentTime - timestamps.peek() >= windowSizeMs) {
            timestamps.poll();
        }

        // Check if we can allow this request
        if (timestamps.size() < maxRequests) {
            timestamps.add(currentTime);
            return true;
        }

        return false;
    }

    @Override
    public synchronized void reset(String userId) {
        userLogs.remove(userId);
    }

    public synchronized int getRequestCount(String userId, long currentTime) {
        Queue<Long> timestamps = userLogs.get(userId);
        if (timestamps == null) {
            return 0;
        }

        // Count requests still within the window
        int count = 0;
        for (Long timestamp : timestamps) {
            if (currentTime - timestamp < windowSizeMs) {
                count++;
            }
        }
        return count;
    }
}
