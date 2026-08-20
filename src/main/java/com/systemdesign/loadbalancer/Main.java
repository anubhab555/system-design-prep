package com.systemdesign.loadbalancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Create some backend servers with different capacities (weights)
        Server serverA = new Server("Server-A", "192.168.1.10", 5);
        Server serverB = new Server("Server-B", "192.168.1.11", 2);
        Server serverC = new Server("Server-C", "192.168.1.12", 1);
        
        List<Server> servers = new ArrayList<>();
        servers.add(serverA);
        servers.add(serverB);
        servers.add(serverC);

        // Instantiate Strategies
        LoadBalancingStrategy roundRobin = new RoundRobinStrategy();
        LoadBalancingStrategy weightedRR = new WeightedRoundRobinStrategy();
        LoadBalancingStrategy leastConn = new LeastConnectionsStrategy();
        LoadBalancingStrategy powerOfTwo = new PowerOfTwoChoicesStrategy();

        // ---------------------------------------------------------
        // CHANGE THIS VARIABLE TO TEST DIFFERENT STRATEGIES
        // ---------------------------------------------------------
        LoadBalancingStrategy activeStrategy = weightedRR; 
        
        System.out.println("Starting Simulation with Strategy: " + activeStrategy.getClass().getSimpleName());

        LoadBalancer loadBalancer = new LoadBalancer(activeStrategy);
        for (Server s : servers) {
            loadBalancer.addServer(s);
        }

        // Setup threading and tracking
        int totalRequests = 6;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
        
        // Disable individual request logs if you want a cleaner final output, 
        // but we'll leave them to see the action!

        for (int i = 0; i < totalRequests; i++) {
            final String requestId = "Req-" + i;
            final String sourceIp = "10.0.0." + (i % 20); // Dummy IPs
            
            executor.submit(() -> {
                Request req = new Request(requestId, sourceIp);
                
                // Route the request
                Server selectedServer = loadBalancer.routeRequest(req);
                
                // Track stats
                requestCounts.computeIfAbsent(selectedServer.getId(), k -> new AtomicInteger(0)).incrementAndGet();
                
                // Simulate work (increment connections -> sleep -> decrement)
                selectedServer.incrementActiveConnections();
                try {
                    // Random sleep between 10ms and 50ms
                    Thread.sleep((long) (Math.random() * 40 + 10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    selectedServer.decrementActiveConnections();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("\n=== Final Simulation Results ===");
        System.out.println("Strategy used: " + activeStrategy.getClass().getSimpleName());
        for (Server s : servers) {
            int handled = requestCounts.getOrDefault(s.getId(), new AtomicInteger(0)).get();
            System.out.printf("%s (Weight: %d): Handled %d requests.%n", s.getId(), s.getWeight(), handled);
        }
    }
}
