package com.dist.cmd;

import com.dist.common.Config;
import com.dist.simplekafka.Broker;
import com.dist.simplekafka.ZookeeperClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BrokerApp {
    private static final Logger logger = Logger.getLogger(BrokerApp.class);

    public static void main(String[] args) {
        BrokerApp app = new BrokerApp();
        app.run(args);
    }

    public void run(String[] args) {
        validateArguments(args);

        String zkAddress = args[0];
        int brokerId = Integer.parseInt(args[1]);

        displayStartupInfo(zkAddress, brokerId);

        try {
            Config config = createBrokerConfig(brokerId, zkAddress);
            ZookeeperClient zkClient = createZookeeperClient(config);

            setupBrokerChangeWatcher(zkClient);

            registerBrokerWithZookeeper(zkClient, brokerId);

            displayCurrentClusterState(zkClient);

            keepBrokerRunning(config, zkClient, brokerId, zkAddress);

        } catch (Exception e) {
            handleError(e);
        }
    }

    private void validateArguments(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java BrokerApp <zookeeper-address> <broker-id>");
            System.out.println("Example: java BrokerApp localhost:2181 1");
            System.exit(1);
        }
    }

    private void displayStartupInfo(String zkAddress, int brokerId) {
        System.out.println("=== Broker Demo Application ===");
        System.out.println("ZooKeeper Address: " + zkAddress);
        System.out.println("Broker ID: " + brokerId);
        System.out.println("================================");
    }

    private Config createBrokerConfig(int brokerId, String zkAddress) throws Exception {
        String logDir = System.getProperty("java.io.tmpdir") + "/broker-" + brokerId;
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        int brokerPort = 9092 + brokerId;

        return new Config(
                brokerId,
                hostAddress,
                brokerPort,
                zkAddress,
                Arrays.asList(logDir)
        );
    }

    private ZookeeperClient createZookeeperClient(Config config) {
        return new ZookeeperClient(config);
    }

    private void setupBrokerChangeWatcher(ZookeeperClient zkClient) {
        IZkChildListener brokerChangeListener = createBrokerChangeListener(zkClient);
        zkClient.subscribeBrokerChangeListener(brokerChangeListener);
    }

    private IZkChildListener createBrokerChangeListener(ZookeeperClient zkClient) {
        return new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                displayBrokerMembershipChange(parentPath, currentChildren, zkClient);
            }
        };
    }

    private void displayBrokerMembershipChange(String parentPath, List<String> currentChildren, ZookeeperClient zkClient) {
        System.out.println("\n=== BROKER MEMBERSHIP CHANGE DETECTED ===");
        System.out.println("Parent Path: " + parentPath);
        System.out.println("Current Brokers: " + currentChildren);

        if (!currentChildren.isEmpty()) {
            displayDetailedBrokerInformation(currentChildren, zkClient);
        }

        System.out.println("========================================\n");
    }

    private void displayDetailedBrokerInformation(List<String> brokerIds, ZookeeperClient zkClient) {
        System.out.println("\nDetailed Broker Information:");
        for (String brokerIdStr : brokerIds) {
            displayBrokerInfo(brokerIdStr, zkClient);
        }
    }

    private void displayBrokerInfo(String brokerIdStr, ZookeeperClient zkClient) {
        try {
            int brokerId = Integer.parseInt(brokerIdStr);
            Broker broker = zkClient.getBrokerInfo(brokerId);
            System.out.println("  Broker " + brokerId + ": " + broker);
        } catch (Exception e) {
            System.out.println("  Broker " + brokerIdStr + ": Error reading info - " + e.getMessage());
        }
    }

    private void registerBrokerWithZookeeper(ZookeeperClient zkClient, int brokerId) throws InterruptedException {
        System.out.println("Registering broker " + brokerId + " with ZooKeeper...");
        zkClient.registerSelf();
        System.out.println("✓ Broker " + brokerId + " registered successfully!");

        // Wait for registration to be processed
        Thread.sleep(1000);
    }

    private void displayCurrentClusterState(ZookeeperClient zkClient) {
        System.out.println("\n=== CURRENT CLUSTER STATE ===");
        Set<Broker> allBrokers = zkClient.getAllBrokers();
        System.out.println("Total brokers in cluster: " + allBrokers.size());

        for (Broker broker : allBrokers) {
            System.out.println("  " + broker);
        }
        System.out.println("=============================\n");
    }

    /**
     * This is a temporary method to keep the broker running.
     * Once we have a SocketServer, we can remove this method.
     */
    private void keepBrokerRunning(Config config, ZookeeperClient zkClient, int brokerId, String zkAddress) throws InterruptedException {
        displayRunningInstructions(config, brokerId, zkAddress);

        while (true) {
            Thread.sleep(5000);
            System.out.println("Broker " + brokerId + " is still alive and watching...");
        }
    }

    private void displayRunningInstructions(Config config, int brokerId, String zkAddress) {
        System.out.println("Broker application is running...");
        System.out.println("Broker ID: " + brokerId);
        System.out.println("Data directory: " + config.getLogDirs());
        System.out.println("This broker will stay registered and watch for membership changes.");
        System.out.println("Press Ctrl+C to exit.");
        System.out.println("You can start other brokers in separate terminals to see watch events.");
        System.out.println("Example: java BrokerApp " + zkAddress + " 2");
    }

    private void handleError(Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
} 