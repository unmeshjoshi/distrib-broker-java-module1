package com.dist.common;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    public static String hostName() {
        return new Networks().hostname();
    }

    /**
     * Choose a number of random available ports
     */
    public static List<Integer> choosePorts(int count) {
        List<ServerSocket> sockets = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                ServerSocket socket = new ServerSocket(0);
                sockets.add(socket);
                ports.add(socket.getLocalPort());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to open server socket", e);
        } finally {
            for (ServerSocket socket : sockets) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return ports;
    }

    /**
     * Choose an available port
     */
    public static int choosePort() {
        return choosePorts(1).get(0);
    }

    public static File tempDirWithName(String name) {
        String ioDir = System.getProperty("java.io.tmpdir");
        File f = new File(ioDir, "kafka-" + name);
        f.mkdirs();
        // f.deleteOnExit();
        return f;
    }

    public static File tempDir(String prefix) {
        String ioDir = System.getProperty("java.io.tmpdir");
        File f = new File(ioDir, prefix + random.nextInt(1000000));
        f.mkdirs();
        f.deleteOnExit();
        return f;
    }

    public static File tempDir() {
        return tempDir("kafka-");
    }

    /**
     * Create a temporary file
     */
    public static File tempFile() throws IOException {
        File f = File.createTempFile("kafka", ".tmp");
        f.deleteOnExit();
        return f;
    }

    private static final long DEFAULT_MAX_WAIT_MS = 1000;

    /**
     * Wait until the given condition is true or throw an exception if the given wait time elapses.
     *
     * @param condition condition to check
     * @param msg error message
     * @param waitTimeMs maximum time to wait and retest the condition before failing the test
     * @param pause delay between condition checks
     */
    public static void waitUntilTrue(Condition condition, String msg, long waitTimeMs, long pause) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (condition.check()) {
                return;
            }
            if (System.currentTimeMillis() > startTime + waitTimeMs) {
                throw new AssertionError(msg);
            }
            try {
                Thread.sleep(Math.min(waitTimeMs, pause));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for condition", e);
            }
        }
    }

    public static void waitUntilTrue(Condition condition, String msg) {
        waitUntilTrue(condition, msg, DEFAULT_MAX_WAIT_MS, 100L);
    }

    @FunctionalInterface
    public interface Condition {
        boolean check();
    }
}