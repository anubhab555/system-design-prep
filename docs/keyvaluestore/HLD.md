# Design a Key-Value Store HLD

## 1. Problem

Design a distributed key-value store that supports storing, reading, and
deleting values by key. The system should scale horizontally and remain
available when some nodes fail.

This is the family of systems behind Dynamo-style stores, Cassandra-like
databases, Redis-like caches, and metadata stores. In an interview, clarify
which behavior the interviewer wants before jumping into architecture.

## 2. Requirements

Functional:

- `put(key, value)` writes a value.
- `get(key)` returns the latest visible value.
- `delete(key)` removes a value.
- Optional TTL per key.
- Optional prefix/range scan if keys are ordered.
- Optional conditional write for concurrency control.

Non-functional:

- Low read/write latency.
- High availability.
- Horizontal scalability.
- Durable writes.
- Configurable consistency.
- Operationally manageable rebalancing and recovery.

Out of scope unless asked:

- SQL joins.
- Multi-key transactions.
- Full-text search.
- Complex secondary indexes.

## 3. Back-of-the-Envelope Scale

Example assumptions:

- 100 million keys.
- Average value size: 1 KB.
- Replication factor: 3.
- Read QPS: 200,000.
- Write QPS: 50,000.

Storage:

```text
Raw data = 100M * 1 KB = 100 GB
With replication factor 3 = 300 GB
Add metadata, indexes, compaction overhead = plan for 500+ GB
```

Traffic:

```text
Reads = 200K/s * 1 KB = 200 MB/s
Writes = 50K/s * 1 KB * replication factor 3 = 150 MB/s internal write traffic
```

## 4. APIs

```http
PUT /v1/kv/{key}
Headers:
  X-TTL-Millis: optional
Body:
  raw value
```

```http
GET /v1/kv/{key}
```

```http
DELETE /v1/kv/{key}
```

```http
PUT /v1/kv/{key}
Headers:
  If-Match-Version: 42
```

The conditional write is useful for optimistic concurrency: update only if the
client is still looking at the expected version.

## 5. Data Model

```text
Key:
  string or bytes

ValueRecord:
  key
  value bytes
  version
  createdAt
  updatedAt
  expiresAt optional
  tombstone flag for deletes
```

Deletes should usually be represented as tombstones in distributed storage so
replicas that missed the delete can learn about it later.

## 6. High-Level Architecture

```text
Client
  |
Load Balancer / API Gateway
  |
Coordinator Nodes
  |-------------------- Cluster Metadata Service
  |-------------------- Metrics / Logs / Tracing
  |
Partitioned Storage Nodes
  |-- Write-Ahead Log
  |-- Memtable
  |-- SSTables / Segment Files
  |-- Compaction
```

Coordinator nodes are stateless. They locate the partition for a key, send the
request to replica storage nodes, and apply the consistency policy.

## 7. Partitioning

Use consistent hashing or fixed hash slots.

Consistent hashing:

- Good for dynamic membership.
- Minimal key movement when nodes change.
- Virtual nodes improve distribution.

Hash slots:

- Divide hash space into a fixed number of slots.
- Move slots between nodes during rebalancing.
- Easier to reason about operationally.

In the interview, either is acceptable. For a Dynamo-style design, consistent
hashing is a natural answer.

## 8. Replication

Use replication factor `N`, commonly 3.

For a key:

1. Find primary node from the partitioning scheme.
2. Choose the next distinct nodes as replicas.
3. Write to multiple replicas.
4. Read from one or more replicas depending on consistency needs.

Quorum model:

```text
N = replica count
W = write acknowledgements required
R = read responses required

If R + W > N, reads and writes overlap on at least one replica.
```

Example:

```text
N = 3, W = 2, R = 2
```

This gives stronger consistency than `W = 1, R = 1`, but adds latency and can
reduce availability during failures.

## 9. Write Path

```text
Client sends PUT(key, value)
Coordinator hashes key
Coordinator selects replicas
Coordinator sends write to replicas
Each replica appends to WAL
Each replica updates memtable
Coordinator waits for W acknowledgements
Coordinator returns success
```

The write-ahead log makes the write durable before acknowledging. The memtable
keeps recent data fast in memory.

## 10. Read Path

```text
Client sends GET(key)
Coordinator hashes key
Coordinator queries R replicas
Replicas check memtable, then immutable files
Coordinator resolves versions if responses differ
Coordinator returns value
Coordinator may trigger read repair for stale replicas
```

## 11. Storage Engine

Common design: LSM tree.

- WAL for durability.
- Memtable for in-memory writes.
- Immutable SSTables flushed from memtable.
- Bloom filters to avoid unnecessary disk reads.
- Compaction to merge files and remove old versions/tombstones.

Why LSM works well:

- Writes are sequential and fast.
- Compaction handles update-heavy workloads.
- Reads can be optimized with indexes and Bloom filters.

Alternative: B-tree storage. Better for range reads and read-heavy workloads,
but random writes can be more expensive.

## 12. Consistency and Conflict Resolution

Options:

- Strong consistency through leader-based replication.
- Tunable consistency through quorum reads/writes.
- Eventual consistency with conflict resolution.

Conflict resolution choices:

- Last-write-wins using timestamps. Simple but can lose updates.
- Version numbers for single-writer or per-key leader designs.
- Vector clocks for multi-writer systems. More correct but more complex.
- Application-level merge for complex values.

## 13. Failure Handling

Node down:

- Coordinator writes to available replicas.
- Use hinted handoff so a healthy node temporarily stores writes for the failed node.
- Repair missing data when the node returns.

Network partition:

- Choose availability or stronger consistency depending on product needs.
- Quorum settings control this trade-off.

Disk failure:

- Re-replicate data from healthy replicas.
- Keep checksums for corruption detection.

Hot key:

- Cache hot reads.
- Replicate hot key reads more widely.
- Split key or add request coalescing when possible.

## 14. Observability

Track:

- Read/write latency p50, p95, p99.
- Error rate.
- Quorum failure count.
- Replication lag.
- Compaction backlog.
- Disk utilization.
- Hot keys and hot partitions.
- Tombstone count.

## 15. Interview Summary

"I would build a partitioned and replicated key-value store. A coordinator hashes
each key to a partition, chooses replica nodes through consistent hashing, and
uses quorum reads and writes for tunable consistency. Each storage node writes
to a WAL for durability, updates an in-memory memtable, and later flushes to
immutable SSTables with compaction. TTL and deletes are handled with metadata
and tombstones. The main trade-off is consistency versus availability and
latency."
