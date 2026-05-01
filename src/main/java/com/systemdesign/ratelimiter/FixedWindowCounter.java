package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.Map;

/**
 * Fixed Window Counter Algorithm
 * 
 * Concept: Divides time into fixed intervals. Each window has a counter.
 * When a request arrives, increment the counter for the current window.
 * If counter exceeds the limit, deny the request.
 * 
 * Pros:
 * - Simple to implement and understand
 * - Low memory footprint
 * - Very efficient computation
 * 
 * Cons:
 * - Boundary issue: Traffic spike at window boundaries can exceed rate limit
 * - Not precise at window edges
 * 
 * Time: O(1)
 * Space: O(number of unique users)
 */
public class FixedWindowCounter implements RateLimiter {
    private final int maxRequests;
    private final long windowSizeMs;
    
    // Map: userId -> [windowStartTime, requestCount]
    private final Map<String, long[]> userWindows = new HashMap<>();

    public FixedWindowCounter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public synchronized boolean allowRequest(String userId, long currentTime) {
        long[] windowData = userWindows.getOrDefault(userId, new long[]{currentTime, 0});
        long windowStart = windowData[0];
        long count = windowData[1];

        // Check if we're still in the same window
        if (currentTime - windowStart >= windowSizeMs) {
            // New window started, reset counter
            windowData[0] = currentTime;
            windowData[1] = 1;
            userWindows.put(userId, windowData);
            return true;
        }

        // Still in the same window
        if (count < maxRequests) {
            windowData[1]++;
            userWindows.put(userId, windowData);
            return true;
        }

        return false;
    }

    @Override
    public synchronized void reset(String userId) {
        userWindows.remove(userId);
    }

    public synchronized int getRequestCount(String userId, long currentTime) {
        long[] windowData = userWindows.get(userId);
        if (windowData == null) {
            return 0;
        }
        
        if (currentTime - windowData[0] >= windowSizeMs) {
            return 0;
        }
        
        return (int) windowData[1];
    }
}
