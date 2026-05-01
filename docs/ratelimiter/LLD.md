# Rate Limiter LLD

## Source

- `src/main/java/com/systemdesign/ratelimiter/RateLimiter.java`
- `src/main/java/com/systemdesign/ratelimiter/FixedWindowCounter.java`
- `src/main/java/com/systemdesign/ratelimiter/SlidingWindowLog.java`
- `src/main/java/com/systemdesign/ratelimiter/SlidingWindowCounter.java`
- `src/main/java/com/systemdesign/ratelimiter/TokenBucket.java`
- `src/main/java/com/systemdesign/ratelimiter/LeakyBucket.java`
- `src/test/java/com/systemdesign/ratelimiter/RateLimiterTest.java`

## Interface

```java
public interface RateLimiter {
    boolean allowRequest(String userId, long currentTime);
    void reset(String userId);
}
```

The implementation accepts `currentTime` as an argument. That makes tests
deterministic and avoids sleeping.

## Fixed Window Counter

State:

```text
Map<userId, [windowStartTime, requestCount]>
```

Flow:

```text
if currentTime is outside stored window:
    start new window with count = 1
    allow
else if count < maxRequests:
    increment count
    allow
else:
    reject
```

Complexity:

- Time: O(1)
- Space: O(active users)

Main weakness:

- Boundary spike. A user can send `maxRequests` at the end of one window and
  another `maxRequests` at the start of the next.

## Sliding Window Log

State:

```text
Map<userId, Queue<requestTimestamp>>
```

Flow:

```text
remove timestamps older than window
if queue size < maxRequests:
    add current timestamp
    allow
else:
    reject
```

Complexity:

- Time: O(requests in window) worst case for cleanup.
- Space: O(active users * requests per window).

Best use:

- Strict accuracy for moderate traffic.

## Sliding Window Counter

State:

```text
Map<userId, [
  previousWindowStart,
  previousWindowCount,
  currentWindowStart,
  currentWindowCount
]>
```

Flow:

```text
if current window expired:
    move current count into previous window
    start new current window

overlapRatio = remaining time in current window / window size
estimatedCount = previousCount * overlapRatio + currentCount

if estimatedCount < maxRequests:
    increment current count
    allow
else:
    reject
```

Complexity:

- Time: O(1)
- Space: O(active users)

Best use:

- Good balance when exact timestamp logs are too expensive.

## Token Bucket

State:

```text
Map<userId, [currentTokens, lastRefillTime]>
capacity
refillRate
```

Flow:

```text
elapsed = currentTime - lastRefillTime
tokens = min(capacity, tokens + elapsed * refillRate)
lastRefillTime = currentTime

if tokens >= 1:
    tokens = tokens - 1
    allow
else:
    reject
```

Complexity:

- Time: O(1)
- Space: O(active users)

Best use:

- Default API rate limiting because it allows controlled bursts.

## Leaky Bucket

State:

```text
Map<userId, [queue, lastLeakTime]>
capacity
leakRate
```

Flow:

```text
elapsed = currentTime - lastLeakTime
leakCount = elapsed * leakRate
remove leakCount queued requests
lastLeakTime = currentTime

if queue size < capacity:
    add request
    allow
else:
    reject
```

Complexity:

- Time: O(items leaked)
- Space: O(active users * capacity)

Best use:

- Traffic shaping when downstream processing should be smoothed.

## Thread Safety In This Repo

All implementations use `synchronized` methods. This gives correctness and is
simple for learning, but it serializes all users through one object lock.

For production:

- Use `ConcurrentHashMap`.
- Lock or compute per user key.
- Move atomic state updates into Redis Lua for distributed deployment.

More detail: `THREAD_SAFETY.md`.

## Input and State Considerations

Production systems should add:

- Validation for blank/null principal IDs.
- Cleanup of inactive users.
- Maximum number of tracked principals.
- Stable time source and monotonic handling.
- Request cost greater than 1 for expensive endpoints.
- Policy lookup by endpoint and user tier.

## Test Strategy

The tests cover:

- Allowing requests under the limit.
- Rejecting requests over the limit.
- Window reset behavior.
- Token refill behavior.
- Multiple user isolation.
- Fixed-window boundary spike.
- Reset behavior.

Useful additional tests:

- Concurrent requests for the same user.
- Clock moving backward.
- Large number of users.
- Very small and very large limits.
