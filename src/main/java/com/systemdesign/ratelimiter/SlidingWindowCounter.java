package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.Map;

/**
 * Sliding Window Counter Algorithm (Hybrid approach)
 * 
 * Concept: Combines fixed window and sliding window approaches.
 * Keeps track of counts in multiple buckets (e.g., previous and current).
 * Calculates weighted request count based on overlap of current window with previous bucket.
 * 
 * Formula: 
 * allowedRequests = maxRequests - (previousBucketCount * overlapRatio + currentBucketCount)
 * 
 * Pros:
 * - More memory efficient than sliding window log
 * - Better accuracy than fixed window counter
 * - No boundary issue spike
 * - O(1) operation
 * 
 * Cons:
 * - More complex than fixed window
 * - Slightly less accurate than sliding window log
 * - Still has minor edge case issues depending on implementation
 * 
 * Time: O(1)
 * Space: O(2 * number of unique users)
 * 
 * Best for: Most practical use cases requiring good balance of accuracy and performance
 */
public class SlidingWindowCounter implements RateLimiter {
    private final int maxRequests;
    private final long windowSizeMs;
    
    // Map: userId -> [prevWindowStartTime, prevWindowCount, currentWindowStartTime, currentWindowCount]
    private final Map<String, long[]> userCounters = new HashMap<>();

    public SlidingWindowCounter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
    }

    @Override
    public synchronized boolean allowRequest(String userId, long currentTime) {
        long[] counter = userCounters.getOrDefault(userId, new long[4]);
        
        long prevWindowStart = counter[0];
        long prevWindowCount = counter[1];
        long currWindowStart = counter[2];
        long currWindowCount = counter[3];

        // Determine which window we're in
        long currWindowEnd = currWindowStart + windowSizeMs;
        
        if (currentTime >= currWindowEnd) {
            // Moving to a new window
            prevWindowStart = currWindowStart;
            prevWindowCount = currWindowCount;
            currWindowStart = currentTime;
            currWindowCount = 0;
        } else if (currentTime < currWindowStart) {
            // Edge case: time moved backwards (shouldn't happen in practice)
            currWindowStart = currentTime;
            currWindowCount = 0;
        }

        // Calculate how many requests we can still make
        // Weighted count from previous window based on overlap
        double timePassedInCurrWindow = currentTime - currWindowStart;
        double overlapRatio = (double) (windowSizeMs - timePassedInCurrWindow) / windowSizeMs;
        
        long weightedPrevCount = Math.round(prevWindowCount * overlapRatio);
        long totalCount = weightedPrevCount + currWindowCount;

        if (totalCount < maxRequests) {
            currWindowCount++;
            counter[0] = prevWindowStart;
            counter[1] = prevWindowCount;
            counter[2] = currWindowStart;
            counter[3] = currWindowCount;
            userCounters.put(userId, counter);
            return true;
        }

        return false;
    }

    @Override
    public synchronized void reset(String userId) {
        userCounters.remove(userId);
    }

    public synchronized long getRequestCount(String userId, long currentTime) {
        long[] counter = userCounters.get(userId);
        if (counter == null) {
            return 0;
        }
        
        long prevWindowCount = counter[1];
        long currWindowStart = counter[2];
        long currWindowCount = counter[3];
        
        double timePassedInCurrWindow = currentTime - currWindowStart;
        double overlapRatio = (double) (windowSizeMs - timePassedInCurrWindow) / windowSizeMs;
        
        long weightedPrevCount = Math.round(prevWindowCount * overlapRatio);
        return weightedPrevCount + currWindowCount;
    }
}
