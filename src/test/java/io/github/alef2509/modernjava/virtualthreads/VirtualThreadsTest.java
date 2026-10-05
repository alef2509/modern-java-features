package io.github.alef2509.modernjava.virtualthreads;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualThreadsTest {

    /// 10,000 tasks blocking 100 ms each. With a pool of 100 platform threads this
    /// needs at least 10,000 / 100 * 100 ms = 10 s. With one virtual thread per
    /// task it takes little more than the 100 ms of a single task.
    @Test
    void tenThousandBlockingTasksFinishAlmostAsFastAsOne() {
        Duration elapsed = BlockingWorkload.run(
                Executors.newVirtualThreadPerTaskExecutor(), 10_000, Duration.ofMillis(100));

        assertThat(elapsed).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void boundedPlatformPoolIsLimitedByItsSize() {
        Duration elapsed = BlockingWorkload.run(Executors.newFixedThreadPool(10), 50, Duration.ofMillis(100));

        assertThat(elapsed).isGreaterThanOrEqualTo(Duration.ofMillis(500)); // 50 / 10 * 100 ms
    }

    /// JEP 491 (Java 24): blocking inside `synchronized` no longer pins the carrier.
    /// On Java 21 these 1,000 tasks would be serialized onto the few carrier threads
    /// (about 1,000 / cores * 100 ms); now they all sleep concurrently.
    @Test
    void synchronizedNoLongerPinsTheCarrierThread() {
        Duration elapsed = BlockingWorkload.runInsideSynchronized(1_000, Duration.ofMillis(100));

        assertThat(elapsed).isLessThan(Duration.ofSeconds(3));
    }

    @Test
    void builderCreatesVirtualThreads() throws Exception {
        assertThat(BlockingWorkload.runsOnVirtualThread()).isTrue();
    }
}
