package com.systemdesign.ratelimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class FixedWindowStrategy implements RateLimiterStrategy {
    private final int maxRequests;
    private final long windowSizeInMillis;
    
    // Map: userId -> [windowStartTime, requestCount]
    private final Map<String, UserRequestInfo> userRequestMap = new ConcurrentHashMap<>();

    public FixedWindowStrategy(int maxRequests, long windowSizeInMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeInMillis = windowSizeInMillis;
    }

    @Override
    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userRequestMap.putIfAbsent(userId, new UserRequestInfo(currentTime, 0));
        UserRequestInfo requestInfo = userRequestMap.get(userId);

        synchronized(requestInfo){
            if(currentTime - requestInfo.windowStart >= windowSizeInMillis){
                requestInfo.reset(currentTime);
            }

            // Still in the same window
            if (requestInfo.requestCount < maxRequests) {
                requestInfo.requestCount++;
                return true;
            }
            else
                return false;
        }
    }

    private static class UserRequestInfo{
        long windowStart;
        int requestCount;

        public UserRequestInfo(long windowStart, int requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }

        public void reset(long currentTime){
            this.windowStart = currentTime;
            this.requestCount = 1;
        }
    }

}
