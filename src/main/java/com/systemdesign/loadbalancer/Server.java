package com.systemdesign.loadbalancer;

public class Server {
    private final String id;
    private final String ipAddress;
    private int weight;
    private int activeConnections;

    public Server(String id, String ipAddress, int weight) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.weight = weight;
        this.activeConnections = 0;
    }

    public String getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public void incrementActiveConnections() {
        this.activeConnections++;
    }

    public void decrementActiveConnections() {
        if (this.activeConnections > 0) {
            this.activeConnections--;
        }
    }

    @Override
    public String toString() {
        return "Server{" +
                "id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
