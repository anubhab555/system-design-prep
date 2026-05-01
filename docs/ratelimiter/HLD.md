# Design a Rate Limiter HLD

## 1. Problem

Design a system that controls how many requests a client can make in a time
window. The limiter protects downstream services, prevents abuse, and returns a
clear response when traffic exceeds policy.

This can be used at an API gateway, service sidecar, load balancer, or inside a
specific application service.

## 2. Requirements

Functional:

- Limit by user ID, API key, IP address, endpoint, tenant, or a combination.
- Support configurable limits such as `100 requests/minute`.
- Support different policies for different users or endpoints.
- Return allow/deny decision before the request reaches the protected service.
- Return metadata such as remaining quota and retry-after.
- Support burst handling for normal short traffic spikes.
- Log or emit metrics for rejected requests.

Non-functional:

- Very low latency because every request checks the limiter.
- Highly available because it sits on the critical path.
- Horizontally scalable to many API servers.
- Accurate enough for product needs.
- Safe under concurrency.
- Observable and easy to reconfigure.

Out of scope unless asked:

- Billing-grade exact metering.
- Complex fraud detection.
- Long-term analytics storage for every request.

## 3. Clarifying Questions

Ask:

- Is this a global limiter or per-region limiter?
- Do we need exact limits or approximate limits are acceptable?
- Should we fail open or fail closed if the limiter backend is down?
- Are limits per user, IP, endpoint, tenant, or all of them?
- Do premium users have higher limits?
- What is the target QPS and latency budget?

## 4. Scale Estimate

Example:

```text
Traffic: 1M requests/second peak
Active principals: 10M/day
Hot active principals at a time: 500K
Limiter latency target: less than 5 ms
Policy count: 10K endpoint/user-tier combinations
```

State size for token bucket:

```text
Per active principal:
  current tokens: 8 bytes
  last refill timestamp: 8 bytes
  metadata overhead: roughly 50-100 bytes

500K active principals * about 100 bytes = about 50 MB before store overhead
```

Full request logs are too expensive at high scale. Emit aggregate metrics and
sample logs; store detailed logs mainly for violations or audits.

## 5. API

Internal check:

```http
POST /v1/rate-limit/check
{
  "principal": "user:123",
  "resource": "POST:/payments",
  "cost": 1,
  "timestampMillis": 1710000000000
}
```

Response:

```json
{
  "allowed": true,
  "limit": 100,
  "remaining": 42,
  "retryAfterMillis": 0,
  "policyId": "payments-write-standard"
}
```

Client-facing rejection:

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
Retry-After: 12
```

## 6. High-Level Architecture

```text
Client
  |
Load Balancer / API Gateway
  |
Rate Limiter Module
  |---------------- Policy Cache
  |---------------- Policy Store
  |---------------- Redis / Counter Store
  |---------------- Metrics + Logs
  |
Protected Service
```

The rate limiter is commonly deployed close to the edge so abusive traffic is
blocked before expensive business logic runs.

## 7. Core Flow

```text
Request arrives
Gateway extracts principal and resource
Gateway finds matching policy
Limiter computes storage key
Limiter atomically updates counter/bucket state
If allowed:
    forward request to service
Else:
    return 429 with retry metadata
Emit metrics
```

## 8. Algorithm Options

| Algorithm | Accuracy | Memory | Burst Support | Notes |
|-----------|----------|--------|---------------|-------|
| Fixed Window Counter | Low | Low | Poor | Allows boundary spikes |
| Sliding Window Log | High | High | No | Stores every timestamp |
| Sliding Window Counter | Medium | Low | Limited | Good production compromise |
| Token Bucket | Medium | Low | Yes | Best default for APIs |
| Leaky Bucket | Medium | Medium | No | Good traffic shaping |

## 9. Recommended Design

Use token bucket for most APIs.

State per principal/resource:

```text
tokens
lastRefillTime
capacity
refillRate
```

Decision:

```text
newTokens = min(capacity, tokens + elapsedTime * refillRate)
if newTokens >= requestCost:
    allow and subtract requestCost
else:
    reject and return retry-after
```

Why token bucket:

- Allows short bursts up to bucket capacity.
- Keeps average rate bounded.
- O(1) state and O(1) update.
- Easy to explain and implement atomically.

## 10. Distributed Rate Limiting

For one application node, local memory can work. For many nodes, local memory
allows each node to grant its own quota, so the global limit can be exceeded.

Distributed options:

| Option | Pros | Cons |
|--------|------|------|
| Redis atomic script | Fast, simple, widely used | Redis is on critical path |
| Local limiter plus periodic sync | Very low latency | Approximate global limit |
| Dedicated counter service | Scales and isolates logic | More infrastructure |
| Database conditional writes | Durable | Higher latency |

Most interviews: choose Redis with Lua for atomicity, then discuss fail-open and
hot-key mitigation.

## 11. Redis Atomicity

The limiter must avoid this race:

```text
worker A reads 1 token
worker B reads 1 token
both allow
both write 0 tokens
```

Use a Redis Lua script so read, refill, decrement, and expiry update happen as
one atomic operation.

Also set key expiry so inactive principals do not consume memory forever.

## 12. Failure Handling

Redis down:

- Fail open for low-risk read traffic to preserve availability.
- Fail closed for sensitive endpoints such as payments or login attempts.
- Apply a small local emergency limiter to reduce blast radius.

Policy store down:

- Use cached policies.
- Attach TTLs to policy cache entries.
- Keep a conservative default policy.

Clock skew:

- Use Redis server time or monotonic service time.

Hot key:

- Add local pre-limit.
- Split by endpoint where possible.
- Protect Redis with connection pools and circuit breakers.

## 13. Observability

Track:

- Allowed and rejected request count.
- Rejection rate by policy.
- Redis latency and timeout rate.
- Policy cache hit rate.
- Top throttled principals.
- 429 response rate.
- Limiter fail-open/fail-closed events.

## 14. Interview Summary

"I would put the rate limiter at the API gateway. For most APIs I would use a
token bucket because it bounds average traffic while allowing short bursts. In a
distributed deployment the bucket state lives in Redis, and the refill plus
decrement happens in a Lua script for atomicity. Policies are cached locally,
and failures use a clear fail-open or fail-closed strategy depending on endpoint
risk. I would monitor rejection rate, Redis latency, and hot principals."
