package com.systemdesign.keyvaluestore;

import java.util.Map;

/**
 * Demonstrates key-value store operations and the interview-relevant API.
 */
public class KeyValueStoreDemo {
    public static void main(String[] args) throws InterruptedException {
        KeyValueStore store = new InMemoryKeyValueStore();

        System.out.println("=== Key-Value Store Demo ===\n");

        ValueRecord user = store.put("user:101", "Anika");
        System.out.printf("PUT user:101 -> value=%s, version=%d%n", user.getValue(), user.getVersion());

        ValueRecord updated = store.put("user:101", "Anika Sharma");
        System.out.printf("PUT user:101 again -> value=%s, version=%d%n", updated.getValue(), updated.getVersion());

        boolean casFailed = store.compareAndSet("user:101", 1L, "Wrong version update");
        boolean casSucceeded = store.compareAndSet("user:101", 2L, "Anika S.");
        System.out.printf("CAS expected version 1 -> %s%n", casFailed ? "updated" : "rejected");
        System.out.printf("CAS expected version 2 -> %s%n", casSucceeded ? "updated" : "rejected");

        store.put("session:abc", "active", 100L);
        System.out.printf("TTL before expiry -> present=%s%n", store.get("session:abc").isPresent());
        Thread.sleep(150L);
        System.out.printf("TTL after expiry -> present=%s%n", store.get("session:abc").isPresent());

        store.put("user:102", "Ravi");
        store.put("order:9001", "created");

        System.out.println("\nScan prefix 'user:':");
        for (Map.Entry<String, ValueRecord> entry : store.scanByPrefix("user:").entrySet()) {
            System.out.printf("  %s -> %s (version %d)%n",
                    entry.getKey(),
                    entry.getValue().getValue(),
                    entry.getValue().getVersion());
        }

        System.out.printf("%nCurrent live keys: %d%n", store.size());
    }
}
