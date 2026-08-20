package com.systemdesign.loadbalancer;

import java.util.List;

public class LeastConnectionsStrategy implements LoadBalancingStrategy {

    @Override
    public Server selectServer(List<Server> servers, Request request) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server list can't be empty");
        }

        Server leastConnectedServer = null;
        int minConnections = Integer.MAX_VALUE;

        for (Server server : servers) {
            // Get the current active connections for this server
            int currentConnections = server.getActiveConnections();

            // If it's strictly less than our current minimum, it becomes the new best choice
            if (currentConnections < minConnections) {
                leastConnectedServer = server;
                minConnections = currentConnections;
            }
        }

        System.out.printf("Request %s routed to %s (Active Connections: %d) using Least Connections%n", 
                          request.getId(), leastConnectedServer.getId(), leastConnectedServer.getActiveConnections());

        return leastConnectedServer;
    }
}
