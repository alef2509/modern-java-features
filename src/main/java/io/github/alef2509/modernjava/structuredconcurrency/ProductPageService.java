package io.github.alef2509.modernjava.structuredconcurrency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;

/// **Structured Concurrency** (JEP 505, *preview* in Java 25, needs `--enable-preview`).
///
/// Concurrent subtasks are treated as a unit whose lifetime is bounded by a
/// lexical scope, like a `try` block:
/// - if one subtask fails, the others are cancelled (no leaked threads);
/// - if the caller is interrupted or times out, all subtasks are cancelled;
/// - thread dumps show the parent/child relationship.
///
/// Compare with `CompletableFuture.allOf(...)`, where a failure in one future
/// leaves the others running in the background.
public final class ProductPageService {

    public record Product(String id, String name) {
    }

    public record ProductPage(Product product, int stock, List<String> reviews) {
    }

    /// Remote dependencies of the page; each call blocks (on a virtual thread).
    public interface Backends {

        Product product(String id) throws Exception;

        int stock(String id) throws Exception;

        List<String> reviews(String id) throws Exception;

        String priceFrom(String mirror) throws Exception;
    }

    private final Backends backends;

    public ProductPageService(Backends backends) {
        this.backends = backends;
    }

    /// Fan-out / fan-in: all three calls run in parallel; the page is built only
    /// if all succeed. The first failure cancels the remaining calls and is
    /// rethrown wrapped in `StructuredTaskScope.FailedException`.
    public ProductPage load(String productId) throws InterruptedException {
        try (var scope = StructuredTaskScope.open()) {
            Subtask<Product> product = scope.fork(() -> backends.product(productId));
            Subtask<Integer> stock = scope.fork(() -> backends.stock(productId));
            Subtask<List<String>> reviews = scope.fork(() -> backends.reviews(productId));

            scope.join();

            return new ProductPage(product.get(), stock.get(), reviews.get());
        }
    }

    /// Same fan-out with a deadline for the whole operation, not per call.
    public ProductPage load(String productId, Duration timeout) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow(),
                config -> config.withTimeout(timeout))) {
            Subtask<Product> product = scope.fork(() -> backends.product(productId));
            Subtask<Integer> stock = scope.fork(() -> backends.stock(productId));
            Subtask<List<String>> reviews = scope.fork(() -> backends.reviews(productId));

            scope.join();

            return new ProductPage(product.get(), stock.get(), reviews.get());
        }
    }

    /// Race: ask every mirror and keep the first successful answer; the slower
    /// requests are cancelled as soon as one wins.
    public String fastestPrice(List<String> mirrors) throws InterruptedException {
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            mirrors.forEach(mirror -> scope.fork(() -> backends.priceFrom(mirror)));
            return scope.join();
        }
    }
}
