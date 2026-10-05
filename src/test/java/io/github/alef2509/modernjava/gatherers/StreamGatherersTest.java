package io.github.alef2509.modernjava.gatherers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StreamGatherersTest {

    @Test
    void batchesInFixedWindows() {
        assertThat(StreamGatherers.batches(List.of(1, 2, 3, 4, 5), 2))
                .containsExactly(List.of(1, 2), List.of(3, 4), List.of(5));
    }

    @Test
    void movingAverageOverSlidingWindows() {
        List<BigDecimal> sales = Stream.of("10", "20", "30", "40").map(BigDecimal::new).toList();

        assertThat(StreamGatherers.movingAverage(sales, 3))
                .containsExactly(new BigDecimal("20.00"), new BigDecimal("30.00"));
    }

    @Test
    void runningBalanceEmitsEveryIntermediateState() {
        List<BigDecimal> movements = Stream.of("50", "-20", "5.5").map(BigDecimal::new).toList();

        assertThat(StreamGatherers.runningBalance(new BigDecimal("100"), movements))
                .containsExactly(new BigDecimal("150"), new BigDecimal("130"), new BigDecimal("135.5"));
    }

    @Test
    void mapConcurrentRunsInParallelAndKeepsOrder() {
        long start = System.nanoTime();

        List<Integer> results = StreamGatherers.mapConcurrently(
                IntStream.rangeClosed(1, 20).boxed().toList(), 10, n -> {
                    sleep(Duration.ofMillis(200));
                    return n * n;
                });

        assertThat(results).first().isEqualTo(1);
        assertThat(results).last().isEqualTo(400);
        assertThat(results).isSorted();
        // 20 calls of 200 ms, 10 at a time: about 400 ms instead of 4 s sequentially
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(2_000));
    }

    @Test
    void customGathererKeepsFirstElementPerKey() {
        record Order(String customer, int number) {
        }
        Stream<Order> orders = Stream.of(new Order("ana", 1), new Order("bob", 2), new Order("ana", 3));

        assertThat(StreamGatherers.distinctBy(orders, Order::customer))
                .containsExactly(new Order("ana", 1), new Order("bob", 2));
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
