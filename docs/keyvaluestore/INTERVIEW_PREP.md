# Key-Value Store One-Stop Interview Prep

This is the focused prep sheet for "Design a Key-Value Store".

## 30-Second Answer

"I would design a partitioned, replicated key-value store. Clients talk to
stateless coordinator nodes. A coordinator hashes the key to find the primary
partition and replica nodes. Writes go to a write-ahead log and memtable on each
replica, then flush to immutable files with compaction. Replication uses quorum
reads and writes for tunable consistency. TTL and deletes are represented with
metadata and tombstones. The main trade-offs are consistency, availability,
latency, and operational complexity."

## Clarifying Questions

Ask these before drawing:

- Is this a cache or durable database?
- What are max key and value sizes?
- Do we need TTL?
- Do we need range scans or only point lookups?
- What consistency is required: strong, eventual, or tunable?
- What is the target read/write QPS?
- Is the workload read-heavy or write-heavy?
- Are multi-key transactions required?

## Requirements To State

Functional:

- Get value by key.
- Put value by key.
- Delete key.
- Optional TTL.
- Optional conditional update.

Non-functional:

- Low latency.
- High availability.
- Horizontal scaling.
- Durability.
- Configurable consistency.
- Observability.

## Scale Template

Use round numbers:

```text
Keys: 100M
Average value: 1 KB
Raw storage: 100 GB
Replication factor: 3
Total storage: about 300 GB before overhead
Read QPS: 200K
Write QPS: 50K
```

Then say storage overhead comes from metadata, WAL, compaction, indexes, and
replication repair, so capacity planning should be higher than raw data size.

## Core APIs

```text
PUT /kv/{key}
GET /kv/{key}
DELETE /kv/{key}
PUT /kv/{key} with If-Match-Version for conditional update
```

Internal record:

```text
key, value, version, createdAt, updatedAt, expiresAt, tombstone
```

## Architecture To Draw

```text
Client
  |
Load Balancer
  |
Coordinator Nodes
  |---------------- Metadata / Ring Service
  |---------------- Metrics and Logs
  |
Storage Nodes
  |-- WAL
  |-- Memtable
  |-- SSTables
  |-- Compaction
```

## Read Path

1. Client sends `GET(key)`.
2. Coordinator hashes key and finds replicas.
3. Coordinator sends read to `R` replicas.
4. Replicas check memtable first, then SSTables.
5. Coordinator picks the newest valid version.
6. If replicas disagree, coordinator can trigger read repair.

## Write Path

1. Client sends `PUT(key, value)`.
2. Coordinator hashes key and finds replicas.
3. Each replica appends to WAL.
4. Each replica updates memtable.
5. Coordinator waits for `W` acknowledgements.
6. Coordinator returns success.

## Consistency Script

Say:

"If the product needs lower latency and higher availability, I can use eventual
consistency with `W=1, R=1`. If it needs stronger reads, I can use quorum with
`N=3, W=2, R=2`, where reads and writes overlap. For strict consistency I would
choose leader-based replication per partition, but that reduces availability
during leader failures."

## Partitioning Script

Say:

"I would partition by hashing the key. Consistent hashing with virtual nodes
helps when nodes are added or removed because only a fraction of keys move. An
alternative is fixed hash slots, which can be easier to operate because slots
are explicit units of movement."

## Storage Engine Script

Say:

"For a write-heavy store I would use an LSM-tree style engine: append to WAL,
write to memtable, flush immutable SSTables, and compact in the background. This
turns random writes into sequential writes. Reads use memtable, sparse indexes,
and Bloom filters to avoid unnecessary disk IO."

## TTL And Deletes

- TTL is stored as `expiresAt`.
- Expired keys can be removed lazily on reads and during compaction.
- Deletes become tombstones in distributed storage.
- Tombstones must be retained long enough for all replicas to learn the delete.

## Common Follow-Ups

How do you handle node failure?

- Use replicas.
- Continue if quorum is available.
- Use hinted handoff for temporarily unavailable replicas.
- Run repair jobs to sync missed writes.

How do you handle hot keys?

- Cache hot reads.
- Replicate hot keys more widely.
- Request coalescing.
- Split key into subkeys if product semantics allow it.

How do you rebalance data?

- Add node to ring or assign slots.
- Move partitions gradually.
- Throttle migration.
- Keep old owner serving reads until new owner is caught up.

How do you resolve conflicts?

- Last-write-wins is simple but may lose updates.
- Version or CAS works for single-key optimistic writes.
- Vector clocks detect concurrent writes in multi-writer systems.
- Application merge is safest for complex values.

## LLD Bridge

The repo includes a concrete single-node implementation:

- `KeyValueStore` models the API.
- `InMemoryKeyValueStore` uses `ConcurrentHashMap`.
- `ValueRecord` stores value, version, and TTL metadata.
- `compareAndSet` demonstrates optimistic concurrency.
- Prefix scan is intentionally O(N), which creates a useful interview discussion
  about indexes and sorted storage.

## Mistakes To Avoid

- Jumping to storage internals before clarifying consistency.
- Ignoring deletes and tombstones.
- Saying "use consistent hashing" without explaining replication.
- Forgetting repair, rebalancing, and hot partitions.
- Claiming strong consistency while using `R=1, W=1`.
- Adding range scans without changing the partitioning/storage design.

## Final Answer Template

"The design uses stateless coordinators and partitioned storage nodes. Keys are
hashed to partitions using consistent hashing or hash slots. Each key is stored
on three replicas. Writes append to WAL, update memtable, and later flush to
SSTables. Reads query enough replicas to satisfy the chosen consistency level.
We support TTL with expiration metadata and deletes with tombstones. Background
jobs handle compaction, repair, hinted handoff, and rebalancing. The biggest
trade-off is whether we optimize for availability and latency or stronger
consistency."
