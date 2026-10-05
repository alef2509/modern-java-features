package io.github.alef2509.modernjava.virtualthreads;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/// **Virtual threads** (JEP 444, final in Java 21).
///
/// A virtual thread is cheap (a few hundred bytes of heap, no OS thread) and is
/// unmounted from its carrier thread while it blocks on I/O, `sleep`, locks...
/// The thread-per-request style that Spring MVC/JDBC code already uses can then
/// scale to tens of thousands of concurrent requests without reactive code.
///
/// Since **Java 24 (JEP 491)** a virtual thread blocking inside `synchronized`
/// no longer *pins* its carrier, which removes the main caveat of Java 21.
public final class BlockingWorkload {

    private BlockingWorkload() {
    }

    /// Runs `tasks` jobs that each block for `blockFor`, simulating calls to a
    /// database or remote API, and returns the wall-clock time.
    public static Duration run(ExecutorService executor, int tasks, Duration blockFor) {
        long start = System.nanoTime();
        try (executor) { // ExecutorService is AutoCloseable since Java 19: close() waits for all tasks
            IntStream.range(0, tasks).forEach(_ -> executor.submit(() -> {
                Thread.sleep(blockFor);
                return null;
            }));
        }
        return Duration.ofNanos(System.nanoTime() - start);
    }

    /// Same workload, but each task blocks while holding its own monitor. On
    /// Java 21 this pinned the carrier thread; on Java 24+ it does not.
    public static Duration runInsideSynchronized(int tasks, Duration blockFor) {
        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, tasks).forEach(_ -> executor.submit(() -> {
                Object monitor = new Object();
                synchronized (monitor) {
                    Thread.sleep(blockFor);
                }
                return null;
            }));
        }
        return Duration.ofNanos(System.nanoTime() - start);
    }

    /// The builder API: named, unstarted or started virtual threads.
    public static boolean runsOnVirtualThread() throws Exception {
        Thread thread = Thread.ofVirtual().name("worker-", 0).unstarted(() -> { });
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Boolean> isVirtual = executor.submit(() -> Thread.currentThread().isVirtual());
            return isVirtual.get() && thread.isVirtual() && thread.getName().equals("worker-0");
        }
    }
}
