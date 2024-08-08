package com.dist.common;

import java.util.List;

public class Config {
    private final int brokerId;
    private final String hostName;
    private final int port;
    private final String zkConnect;
    private final List<String> logDirs;
    private int zkSessionTimeoutMs = 6000;
    private int zkConnectionTimeoutMs = 6000;

    public Config(int brokerId, String hostName, int port, String zkConnect, List<String> logDirs) {
        this.brokerId = brokerId;
        this.hostName = hostName;
        this.port = port;
        this.zkConnect = zkConnect;
        this.logDirs = logDirs;

        // Check for logDirs size here to mimic Scala's require
        if (logDirs.isEmpty()) {
            throw new IllegalArgumentException("logDirs cannot be empty");
        }
    }

    public int getBrokerId() {
        return brokerId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getZkConnect() {
        return zkConnect;
    }

    public List<String> getLogDirs() {
        return logDirs;
    }

    public int getZkSessionTimeoutMs() {
        return zkSessionTimeoutMs;
    }

    public int getZkConnectionTimeoutMs() {
        return zkConnectionTimeoutMs;
    }
}

