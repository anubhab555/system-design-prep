package com.systemdesign.consistenthashing;

import java.util.*;

/**
 * Demonstration of Consistent Hashing
 * Shows how keys are distributed and how adding/removing servers affects key allocation
 */
public class ConsistentHashingDemo {

    public static void main(String[] args) {
        System.out.println("=== Consistent Hashing Demo ===\n");

        demoBasicConsistentHashing();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoAddingServers();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoRemovingServers();
        System.out.println("\n" + "=".repeat(70) + "\n");
        
        demoLoadDistribution();
    }

    private static void demoBasicConsistentHashing() {
        System.out.println("1. BASIC CONSISTENT HASHING");
        System.out.println("- Initialize hash ring with 3 servers");
        System.out.println("- Map 10 keys to servers\n");

        ConsistentHashingImpl hash = new ConsistentHashingImpl();
        
        // Add servers
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        System.out.println("Added 3 servers to the ring\n");

        // Map keys
        String[] keys = {"user:1001", "user:1002", "user:1003", "cache:product:100", 
                         "cache:product:101", "session:abc", "session:def", 
                         "data:config", "data:user:profile", "data:settings"};
        
        System.out.println("Key -> Server Mapping:");
        Map<String, List<String>> serverToKeys = new HashMap<>();
        
        for (String key : keys) {
            String node = hash.getNode(key);
            serverToKeys.computeIfAbsent(node, k -> new ArrayList<>()).add(key);
            System.out.printf("  %s -> %s\n", key, node);
        }
        
        System.out.println("\nDistribution across servers:");
        hash.printRingStatistics();
    }

    private static void demoAddingServers() {
        System.out.println("2. ADDING A NEW SERVER");
        System.out.println("- Start with 3 servers and 20 keys");
        System.out.println("- Add a 4th server and observe key redistribution\n");

        ConsistentHashingImpl hash = new ConsistentHashingImpl();
        
        // Add initial servers
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        String[] keys = generateKeys(20);
        Map<String, String> keyToNodeBefore = new HashMap<>();
        
        System.out.println("Initial mapping (3 servers):");
        for (String key : keys) {
            String node = hash.getNode(key);
            keyToNodeBefore.put(key, node);
        }
        System.out.printf("  Distributed %d keys across 3 servers\n\n", keys.length);
        
        // Add a new server
        System.out.println("Adding server4...");
        hash.addNode("server4");
        
        // Recalculate mappings
        Map<String, String> keyToNodeAfter = new HashMap<>();
        int keysAffected = 0;
        
        for (String key : keys) {
            String nodeBefore = keyToNodeBefore.get(key);
            String nodeAfter = hash.getNode(key);
            keyToNodeAfter.put(key, nodeAfter);
            
            if (!nodeBefore.equals(nodeAfter)) {
                keysAffected++;
            }
        }
        
        System.out.println("After adding server4:");
        System.out.printf("  Keys affected: %d out of %d (%.1f%%)\n", 
            keysAffected, keys.length, (keysAffected * 100.0 / keys.length));
        System.out.println("  This is much better than traditional hashing (which would affect ~75% of keys)");
        
        hash.printRingStatistics();
    }

    private static void demoRemovingServers() {
        System.out.println("3. REMOVING A SERVER");
        System.out.println("- Start with 4 servers and keys");
        System.out.println("- Remove server2 and observe redistribution\n");

        ConsistentHashingImpl hash = new ConsistentHashingImpl();
        
        // Add servers
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        hash.addNode("server4");
        
        String[] keys = generateKeys(20);
        Map<String, String> keyToNodeBefore = new HashMap<>();
        
        System.out.println("Initial mapping (4 servers):");
        for (String key : keys) {
            String node = hash.getNode(key);
            keyToNodeBefore.put(key, node);
        }
        System.out.printf("  Distributed %d keys\n\n", keys.length);
        
        // Remove a server
        System.out.println("Removing server2...");
        hash.removeNode("server2");
        
        int affectedByRemoval = 0;
        for (String key : keys) {
            String nodeBefore = keyToNodeBefore.get(key);
            String nodeAfter = hash.getNode(key);
            
            // Count only keys that were on server2
            if (nodeBefore.equals("server2") && !nodeAfter.equals("server2")) {
                affectedByRemoval++;
            }
        }
        
        System.out.println("After removing server2:");
        System.out.printf("  Keys redistributed: Only those that were on server2 (%d keys)\n", affectedByRemoval);
        System.out.println("  Other servers' keys remain unchanged!");
        
        hash.printRingStatistics();
    }

    private static void demoLoadDistribution() {
        System.out.println("4. LOAD DISTRIBUTION WITH MANY KEYS");
        System.out.println("- Distribute 10000 keys across different numbers of servers");
        System.out.println("- Compare with traditional hashing\n");

        String[] keys = generateKeys(10000);
        int[] serverCounts = {3, 5, 10};
        
        for (int numServers : serverCounts) {
            ConsistentHashingImpl hash = new ConsistentHashingImpl();
            
            for (int i = 1; i <= numServers; i++) {
                hash.addNode("server" + i);
            }
            
            Map<String, Integer> distribution = new HashMap<>();

            for (String key : keys) {
                String node = hash.getNode(key);
                distribution.put(node, distribution.getOrDefault(node, 0) + 1);
            }

            double minLoad = Integer.MAX_VALUE;
            double maxLoad = Integer.MIN_VALUE;

            for (Integer count : distribution.values()) {
                minLoad = Math.min(minLoad, count);
                maxLoad = Math.max(maxLoad, count);
            }
            
            double avgLoad = 10000.0 / numServers;
            double skew = ((maxLoad - minLoad) / avgLoad) * 100;
            
            System.out.printf("Servers: %d\n", numServers);
            System.out.printf("  Avg load per server: %.0f keys\n", avgLoad);
            System.out.printf("  Min: %.0f, Max: %.0f\n", minLoad, maxLoad);
            System.out.printf("  Load skew: %.1f%%\n\n", skew);
        }
    }

    private static String[] generateKeys(int count) {
        String[] keys = new String[count];
        for (int i = 0; i < count; i++) {
            keys[i] = "key:" + i;
        }
        return keys;
    }
}
