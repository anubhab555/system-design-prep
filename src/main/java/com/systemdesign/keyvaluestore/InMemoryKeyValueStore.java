package com.systemdesign.keyvaluestore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * Thread-safe in-memory key-value store implementation.
 *
 * The class is intentionally small enough for interview LLD discussion while
 * still covering production-shaped concerns such as TTL and optimistic writes.
 */
public class InMemoryKeyValueStore implements KeyValueStore {
    public static final long NO_EXPIRY = -1L;

    private final ConcurrentHashMap<String, ValueRecord> records = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public InMemoryKeyValueStore() {
        this(System::currentTimeMillis);
    }

    public InMemoryKeyValueStore(LongSupplier clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public ValueRecord put(String key, String value) {
        return putInternal(key, value, NO_EXPIRY);
    }

    @Override
    public ValueRecord put(String key, String value, long ttlMillis) {
        validateTtl(ttlMillis);
        return putInternal(key, value, ttlMillis);
    }

    @Override
    public Optional<ValueRecord> get(String key) {
        validateKey(key);
        ValueRecord record = records.get(key);
        long now = clock.getAsLong();

        if (record == null) {
            return Optional.empty();
        }

        if (record.isExpired(now)) {
            records.remove(key, record);
            return Optional.empty();
        }

        return Optional.of(record);
    }

    @Override
    public boolean delete(String key) {
        validateKey(key);
        return records.remove(key) != null;
    }

    @Override
    public boolean compareAndSet(String key, long expectedVersion, String newValue) {
        return compareAndSetInternal(key, expectedVersion, newValue, NO_EXPIRY);
    }

    @Override
    public boolean compareAndSet(String key, long expectedVersion, String newValue, long ttlMillis) {
        validateTtl(ttlMillis);
        return compareAndSetInternal(key, expectedVersion, newValue, ttlMillis);
    }

    @Override
    public Map<String, ValueRecord> scanByPrefix(String prefix) {
        validatePrefix(prefix);
        long now = clock.getAsLong();
        Map<String, ValueRecord> result = new LinkedHashMap<>();

        for (Map.Entry<String, ValueRecord> entry : records.entrySet()) {
            String key = entry.getKey();
            ValueRecord record = entry.getValue();

            if (record.isExpired(now)) {
                records.remove(key, record);
            } else if (key.startsWith(prefix)) {
                result.put(key, record);
            }
        }

        return result;
    }

    @Override
    public int size() {
        removeExpiredRecords();
        return records.size();
    }

    @Override
    public void clear() {
        records.clear();
    }

    private ValueRecord putInternal(String key, String value, long ttlMillis) {
        validateKey(key);
        validateValue(value);

        long now = clock.getAsLong();
        long expiresAt = calculateExpiry(now, ttlMillis);

        return records.compute(key, (currentKey, existing) -> {
            long nextVersion = 1L;
            long createdAt = now;

            if (existing != null && !existing.isExpired(now)) {
                nextVersion = existing.getVersion() + 1L;
                createdAt = existing.getCreatedAtMillis();
            }

            return new ValueRecord(currentKey, value, nextVersion, createdAt, now, expiresAt);
        });
    }

    private boolean compareAndSetInternal(String key, long expectedVersion, String newValue, long ttlMillis) {
        validateKey(key);
        validateValue(newValue);
        validateExpectedVersion(expectedVersion);

        long now = clock.getAsLong();
        long expiresAt = calculateExpiry(now, ttlMillis);
        AtomicBoolean updated = new AtomicBoolean(false);

        records.compute(key, (currentKey, existing) -> {
            ValueRecord current = existing;
            if (current != null && current.isExpired(now)) {
                current = null;
            }

            long actualVersion = current == null ? 0L : current.getVersion();
            if (actualVersion != expectedVersion) {
                return current;
            }

            updated.set(true);
            long nextVersion = expectedVersion + 1L;
            long createdAt = current == null ? now : current.getCreatedAtMillis();
            return new ValueRecord(currentKey, newValue, nextVersion, createdAt, now, expiresAt);
        });

        return updated.get();
    }

    private void removeExpiredRecords() {
        long now = clock.getAsLong();
        for (Map.Entry<String, ValueRecord> entry : records.entrySet()) {
            ValueRecord record = entry.getValue();
            if (record.isExpired(now)) {
                records.remove(entry.getKey(), record);
            }
        }
    }

    private long calculateExpiry(long now, long ttlMillis) {
        if (ttlMillis == NO_EXPIRY) {
            return NO_EXPIRY;
        }

        long expiresAt = now + ttlMillis;
        if (expiresAt < now) {
            throw new IllegalArgumentException("ttlMillis is too large");
        }
        return expiresAt;
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key must not be null or blank");
        }
    }

    private void validatePrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null");
        }
    }

    private void validateValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    private void validateTtl(long ttlMillis) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("ttlMillis must be positive");
        }
    }

    private void validateExpectedVersion(long expectedVersion) {
        if (expectedVersion < 0) {
            throw new IllegalArgumentException("expectedVersion must be >= 0");
        }
    }
}
