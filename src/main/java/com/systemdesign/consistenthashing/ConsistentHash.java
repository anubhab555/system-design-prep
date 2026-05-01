package com.systemdesign.consistenthashing;

/**
 * Interface for consistent hashing implementations.
 * Used for distributed cache/database systems where servers can be dynamically added/removed.
 */
public interface ConsistentHash {
    /**
     * Add a server/node to the ring
     * @param node Server identifier
     */
    void addNode(String node);

    /**
     * Remove a server/node from the ring
     * @param node Server identifier
     */
    void removeNode(String node);

    /**
     * Get the server responsible for this key
     * @param key The key to hash
     * @return The server identifier responsible for this key
     */
    String getNode(String key);

    /**
     * Get all nodes currently in the ring
     * @return Array of node identifiers
     */
    String[] getAllNodes();

    /**
     * Get the number of nodes in the ring
     * @return Number of nodes
     */
    int getNodeCount();
}
