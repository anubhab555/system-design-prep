# Consistent Hashing LLD

## Source

- `src/main/java/com/systemdesign/consistenthashing/ConsistentHash.java`
- `src/main/java/com/systemdesign/consistenthashing/ConsistentHashingImpl.java`
- `src/main/java/com/systemdesign/consistenthashing/ConsistentHashingDemo.java`
- `src/test/java/com/systemdesign/consistenthashing/ConsistentHashingTest.java`

## Interface

```java
public interface ConsistentHash {
    void addNode(String node);
    void removeNode(String node);
    String getNode(String key);
    String[] getAllNodes();
    int getNodeCount();
}
```

This interface models the minimum operations needed for routing keys to nodes.

## Implementation Classes

`ConsistentHashingImpl`:

- Uses a sorted ring.
- Adds 150 virtual nodes per physical node.
- Uses a stable MD5-based hash for better distribution than Java
  `String.hashCode()`.
- Uses synchronized methods for simple thread safety.

## Data Structures

```java
private static final int VIRTUAL_NODES = 150;
private final TreeMap<Integer, String> ring = new TreeMap<>();
private final Map<String, Integer> nodeCount = new HashMap<>();
```

Why `TreeMap`:

- Maintains sorted hash positions.
- `ceilingEntry(hash)` finds the first node clockwise.
- `firstEntry()` handles wraparound.
- Operations are O(log N).

Why `nodeCount`:

- Tracks physical nodes.
- Returns physical node count instead of virtual node count.

## Hash Function

The implementation hashes strings using MD5 and converts the first four bytes to
a positive integer.

Why not plain `String.hashCode()`:

- It is fast but can produce poor distribution for similar small strings.
- Tests with keys like `key:1`, `key:2`, `server1:0` are sensitive to poor
  distribution.

Production alternative:

- MurmurHash3.
- xxHash.
- CityHash.

## Add Node

```text
for i from 0 to VIRTUAL_NODES - 1:
    virtualNode = node + ":" + i
    hash = hash(virtualNode)
    ring.put(hash, node)
nodeCount.put(node, VIRTUAL_NODES)
```

Complexity:

- Time: O(V log N)
- Space: O(V)

## Remove Node

```text
for i from 0 to VIRTUAL_NODES - 1:
    virtualNode = node + ":" + i
    hash = hash(virtualNode)
    ring.remove(hash)
nodeCount.remove(node)
```

Complexity:

- Time: O(V log N)

## Lookup

```text
if ring is empty:
    return null

hash = hash(key)
entry = ring.ceilingEntry(hash)

if entry is null:
    entry = ring.firstEntry()

return entry.value
```

Complexity:

- Time: O(log N)
- Space: O(1)

## Thread Safety

The repo implementation uses synchronized methods:

```java
public synchronized void addNode(String node) { ... }
public synchronized String getNode(String key) { ... }
```

This is correct and simple. It blocks concurrent reads during writes.

Production alternatives:

- `ReadWriteLock`: many concurrent lookups, exclusive add/remove.
- Immutable ring snapshot: rebuild a new ring and atomically swap reference.
- Versioned metadata: clients know which ring version routed a request.

## Replica Extension

A common follow-up is `getNodes(key, replicaCount)`.

Pseudocode:

```text
result = []
start at ceilingEntry(hash(key))
while result has fewer than replicaCount:
    visit next ring entry clockwise
    if physical node not already in result:
        add physical node
return result
```

Add zone awareness by skipping nodes in already-used failure domains until
needed.

## Edge Cases

- Empty ring: return null.
- Single physical node: all keys map to it.
- Key hash greater than every node hash: wrap to first entry.
- Duplicate node add: current implementation overwrites existing virtual hashes
  and stores the physical node once in `nodeCount`.
- Hash collision: current implementation overwrites the ring position. A
  production implementation should handle collision by chaining or rehashing.

## Test Strategy

The tests cover:

- Add and remove node.
- Empty ring lookup.
- Stable repeated lookup.
- Load distribution across multiple nodes.
- Minimal movement after adding a node.
- Node removal and replacement.
- Stress test with many nodes.

Useful additional tests:

- Replica selection.
- Weighted nodes.
- Concurrent lookup while membership changes.
- Hash collision behavior.
- Ring snapshot versioning.

## Production Improvements

- Support weighted virtual node count per physical node.
- Store node metadata: host, port, zone, status, capacity.
- Add `getNodes(key, replicaCount)`.
- Use immutable snapshots for lock-free reads.
- Use a faster stable hash.
- Add metrics for distribution and movement after membership changes.
