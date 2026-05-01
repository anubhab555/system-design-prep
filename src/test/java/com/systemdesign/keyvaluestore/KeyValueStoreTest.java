package com.systemdesign.keyvaluestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValueStoreTest {
    private AtomicLong clock;
    private InMemoryKeyValueStore store;

    @BeforeEach
    public void setUp() {
        clock = new AtomicLong(1_000L);
        store = new InMemoryKeyValueStore(clock::get);
    }

    @Test
    public void putAndGetRecord() {
        ValueRecord record = store.put("user:1", "alice");

        Optional<ValueRecord> found = store.get("user:1");

        assertTrue(found.isPresent());
        assertEquals("alice", found.get().getValue());
        assertEquals(1L, record.getVersion());
        assertEquals(1L, found.get().getVersion());
    }

    @Test
    public void putIncrementsVersionAndKeepsCreateTime() {
        ValueRecord first = store.put("user:1", "alice");
        clock.addAndGet(25L);

        ValueRecord second = store.put("user:1", "alice-updated");

        assertEquals(2L, second.getVersion());
        assertEquals(first.getCreatedAtMillis(), second.getCreatedAtMillis());
        assertEquals(1_025L, second.getUpdatedAtMillis());
    }

    @Test
    public void missingKeyReturnsEmpty() {
        assertFalse(store.get("missing").isPresent());
    }

    @Test
    public void ttlRecordExpiresLazily() {
        store.put("session:1", "active", 100L);

        assertTrue(store.get("session:1").isPresent());

        clock.addAndGet(101L);

        assertFalse(store.get("session:1").isPresent());
        assertEquals(0, store.size());
    }

    @Test
    public void deleteRemovesRecord() {
        store.put("user:1", "alice");

        assertTrue(store.delete("user:1"));
        assertFalse(store.get("user:1").isPresent());
        assertFalse(store.delete("user:1"));
    }

    @Test
    public void compareAndSetUpdatesOnlyWhenVersionMatches() {
        ValueRecord initial = store.put("user:1", "alice");

        assertFalse(store.compareAndSet("user:1", initial.getVersion() + 1, "bad-update"));
        assertTrue(store.compareAndSet("user:1", initial.getVersion(), "alice-v2"));

        ValueRecord found = store.get("user:1").orElseThrow(AssertionError::new);
        assertEquals("alice-v2", found.getValue());
        assertEquals(2L, found.getVersion());
    }

    @Test
    public void compareAndSetCanCreateWhenExpectedVersionIsZero() {
        assertTrue(store.compareAndSet("user:1", 0L, "alice"));

        ValueRecord found = store.get("user:1").orElseThrow(AssertionError::new);
        assertEquals("alice", found.getValue());
        assertEquals(1L, found.getVersion());
    }

    @Test
    public void compareAndSetTreatsExpiredRecordAsMissing() {
        store.put("session:1", "active", 50L);
        clock.addAndGet(51L);

        assertTrue(store.compareAndSet("session:1", 0L, "new-session"));

        ValueRecord found = store.get("session:1").orElseThrow(AssertionError::new);
        assertEquals("new-session", found.getValue());
        assertEquals(1L, found.getVersion());
    }

    @Test
    public void scanByPrefixReturnsOnlyLiveMatchingKeys() {
        store.put("user:1", "alice");
        store.put("user:2", "bob");
        store.put("user:expired", "old", 50L);
        store.put("order:1", "created");

        clock.addAndGet(51L);
        Map<String, ValueRecord> users = store.scanByPrefix("user:");

        assertEquals(2, users.size());
        assertTrue(users.containsKey("user:1"));
        assertTrue(users.containsKey("user:2"));
        assertFalse(users.containsKey("user:expired"));
    }

    @Test
    public void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> store.put("", "value"));
        assertThrows(IllegalArgumentException.class, () -> store.put("key", null));
        assertThrows(IllegalArgumentException.class, () -> store.put("key", "value", 0L));
        assertThrows(IllegalArgumentException.class, () -> store.compareAndSet("key", -1L, "value"));
        assertThrows(IllegalArgumentException.class, () -> store.scanByPrefix(null));
    }
}
