# Rate Limiter One-Stop Interview Prep

## 30-Second Answer

"I would place the rate limiter at the API gateway so rejected traffic does not
reach expensive services. For most APIs I would use token bucket because it
allows controlled bursts while enforcing an average rate. In a distributed
setup I would store bucket state in Redis and update it atomically with Lua.
Policies are cached locally, responses include 429 and retry-after metadata, and
the failure mode is fail open or fail closed depending on endpoint risk."

## Clarifying Questions

- What are we limiting by: user, API key, IP, endpoint, tenant, or all?
- Is the limit global or per region?
- Do we need exact enforcement or is approximate enforcement acceptable?
- What should happen if the limiter state store is down?
- Are bursts allowed?
- Are there different tiers such as free, paid, internal?
- What is peak QPS?
- What latency can the limiter add?

## Requirements To State

Functional:

- Check whether a request is allowed.
- Support configurable policies.
- Return retry metadata.
- Support burst handling.
- Track metrics for allowed and rejected requests.

Non-functional:

- Low latency.
- High availability.
- Horizontally scalable.
- Atomic under concurrency.
- Observable.

## Scale Template

```text
Peak traffic: 1M requests/sec
Active principals at peak: 500K
State per principal: tokens + last refill timestamp
Approx state: 500K * about 100 bytes = about 50 MB before store overhead
```

Then say:

"I would not log every allowed request at this scale. I would aggregate metrics
and sample logs, while logging rejected requests more carefully."

## API To Present

```text
check(principal, resource, cost, timestamp) -> decision
```

Decision:

```text
allowed
limit
remaining
retryAfterMillis
policyId
```

## Algorithm Script

Start simple:

"A fixed window counter is easy but has a boundary spike. Sliding log is
accurate but expensive. Sliding counter is a good approximation. Token bucket is
my default for APIs because it supports bursts and keeps O(1) state."

Token bucket pseudocode:

```text
tokens = min(capacity, tokens + elapsed * refillRate)
if tokens >= cost:
    tokens -= cost
    allow
else:
    reject
```

## Architecture To Draw

```text
Client
  |
API Gateway
  |
Rate Limiter Module
  |-- Local Policy Cache
  |-- Redis Atomic Bucket State
  |-- Metrics
  |
Service
```

## Distributed Script

"If all app servers keep their own local counters, global quota can be exceeded.
So for exact distributed limiting, I put token bucket state in Redis and use a
Lua script to atomically refill and decrement. If the product can tolerate some
overage, I can use local buckets with periodic synchronization to reduce
latency."

## Failure Script

"For public read APIs I may fail open to preserve availability. For sensitive
actions like login, password reset, or payments, I may fail closed or use a
small local emergency limit. I would make this configurable per endpoint."

## Common Follow-Ups

How do you avoid Redis becoming a bottleneck?

- Use connection pooling.
- Shard limiter keys.
- Cache policy locally.
- Use local pre-limits for obvious abuse.
- Keep Lua script O(1).

How do you handle hot users?

- Isolate hot keys.
- Apply stricter local limit before Redis.
- Split by resource if semantics allow.
- Alert on top throttled principals.

How do you support different user tiers?

- Policy store maps principal/resource to capacity and refill rate.
- Cache policies in gateway with TTL.
- Include policy ID in metrics.

How do you calculate retry-after?

```text
missingTokens = cost - currentTokens
retryAfter = missingTokens / refillRate
```

## LLD Bridge

The repo includes five algorithms:

- `FixedWindowCounter`: easiest baseline.
- `SlidingWindowLog`: exact but memory-heavy.
- `SlidingWindowCounter`: practical approximation.
- `TokenBucket`: recommended default.
- `LeakyBucket`: traffic shaping.

Study `TokenBucket.java` first for interviews, then compare it with fixed
window to explain why the design improves.

## Mistakes To Avoid

- Forgetting the boundary spike in fixed window.
- Claiming local memory is globally accurate across many servers.
- Ignoring Redis atomicity.
- Ignoring failure mode.
- Logging every request at massive scale.
- Forgetting policy cache invalidation.

## Final Answer Template

"I would use a token bucket limiter at the gateway. The policy store defines
capacity and refill rate per principal/resource. The gateway caches policies and
uses Redis Lua to atomically update bucket state. Allowed requests continue to
the service; rejected requests return 429 with retry-after. I would monitor
rejections, Redis latency, and hot keys. The main trade-off is strict global
accuracy versus latency and availability."
