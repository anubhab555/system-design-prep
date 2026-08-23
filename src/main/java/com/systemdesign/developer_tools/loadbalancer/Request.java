package com.systemdesign.developer_tools.loadbalancer;

public class Request {
    private final String id;
    private final String sourceIp;

    public Request(String id, String sourceIp) {
        this.id = id;
        this.sourceIp = sourceIp;
    }

    public String getId() {
        return id;
    }

    public String getSourceIp() {
        return sourceIp;
    }
}
