# Consistent Hashing One-Stop Interview Prep

## 30-Second Answer

"Consistent hashing maps both servers and keys onto a hash ring. A key belongs
to the first server clockwise from the key's hash. When a server is added or
removed, only keys in neighboring ranges move instead of most keys moving as
with modulo hashing. I would use virtual nodes to improve balance and choose
replicas by continuing clockwise to distinct physical nodes."

## Clarifying Questions

- Is this for cache routing, database sharding, or load balancing?
- Do we need replication?
- How often do nodes join or leave?
- Do nodes have equal capacity?
- Do clients compute the ring or does a coordinator route requests?
- Do we need zone-aware replica placement?
- Is range scan support required?

## Problem Script

Start with modulo hashing:

```text
node = hash(key) % N
```

Then explain:

"If N changes from 3 to 4, the mapping changes for many keys. That creates cache
misses or data movement. Consistent hashing avoids remapping unrelated ranges."

## Core Algorithm

```text
addNode(node):
    add many virtual node positions to sorted ring

getNode(key):
    keyHash = hash(key)
    find first ring position >= keyHash
    if none, wrap to first ring position
    return physical node

removeNode(node):
    remove all virtual node positions for that node
```

## Architecture To Draw

```text
Client / Coordinator
  |
Ring Metadata
  |
Consistent Hash Lookup
  |
Primary Node
  |
Replica Nodes
```

## Virtual Nodes Script

"Without virtual nodes, one physical node may own a large slice of the ring.
With virtual nodes, each physical node owns many small slices, so the law of
large numbers gives better balance. We can also assign more virtual nodes to
bigger machines."

## Complexity

```text
Lookup: O(log N)
Add node: O(V log N)
Remove node: O(V log N)
Space: O(S * V)
```

Where `S` is physical nodes and `V` is virtual nodes per node.

## Replication Follow-Up

Say:

"After finding the primary, I continue clockwise and choose the next distinct
physical nodes as replicas. For production I would make this rack or
availability-zone aware so one failure domain cannot take out all replicas."

## Failure Follow-Up

Node fails:

- Remove it from active membership.
- Route reads/writes to replicas.
- Re-replicate data to restore replication factor.
- When it returns, stream missing ranges back gradually.

Membership disagreement:

- Version the ring.
- Clients refresh metadata.
- Coordinators can reject or redirect stale-ring requests.

## Hot Key Follow-Up

Say:

"Consistent hashing balances key ownership, not request rate. A single hot key
can still overload one node. I would use caching, request coalescing, hot-key
replication, or split the key if the data model allows it."

## Common Mistakes To Avoid

- Saying consistent hashing guarantees perfect load balance.
- Forgetting virtual nodes.
- Forgetting replication.
- Ignoring node capacity differences.
- Using modulo hashing after explaining node churn.
- Not handling ring wraparound.
- Ignoring membership propagation.

## LLD Bridge

The repo implementation:

- Uses `TreeMap<Integer, String>` as the ring.
- Adds 150 virtual nodes per physical node.
- Uses `ceilingEntry` for clockwise lookup.
- Uses MD5-based hashing for stable distribution.
- Uses synchronized methods for simple thread safety.

Study `ConsistentHashingImpl.java` and tests for the core mechanics.

## Final Answer Template

"I would use a sorted hash ring with virtual nodes. Each physical node appears
many times on the ring. To route a key, hash it and choose the first ring entry
clockwise, wrapping to the first entry when needed. Adding a node only moves the
keys in ranges it takes over; removing a node moves only that node's ranges.
For storage, I would choose replicas by walking clockwise to distinct physical
nodes and make placement zone-aware."
