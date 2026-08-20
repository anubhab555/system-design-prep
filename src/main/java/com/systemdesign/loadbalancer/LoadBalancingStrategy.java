package com.systemdesign.loadbalancer;

import java.util.List;

public interface LoadBalancingStrategy {
    /**
     * Selects a server from the available list to handle the incoming request.
     *
     * @param servers The list of available healthy servers.
     * @param request The incoming client request.
     * @return The selected Server, or null if no servers are available.
     */
    Server selectServer(List<Server> servers, Request request);
}
