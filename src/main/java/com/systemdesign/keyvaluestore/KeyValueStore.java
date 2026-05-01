package com.systemdesign.keyvaluestore;

import java.util.Map;
import java.util.Optional;

/**
 * Contract for a simple key-value store.
 *
 * This interface models the LLD surface that is useful in interviews: point
 * reads, writes, deletes, TTL, prefix scans, and optimistic concurrency through
 * compare-and-set.
 */
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
