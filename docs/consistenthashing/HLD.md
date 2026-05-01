# Design Consistent Hashing HLD

## 1. Problem

We need to map keys to nodes in a distributed system. A naive approach is:

```text
node = hash(key) % numberOfNodes
```

This works while membership is stable. When a node is added or removed, the
modulo changes and many keys move. That causes cache misses, expensive data
movement, and unstable operations.

Consistent hashing reduces the amount of key movement during membership changes.

## 2. Use Cases

- Distributed cache routing.
- Key-value store partitioning.
- Database sharding.
- Distributed queues by partition key.
- Load balancing sticky requests.

## 3. Requirements

Functional:

- Map a key to a node.
- Add a node with minimal remapping.
- Remove a node with minimal remapping.
- Support virtual nodes for better balance.
- Support replica selection for availability.

Non-functional:

- Fast lookup.
- Balanced distribution.
- Deterministic mapping.
- No central service required for every lookup.
- Operationally safe rebalancing.

## 4. Core Concept

Treat the hash space as a ring.

```text
0 ------------------------------------------------ maxHash
|                                                    |
|---------------- circular wraparound ---------------|
```

Steps:

1. Hash each node onto the ring.
2. Hash each key onto the ring.
3. Walk clockwise from the key position.
4. The first node found owns the key.
5. If there is no node clockwise, wrap to the first node.

## 5. Why It Reduces Movement

When adding a new node, only keys in the range between the new node and its
previous counter-clockwise neighbor move to the new node. Other keys keep the
same owner.

When removing a node, only keys owned by that node move to the next clockwise
node.

## 6. Virtual Nodes

With only one ring position per physical node, distribution can be uneven. A
node might own a large arc of the ring.

Virtual nodes fix this:

```text
server-a -> server-a#0, server-a#1, ..., server-a#149
server-b -> server-b#0, server-b#1, ..., server-b#149
```

Benefits:

- Better load distribution.
- Easier weighted capacity.
- Smaller ranges move during rebalancing.

Trade-off:

- More ring entries and memory.
- Slightly more expensive add/remove operations.

## 7. Architecture

```text
Client / Coordinator
  |
Hash key
  |
Consistent Hash Ring
  |---------------- Cluster Membership / Metadata
  |
Primary Node
  |
Replica Nodes
```

The ring can live in clients, coordinators, or routing services. Membership can
come from a config service, service discovery, or cluster metadata table.

## 8. Replica Selection

For storage systems, one owner is not enough. Choose replicas by continuing
clockwise around the ring and picking distinct physical nodes.

```text
primary = first node clockwise
replica1 = next distinct physical node
replica2 = next distinct physical node
```

Production systems should also consider rack, zone, or region awareness so
replicas are not placed in the same failure domain.

## 9. Complexity

Let:

- `S` = physical servers.
- `V` = virtual nodes per server.
- `N = S * V` ring entries.

| Operation | Time | Space |
|-----------|------|-------|
| Lookup key | O(log N) | O(1) |
| Add server | O(V log N) | O(V) |
| Remove server | O(V log N) | O(1) extra |
| Ring storage | - | O(S * V) |

## 10. Deep Dives

Hash function:

- Needs good distribution.
- Should be stable across languages if clients in multiple languages compute
  the ring.
- Non-cryptographic hashes such as MurmurHash or xxHash are common. MD5/SHA can
  be used for simplicity but may be slower.

Weighted nodes:

- Give larger machines more virtual nodes.
- Example: a server with twice the capacity gets twice the virtual nodes.

Hot keys:

- Consistent hashing balances key count, not request rate.
- One hot key can still overload one node.
- Mitigate with caching, request coalescing, or application-level key splitting.

Rebalancing:

- Move ranges gradually.
- Throttle migration.
- Keep old and new owners serving during handoff.

## 11. Failure Handling

Node failure:

- Remove node from membership.
- Route affected keys to next replicas.
- Re-replicate data when safe.

Membership disagreement:

- Version ring metadata.
- Clients should refresh ring config.
- Requests can include ring version for debugging.

Hash collision:

- Rare with good hash functions.
- If collision occurs for virtual nodes, store a list or rehash with a suffix.

## 12. Interview Summary

"Consistent hashing maps both nodes and keys onto a hash ring. A key is assigned
to the first node clockwise from its hash. Adding or removing a node only moves
keys in adjacent ranges, unlike modulo hashing which remaps many keys. Virtual
nodes improve load balance and support weighted capacity. For storage systems I
would continue clockwise to choose replicas and make replica placement aware of
failure domains."
