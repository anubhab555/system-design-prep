package com.systemdesign.developer_tools.loadbalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PowerOfTwoChoicesStrategy implements LoadBalancingStrategy {

    @Override
    public Server selectServer(List<Server> servers, Request request) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server list can't be empty");
        }
        
        if (servers.size() == 1) {
            return servers.get(0);
        }

        // Randomly pick two distinct servers
        int index1 = ThreadLocalRandom.current().nextInt(servers.size());
        int index2 = ThreadLocalRandom.current().nextInt(servers.size());
        
        // Ensure they are distinct
        while (index1 == index2) {
            index2 = ThreadLocalRandom.current().nextInt(servers.size());
        }

        Server server1 = servers.get(index1);
        Server server2 = servers.get(index2);

        // Pick the one with the least active connections
        Server chosenServer;
        if (server1.getActiveConnections() <= server2.getActiveConnections()) {
            chosenServer = server1;
        } else {
            chosenServer = server2;
        }

        System.out.printf("Request %s routed to %s (Choices were %s:%d and %s:%d) using Power of 2 Choices%n", 
                          request.getId(), 
                          chosenServer.getId(), 
                          server1.getId(), server1.getActiveConnections(), 
                          server2.getId(), server2.getActiveConnections());

        return chosenServer;
    }
}
