package io.github.alef2509.modernjava.gatherers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/// **Stream Gatherers** (JEP 485, final in Java 24).
///
/// `Stream::gather` is to intermediate operations what `Stream::collect` is to
/// terminal ones: an extension point. It enables stateful, order-aware steps that
/// were awkward or impossible with `map`/`filter`/`flatMap`: windows, running
/// totals, de-duplication by key, bounded concurrency...
public final class StreamGatherers {

    private StreamGatherers() {
    }

    /// Split work into fixed-size batches, e.g. bulk inserts of 500 rows.
    public static <T> List<List<T>> batches(List<T> items, int batchSize) {
        return items.stream().gather(Gatherers.windowFixed(batchSize)).toList();
    }

    /// Moving average over a sliding window, e.g. a 7-day average of daily sales.
    public static List<BigDecimal> movingAverage(List<BigDecimal> values, int window) {
        return values.stream()
                .gather(Gatherers.windowSliding(window))
                .map(w -> w.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(w.size()), 2, RoundingMode.HALF_EVEN))
                .toList();
    }

    /// Running balance: every intermediate result of a fold.
    public static List<BigDecimal> runningBalance(BigDecimal opening, List<BigDecimal> movements) {
        return movements.stream().gather(Gatherers.scan(() -> opening, BigDecimal::add)).toList();
    }

    /// Calls a blocking function with at most `maxConcurrency` calls in flight, on
    /// virtual threads, **preserving the input order** of the results.
    public static <T, R> List<R> mapConcurrently(List<T> inputs, int maxConcurrency, Function<T, R> call) {
        return inputs.stream().gather(Gatherers.mapConcurrent(maxConcurrency, call::apply)).toList();
    }

    /// A custom gatherer: keeps the first element for each key, like a
    /// `distinct()` that compares only part of the element.
    public static <T, K> Gatherer<T, ?, T> distinctBy(Function<? super T, ? extends K> keyExtractor) {
        return Gatherer.<T, Set<K>, T>ofSequential(
                HashSet::new,
                Gatherer.Integrator.ofGreedy((seen, element, downstream) ->
                        !seen.add(keyExtractor.apply(element)) || downstream.push(element)));
    }

    public static <T, K> List<T> distinctBy(Stream<T> stream, Function<? super T, ? extends K> keyExtractor) {
        return stream.gather(distinctBy(keyExtractor)).toList();
    }
}
