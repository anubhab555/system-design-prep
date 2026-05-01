package com.systemdesign.consistenthashing;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Consistent Hashing Implementation
 * 
 * Problem it solves:
 * In a distributed system with multiple servers/caches, we need to determine which server
 * stores a particular key. With traditional modulo hashing (hash(key) % num_servers),
 * if we add/remove a server, almost ALL keys need to be rehashed and moved to different servers.
 * 
 * Consistent Hashing Solution:
 * - Place servers on a virtual ring (hash range: 0 to 2^32-1 or similar)
 * - Each key maps to a position on the ring
 * - The key is assigned to the first server found clockwise on the ring
 * - Adding/removing a server only affects keys between that server and the previous one
 * - Typically use virtual nodes to achieve better load distribution
 * 
 * Benefits:
 * - Minimal data movement when servers are added/removed
 * - Better load distribution with virtual nodes
 * - Efficient lookup: O(log N) with tree structure
 * - Works well for caching, distributed databases, key-value stores
 * 
 * Time Complexity:
 * - addNode: O(log N)
 * - removeNode: O(log N)
 * - getNode: O(log N) with tree, O(N) with array
 * 
 * Space: O(V * N) where V = virtual nodes per physical node, N = number of nodes
 * 
 * Real-world usage: Amazon DynamoDB, Cassandra, Redis Cluster, Memcached
 */
public class ConsistentHashingImpl implements ConsistentHash {
    private static final int VIRTUAL_NODES = 150; // Virtual nodes per physical node for better distribution
    private static final int HASH_SPACE = Integer.MAX_VALUE;
    
    // TreeMap maintains sorted order by hash value
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    private final Map<String, Integer> nodeCount = new HashMap<>(); // Count virtual nodes per physical node
    
    public ConsistentHashingImpl() {
    }

    /**
     * Stable hash with better distribution than String.hashCode for ring placement.
     */
    private int hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            int hash = ((bytes[0] & 0x7F) << 24)
                    | ((bytes[1] & 0xFF) << 16)
                    | ((bytes[2] & 0xFF) << 8)
                    | (bytes[3] & 0xFF);
            return hash % HASH_SPACE;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 hash algorithm is not available", e);
        }
    }

    @Override
    public synchronized void addNode(String node) {
        // Add virtual nodes for this physical node
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            String virtualNode = node + ":" + i;
            int hash = hash(virtualNode);
            ring.put(hash, node); // Store the physical node name, not virtual node
        }
        nodeCount.put(node, VIRTUAL_NODES);
    }

    @Override
    public synchronized void removeNode(String node) {
        // Remove all virtual nodes of this physical node
        for (int i = 0; i < VIRTUAL_NODES; i++) {
            String virtualNode = node + ":" + i;
            int hash = hash(virtualNode);
            ring.remove(hash);
        }
        nodeCount.remove(node);
    }

    @Override
    public synchronized String getNode(String key) {
        if (ring.isEmpty()) {
            return null;
        }

        int hash = hash(key);

        // Get the first node clockwise from this hash position
        Map.Entry<Integer, String> entry = ring.ceilingEntry(hash);
        
        // If no entry found after this hash, wrap around to the first entry (circular)
        if (entry == null) {
            entry = ring.firstEntry();
        }

        return entry.getValue();
    }

    @Override
    public synchronized String[] getAllNodes() {
        return new HashSet<>(ring.values()).toArray(new String[0]);
    }

    @Override
    public synchronized int getNodeCount() {
        return nodeCount.size();
    }

    /**
     * Get statistics about the ring - useful for analyzing load distribution
     * Returns the number of virtual nodes (hash positions) each physical node has
     */
    public synchronized Map<String, Integer> getNodeStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (String node : ring.values()) {
            stats.put(node, stats.getOrDefault(node, 0) + 1);
        }
        return stats;
    }

    /**
     * Get the ring visualization - useful for debugging
     */
    public synchronized void printRingStatistics() {
        Map<String, Integer> stats = getNodeStatistics();
        System.out.println("Ring Statistics:");
        System.out.println("Total hash positions: " + ring.size());
        System.out.println("Physical nodes: " + stats.size());
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / ring.size();
            System.out.printf("  %s: %d positions (%.2f%%)\n", entry.getKey(), entry.getValue(), percentage);
        }
    }
}
