package me.yamayaki.musicbot.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Threads {
    private static final int optimalThreadCount;

    private static final ExecutorService mainWorker;
    private static final ExecutorService ioWorker;

    static {
        optimalThreadCount = Math.max(Runtime.getRuntime().availableProcessors() / 2, 4);

        mainWorker = getPool(optimalThreadCount, "Main-Worker");
        ioWorker = getPool(1, "IO-Worker");
    }

    private static ExecutorService getPool(final int threadCount, final String poolName) {
        return Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
            final AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                return new Thread(runnable, poolName + " #" + atomicInteger.getAndIncrement());
            }
        });
    }

    public static String threadToString(Thread thread) {
        return String.format("%d %s: %s", thread.getId(), thread.getName(), thread.getState().name());
    }

    public static ExecutorService mainWorker() {
        return mainWorker;
    }

    public static ExecutorService ioWorker() {
        return ioWorker;
    }
}
