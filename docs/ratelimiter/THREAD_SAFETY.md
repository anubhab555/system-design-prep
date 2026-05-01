# Rate Limiter Thread Safety

## Current Implementation

The Java implementations use synchronized methods around shared maps:

```java
public synchronized boolean allowRequest(String userId, long currentTime) {
    ...
}
```

This means one thread at a time can enter the limiter object.

Guarantees:

- No data races on maps or queues.
- Read-modify-write operations are atomic.
- Memory visibility is handled by the Java monitor.
- The code stays easy to reason about for interviews.

Trade-off:

- All users share one lock. A request for `userA` can block an unrelated request
  for `userB`.

## Better Single-Node Design

Use per-key locking:

```java
ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

Bucket bucket = buckets.computeIfAbsent(userId, ignored -> new Bucket());
synchronized (bucket) {
    refill(bucket, now);
    if (bucket.tokens >= 1) {
        bucket.tokens--;
        return true;
    }
    return false;
}
```

This preserves correctness per user while allowing unrelated users to proceed in
parallel.

## Lock-Free Style

For simple counter-based limiters, `AtomicLong` or `compute` can reduce explicit
locking:

```java
buckets.compute(userId, (key, bucket) -> updatedBucket(bucket, now));
```

This still serializes updates for the same key, which is exactly what the rate
limiter needs.

## Distributed Thread Safety

Local thread safety does not solve multi-server correctness. If three API
servers each have a local bucket of 100 requests/minute, the real global limit
can become 300 requests/minute.

For distributed correctness:

- Put state in Redis or another shared store.
- Use Lua or conditional writes for atomic updates.
- Add TTL to limiter keys.
- Cache policies locally, not bucket state, unless approximation is acceptable.

## Interview Answer

Say:

"The code uses synchronized methods for correctness and clarity. In production I
would avoid a global object lock and use per-user locking or atomic map updates.
For multiple application servers I would move the atomic check-and-update into a
shared store such as Redis Lua, because local locks only protect one process."
