package com.dist.simplekafka;

public class Utils {

    public static void swallow(RunnableWithException runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            // Swallow the exception
        }
    }

    @FunctionalInterface
    interface RunnableWithException {
        void run() throws Exception;
    }
}
