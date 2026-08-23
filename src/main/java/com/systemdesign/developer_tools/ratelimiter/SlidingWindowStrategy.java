package com.systemdesign.developer_tools.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding Window Counter Algorithm (Hybrid approach)
 *
 * Concept: Combines fixed window and sliding window approaches.
 * Calculates weighted request count based on overlap of current window with previous bucket.
 *
 * Formula:
 * estimatedCount = (previousWindowCount * overlapRatio) + currentWindowCount
 *
 * Pros:
 * - More memory efficient than sliding window log
 * - Better accuracy than fixed window counter
 * - O(1) operation
 *
 * Cons:
 * - Slightly less accurate than sliding window log
 *
 * Time: O(1)
 * Space: O(number of unique users)
 *
 * Best for: Most practical use cases requiring good balance of accuracy and performance
 */
public class SlidingWindowStrategy implements RateLimiterStrategy {
    private final int maxRequests;
    private final long windowSizeInMillis;

    private final Map<String, WindowCounter> userCounters = new ConcurrentHashMap<>();

    public SlidingWindowStrategy(int maxRequests, long windowSizeInMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeInMillis = windowSizeInMillis;
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userCounters.putIfAbsent(userId, new WindowCounter(currentTime));
        WindowCounter counter = userCounters.get(userId);

        synchronized (counter) {
            counter.slideIfNeeded(currentTime, windowSizeInMillis);

            double overlapRatio = (double) (windowSizeInMillis - (currentTime - counter.currWindowStart)) / windowSizeInMillis;
            long estimatedCount = Math.round(counter.prevWindowCount * overlapRatio) + counter.currWindowCount;

            if (estimatedCount < maxRequests) {
                counter.currWindowCount++;
                return true;
            }
            return false;
        }
    }

    private static class WindowCounter {
        long prevWindowCount;
        long currWindowStart;
        long currWindowCount;

        public WindowCounter(long currentTime) {
            this.prevWindowCount = 0;
            this.currWindowStart = currentTime;
            this.currWindowCount = 0;
        }

        public void slideIfNeeded(long currentTime, long windowSizeInMillis) {
            if (currentTime >= currWindowStart + windowSizeInMillis) {
                prevWindowCount = currWindowCount;
                currWindowStart = currentTime;
                currWindowCount = 0;
            }
        }
    }
}
