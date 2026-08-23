package com.systemdesign.developer_tools.loadbalancer;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
    private final List<Server> servers;
    private LoadBalancingStrategy strategy;

    public LoadBalancer(LoadBalancingStrategy strategy) {
        this.servers = new ArrayList<>();
        this.strategy = strategy;
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public void removeServer(Server server) {
        servers.remove(server);
    }

    public void setStrategy(LoadBalancingStrategy strategy) {
        this.strategy = strategy;
    }

    public Server routeRequest(Request request) {
        if (servers.isEmpty()) {
            throw new IllegalStateException("No backend servers available.");
        }
        
        Server selectedServer = strategy.selectServer(servers, request);
        System.out.println("Request " + request.getId() + " from IP " + request.getSourceIp() + 
                           " routed to " + selectedServer.getId());
        return selectedServer;
    }
}
