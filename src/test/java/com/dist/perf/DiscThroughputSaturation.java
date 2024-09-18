package com.dist.perf;

import com.dist.common.TestUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class DiscThroughputSaturation {
    private static final String TEST_FILE_NAME = "testfile.bin";
    private static final int WRITE_SIZE = 1024; // Size of each write in bytes (1 KB)
    private static final Duration SIMULATION_DURATION = Duration.ofSeconds(10);
    // Fixed number of worker threads (servers)
    private static final int NUM_SERVERS = 1;

    public static void main(String[] args) throws InterruptedException, IOException {
        // Fixed thread pool to represent the servers
        ExecutorService executor = Executors.newFixedThreadPool(NUM_SERVERS);

        File file = createFile(TEST_FILE_NAME);
        byte[] data = createData(WRITE_SIZE);
        FileChannel fos = openChannel(file);

        System.out.printf("Writing data to = %s", file.getAbsolutePath());

        // Generate requests at increasing rate
        for (int requestRate = 100; requestRate <= 1500; requestRate += 100) {

            // Process requests for SIMULATION_DURATION seconds
            int totalProcessed = submitWrites(requestRate, executor,
                    data, fos);

            // Calculate throughput
            System.out.printf("Request Rate: %d, Processed Requests: %d, Throughput: %.2f requests/sec%n",
                    requestRate, totalProcessed,
                    totalProcessed / (double) SIMULATION_DURATION.getSeconds());
        }

        // Shut down the executor service
        List<Runnable> runnables = executor.shutdownNow();
        System.out.println("Tasks still pending = " + runnables.size());
    }

    static AtomicInteger processedRequests = new AtomicInteger(0);

    private static int submitWrites(int requestRate, ExecutorService executor, byte[] data, FileChannel fos) throws InterruptedException {

        Instant startTime = Instant.now();
        Instant endTime = startTime.plus(SIMULATION_DURATION);
        processedRequests.set(0); //reset counting of processed requests
        // Shared counter for total processed requests
        while (Instant.now().isBefore(endTime)) { //for the duration, submit
            // requests to simulate arrivals per second.
            submitWrites(requestRate, executor, data, fos, processedRequests);
            // Sleep to simulate the time between arrivals (based on request rate)
            waitOneSecond();
        }
        return processedRequests.get();
    }

    private static void waitOneSecond() throws InterruptedException {
        Thread.sleep(Duration.ofSeconds(1).toMillis());
    }

    private static void submitWrites(int requestRate, ExecutorService executor, byte[] data, FileChannel fos, AtomicInteger processedRequests) {
        // Simulate arrival of new requests at the given rate
        for (int i = 0; i < requestRate; i++) {
            executor.submit(()-> {
                try {
                    fos.write(ByteBuffer.wrap(data));
                    syncToPhysicalMedia(fos);
                    processedRequests.incrementAndGet();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    private static void syncToPhysicalMedia(FileChannel fos) {
        try {
            fos.force(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FileChannel openChannel(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        return randomAccessFile.getChannel();
    }

    private static byte[] createData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = 'A';
        }
        return data;
    }

    private static File createFile(String fileName) {
        return new File(TestUtils.tempDir("perf"), fileName);
    }
}