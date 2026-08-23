package com.systemdesign.developer_tools.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeightedRoundRobinStrategy implements LoadBalancingStrategy {

    // Tracks the 'currentWeight' for each server using its ID
    private final Map<String, Integer> currentWeights = new ConcurrentHashMap<>();

    @Override
    public synchronized Server selectServer(List<Server> servers, Request request) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server list can't be empty");
        }

        int totalWeight = 0;
        Server bestServer = null;
        int maxCurrentWeight = Integer.MIN_VALUE;

        // 1. Calculate total weight and update current weights
        for (Server server : servers) {
            int weight = server.getWeight();
            totalWeight += weight;

            // Get existing current weight, default to 0
            int currentWeight = currentWeights.getOrDefault(server.getId(), 0);
            
            // Step 2: Add static weight to current weight
            currentWeight += weight;
            currentWeights.put(server.getId(), currentWeight);

            // Step 3: Keep track of the server with the highest current weight
            if (bestServer == null || currentWeight > maxCurrentWeight) {
                bestServer = server;
                maxCurrentWeight = currentWeight;
            }
        }

        if (bestServer != null) {
            // Step 4: Subtract total weight from the chosen server's current weight
            int chosenServerWeight = currentWeights.get(bestServer.getId());
            currentWeights.put(bestServer.getId(), chosenServerWeight - totalWeight);
            
            System.out.printf("Request %s routed to %s using Weighted Round Robin%n", request.getId(), bestServer.getId());
        }

        return bestServer;
    }
}
