package io.github.alef2509.modernjava.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/// Time to complete N tasks that each block for 10 ms (a typical fast DB query or
/// HTTP call), comparing a classic bounded pool of platform threads, a pool as
/// large as the task count, and one virtual thread per task.
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
@Fork(value = 1, jvmArgsAppend = "--enable-preview")
public class VirtualThreadsBenchmark {

    private static final long BLOCKING_MILLIS = 10;

    @Param({"1000", "10000"})
    int tasks;

    @Benchmark
    public void platformThreadPoolOf200() {
        run(Executors.newFixedThreadPool(200));
    }

    @Benchmark
    public void platformThreadPerTask() {
        run(Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory()));
    }

    @Benchmark
    public void virtualThreadPerTask() {
        run(Executors.newVirtualThreadPerTaskExecutor());
    }

    private void run(ExecutorService executor) {
        try (executor) {
            IntStream.range(0, tasks).forEach(_ -> executor.submit(() -> {
                Thread.sleep(BLOCKING_MILLIS);
                return null;
            }));
        }
    }
}
