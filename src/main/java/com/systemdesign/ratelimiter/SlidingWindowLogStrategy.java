package com.systemdesign.ratelimiter;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

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
 *
 * Cons:
 * - Higher memory usage (stores all request timestamps)
 * - O(n) time complexity where n = number of requests in window
 *
 * Time: O(n) where n is requests in current window
 * Space: O(n * m) where m is number of unique users
 *
 * Best for: Scenarios where accuracy is critical and traffic is moderate
 */
public class SlidingWindowLogStrategy implements RateLimiterStrategy {
    private final int maxRequests;
    private final long windowSizeInMillis;

    private final Map<String, UserRequestLog> userLogs = new ConcurrentHashMap<>();

    public SlidingWindowLogStrategy(int maxRequests, long windowSizeInMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeInMillis = windowSizeInMillis;
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userLogs.putIfAbsent(userId, new UserRequestLog());
        UserRequestLog log = userLogs.get(userId);

        synchronized (log) {
            log.evictExpired(currentTime, windowSizeInMillis);
            if (log.timestamps.size() < maxRequests) {
                log.timestamps.add(currentTime);
                return true;
            }
            return false;
        }
    }

    private static class UserRequestLog {
        final Queue<Long> timestamps = new LinkedList<>();

        public void evictExpired(long currentTime, long windowSizeInMillis) {
            while (!timestamps.isEmpty() && currentTime - timestamps.peek() >= windowSizeInMillis) {
                timestamps.poll();
            }
        }
    }
}
