# One-Stop System Design Prep

Use this as the repeatable answer framework for every topic in this repo.

## Interview Flow

1. Clarify the problem.
   - Who are the users?
   - What are the exact operations?
   - What is out of scope?
   - Is this read-heavy, write-heavy, latency-sensitive, or consistency-sensitive?

2. Write requirements.
   - Functional: APIs and behavior.
   - Non-functional: latency, availability, durability, consistency, throughput.

3. Estimate scale.
   - Daily active users or clients.
   - QPS for reads and writes.
   - Storage per day and retention.
   - Peak-to-average traffic ratio.

4. Define APIs and data model.
   - Keep APIs small and explicit.
   - Explain identifiers, metadata, indexes, and TTL.

5. Draw the HLD.
   - Client, load balancer/API gateway, stateless services, data stores, caches,
     queues, metrics, and config.

6. Explain core flows.
   - Read path.
   - Write path.
   - Failure path.
   - Background jobs.

7. Deep dive into the hard parts.
   - Partitioning and routing.
   - Replication.
   - Consistency model.
   - Hot key mitigation.
   - Backpressure and rate limiting.
   - Observability.

8. Close with trade-offs.
   - Name the chosen design.
   - Mention alternatives and why you did not choose them.
   - State the remaining risks.

## Component Cheat Sheet

| Topic | First Answer | Deep Dive |
|-------|--------------|-----------|
| Rate Limiter | Token bucket at API gateway | distributed counters, Redis Lua, fail open vs fail closed |
| Consistent Hashing | Hash ring with virtual nodes | replication, node churn, hot partitions |
| Key-Value Store | Partitioned, replicated storage | quorum reads/writes, hinted handoff, compaction, conflict resolution |

## Practice Routine

1. Spend 5 minutes writing requirements.
2. Spend 5 minutes estimating QPS and storage.
3. Spend 10 minutes drawing the architecture.
4. Spend 10 minutes on read and write paths.
5. Spend 10 minutes on deep dives.
6. Spend 5 minutes summarizing trade-offs.

The goal is not to memorize one diagram. The goal is to reliably move from
requirements to a defensible design under time pressure.
