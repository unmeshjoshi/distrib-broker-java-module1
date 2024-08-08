package com.dist.common;

import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.io.IOException;

public class EmbeddedZookeeper {
    private final String connectString;
    private final File snapshotDir;
    private final File logDir;
    private final int tickTime = 500;
    private final ZooKeeperServer zookeeper;
    private final int port;
    private final NIOServerCnxnFactory factory;

    public EmbeddedZookeeper(String connectString) throws IOException {
        this.connectString = connectString;
        this.snapshotDir = TestUtils.tempDir();
        this.logDir = TestUtils.tempDir();
        this.zookeeper = new ZooKeeperServer(snapshotDir, logDir, tickTime);
        this.port = Integer.parseInt(connectString.split(":")[1]);
        this.factory = new NIOServerCnxnFactory();

        try {
            factory.configure(new InetSocketAddress("127.0.0.1", port), 60);
            factory.startup(zookeeper);
        } catch (Exception e) {
            throw new IOException("Failed to start ZooKeeper server", e);
        }
    }

    public void shutdown() {
        factory.shutdown();
        Utils.rm(logDir);
        Utils.rm(snapshotDir);
    }
}