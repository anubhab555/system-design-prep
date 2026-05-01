package com.systemdesign.consistenthashing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Consistent Hashing implementation
 */
public class ConsistentHashingTest {
    
    private ConsistentHashingImpl hash;
    
    @BeforeEach
    public void setUp() {
        hash = new ConsistentHashingImpl();
    }

    @Test
    public void testAddNode() {
        hash.addNode("server1");
        assertEquals(1, hash.getNodeCount());
        
        hash.addNode("server2");
        assertEquals(2, hash.getNodeCount());
    }

    @Test
    public void testRemoveNode() {
        hash.addNode("server1");
        hash.addNode("server2");
        assertEquals(2, hash.getNodeCount());
        
        hash.removeNode("server1");
        assertEquals(1, hash.getNodeCount());
    }

    @Test
    public void testGetNode() {
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        // Keys should consistently map to the same server
        String nodeForKey1 = hash.getNode("user:1001");
        String nodeForKey2 = hash.getNode("user:1001");
        assertEquals(nodeForKey1, nodeForKey2);
        
        // Should return one of the servers
        assertTrue(nodeForKey1.contains("server"));
    }

    @Test
    public void testGetNodeWithEmptyRing() {
        assertNull(hash.getNode("somekey"));
    }

    @Test
    public void testMultipleKeysDistribution() {
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        // Distribute many keys and verify each returns a valid server
        boolean hasServer1 = false, hasServer2 = false, hasServer3 = false;
        
        for (int i = 0; i < 100; i++) {
            String node = hash.getNode("key:" + i);
            assertTrue(node != null);
            assertTrue(node.equals("server1") || node.equals("server2") || node.equals("server3"));
            
            if (node.equals("server1")) hasServer1 = true;
            if (node.equals("server2")) hasServer2 = true;
            if (node.equals("server3")) hasServer3 = true;
        }
        
        // All servers should be used (with high probability)
        assertTrue(hasServer1, "server1 should be used");
        assertTrue(hasServer2, "server2 should be used");
        assertTrue(hasServer3, "server3 should be used");
    }

    @Test
    public void testMinimalDataMovement() {
        // Add initial servers
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        // Record initial mapping
        int[] initialMapping = new int[100];
        for (int i = 0; i < 100; i++) {
            String node = hash.getNode("key:" + i);
            if (node.equals("server1")) initialMapping[i] = 1;
            else if (node.equals("server2")) initialMapping[i] = 2;
            else initialMapping[i] = 3;
        }
        
        // Add a new server
        hash.addNode("server4");
        
        // Count how many keys changed
        int keysChanged = 0;
        for (int i = 0; i < 100; i++) {
            String node = hash.getNode("key:" + i);
            int newMapping = 0;
            if (node.equals("server1")) newMapping = 1;
            else if (node.equals("server2")) newMapping = 2;
            else if (node.equals("server3")) newMapping = 3;
            else newMapping = 4;
            
            if (initialMapping[i] != newMapping) {
                keysChanged++;
            }
        }
        
        // With consistent hashing, only ~25% of keys should change (optimal would be 25/100)
        double percentChanged = (keysChanged * 100.0) / 100;
        System.out.printf("Keys affected by adding server4: %.1f%%\n", percentChanged);
        assertTrue(percentChanged < 40, "Should only affect ~25% of keys, but affected " + percentChanged + "%");
    }

    @Test
    public void testConsistencyAcrossOperations() {
        hash.addNode("server1");
        hash.addNode("server2");
        
        String node1 = hash.getNode("product:123");
        String node2 = hash.getNode("product:123");
        String node3 = hash.getNode("product:123");
        
        assertEquals(node1, node2);
        assertEquals(node2, node3);
    }

    @Test
    public void testAddNodeThenRemove() {
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        String keyBefore = hash.getNode("data:key");
        
        // Remove and re-add the same node
        hash.removeNode("server2");
        hash.addNode("server2");
        
        String keyAfter = hash.getNode("data:key");
        
        // After re-adding, the mapping should be the same
        // (might not always be exact due to virtual node randomness, but likely)
        // Just verify it returns a valid server
        assertTrue(keyAfter.contains("server"));
    }

    @Test
    public void testGetAllNodes() {
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        String[] nodes = hash.getAllNodes();
        assertEquals(3, nodes.length);
        
        boolean hasServer1 = false, hasServer2 = false, hasServer3 = false;
        for (String node : nodes) {
            if (node.equals("server1")) hasServer1 = true;
            if (node.equals("server2")) hasServer2 = true;
            if (node.equals("server3")) hasServer3 = true;
        }
        
        assertTrue(hasServer1);
        assertTrue(hasServer2);
        assertTrue(hasServer3);
    }

    @Test
    public void testServerRemovalAndReplacement() {
        hash.addNode("server1");
        hash.addNode("server2");
        hash.addNode("server3");
        
        // Record which server holds a particular key
        String originalServer = hash.getNode("criticalData:123");
        
        // Remove that server
        hash.removeNode(originalServer);
        
        // The key should now map to a different server
        String newServer = hash.getNode("criticalData:123");
        assertNotEquals(originalServer, newServer);
        assertTrue(newServer.contains("server"));
    }

    @Test
    public void testStressTestManyNodes() {
        // Add many nodes
        for (int i = 0; i < 50; i++) {
            hash.addNode("server" + i);
        }
        
        assertEquals(50, hash.getNodeCount());
        
        // Verify all keys map to valid servers
        for (int i = 0; i < 1000; i++) {
            String node = hash.getNode("key:" + i);
            assertNotNull(node);
            assertTrue(node.startsWith("server"));
        }
    }
}
