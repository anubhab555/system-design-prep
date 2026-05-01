package com.systemdesign.keyvaluestore;

import java.util.Objects;

/**
 * Immutable value returned by the key-value store.
 *
 * Version starts at 1 and increases on every successful write. A version of 0
 * is reserved for "key does not exist" compare-and-set calls.
 */
public final class ValueRecord {
    private final String key;
    private final String value;
    private final long version;
    private final long createdAtMillis;
    private final long updatedAtMillis;
    private final long expiresAtMillis;

    public ValueRecord(
            String key,
            String value,
            long version,
            long createdAtMillis,
            long updatedAtMillis,
            long expiresAtMillis) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
        this.version = version;
        this.createdAtMillis = createdAtMillis;
        this.updatedAtMillis = updatedAtMillis;
        this.expiresAtMillis = expiresAtMillis;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public long getVersion() {
        return version;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public boolean hasExpiry() {
        return expiresAtMillis != InMemoryKeyValueStore.NO_EXPIRY;
    }

    public boolean isExpired(long nowMillis) {
        return hasExpiry() && expiresAtMillis <= nowMillis;
    }

    @Override
    public String toString() {
        return "ValueRecord{"
                + "key='" + key + '\''
                + ", value='" + value + '\''
                + ", version=" + version
                + ", expiresAtMillis=" + expiresAtMillis
                + '}';
    }
}
