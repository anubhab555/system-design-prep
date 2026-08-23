package com.systemdesign.developer_tools.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinStrategy implements LoadBalancingStrategy {
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Server selectServer(List<Server> servers, Request request){
        if(servers.isEmpty() || servers == null) throw new IllegalArgumentException("Servers list can't be empty");
        System.out.printf("Returning a server for request: %s%n", request.getId());
        int i = Math.floorMod(index.getAndIncrement(), servers.size());
        return servers.get(i);
    }
}
