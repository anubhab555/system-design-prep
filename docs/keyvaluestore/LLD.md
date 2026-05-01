# Key-Value Store LLD

## Source

- `src/main/java/com/systemdesign/keyvaluestore/KeyValueStore.java`
- `src/main/java/com/systemdesign/keyvaluestore/InMemoryKeyValueStore.java`
- `src/main/java/com/systemdesign/keyvaluestore/ValueRecord.java`
- `src/main/java/com/systemdesign/keyvaluestore/KeyValueStoreDemo.java`
- `src/test/java/com/systemdesign/keyvaluestore/KeyValueStoreTest.java`

## Scope

The Java implementation is a single-node in-memory store. It is not a full
distributed database. It exists to make the low-level API, data structures, TTL,
and concurrency behavior concrete before scaling the same concepts in the HLD.

## Interface

```java
public interface KeyValueStore {
    ValueRecord put(String key, String value);
    ValueRecord put(String key, String value, long ttlMillis);
    Optional<ValueRecord> get(String key);
    boolean delete(String key);
    boolean compareAndSet(String key, long expectedVersion, String newValue);
    boolean compareAndSet(String key, long expectedVersion, String newValue, long ttlMillis);
    Map<String, ValueRecord> scanByPrefix(String prefix);
    int size();
    void clear();
}
```

## Data Structures

```java
ConcurrentHashMap<String, ValueRecord> records;
LongSupplier clock;
```

`ConcurrentHashMap` gives thread-safe access. Atomic per-key update logic is
implemented with `compute`, which prevents two writers from corrupting the same
key's version.

`LongSupplier` makes TTL behavior testable without sleeping in tests.

## Record Model

```text
ValueRecord
  key
  value
  version
  createdAtMillis
  updatedAtMillis
  expiresAtMillis
```

Versioning rules:

- New key starts at version 1.
- Every successful write increments the version.
- Version 0 means "key is missing" for compare-and-set.
- Expired keys are treated as missing.

## Put Flow

```text
validate key and value
now = clock()
records.compute(key):
    if no existing live value:
        version = 1
        createdAt = now
    else:
        version = existing.version + 1
        createdAt = existing.createdAt
    store new ValueRecord
return new record
```

Time complexity: O(1) average.

## Get Flow

```text
validate key
record = records.get(key)
if missing:
    return empty
if expired:
    remove it lazily
    return empty
return record
```

TTL cleanup is lazy. This keeps the implementation simple and avoids a
background thread. A production store would usually combine lazy cleanup with
scheduled expiration or compaction.

Time complexity: O(1) average.

## Compare-And-Set Flow

```text
validate key, value, expected version
records.compute(key):
    if existing is expired:
        current = missing
    actualVersion = current == missing ? 0 : current.version
    if actualVersion != expectedVersion:
        keep current value
    else:
        write new value with version expectedVersion + 1
return whether update happened
```

This models optimistic concurrency control. It is useful when multiple clients
may read, modify, and write the same key.

## Delete Flow

```text
records.remove(key)
```

The in-memory implementation physically removes the key. In a distributed
store, deletes are usually tombstones so replicas can converge.

## Prefix Scan

```text
for each entry in records:
    remove if expired
    if key starts with prefix:
        include in result
```

Time complexity: O(N). This is acceptable for a teaching implementation. In a
production store, prefix/range scans need sorted storage, partition-aware scans,
or a secondary index.

## Thread Safety

The implementation is safe for concurrent access because:

- `ConcurrentHashMap` protects the table structure.
- `compute` serializes updates for the same key.
- `ValueRecord` is immutable.
- Expired-record removal uses conditional remove so it does not delete a newer
  value written by another thread.

## Edge Cases

- Null or blank key: rejected.
- Null value: rejected.
- TTL less than or equal to zero: rejected.
- Negative expected version: rejected.
- Expired key read: removed and returned as missing.
- CAS on missing key with expected version 0: creates the key.

## Production Extensions

- Add a WAL for durability.
- Add a background TTL cleanup worker.
- Store byte arrays instead of strings.
- Add max key and value size checks.
- Add metrics around hit rate, latency, and expired keys.
- Add an LSM-tree or B-tree storage layer.
- Add partition routing and replication for distributed operation.

## Test Coverage

The test suite covers:

- Basic put/get.
- Version increments.
- Missing keys.
- TTL expiry.
- Delete behavior.
- Compare-and-set success and failure.
- CAS create with expected version 0.
- Prefix scans.
- Input validation.
