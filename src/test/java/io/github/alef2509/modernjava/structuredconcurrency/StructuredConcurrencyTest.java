package io.github.alef2509.modernjava.structuredconcurrency;

import io.github.alef2509.modernjava.structuredconcurrency.ProductPageService.Backends;
import io.github.alef2509.modernjava.structuredconcurrency.ProductPageService.Product;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredConcurrencyTest {

    /// Configurable fake: each backend call sleeps, and may fail.
    static class FakeBackends implements Backends {
        Duration productDelay = Duration.ofMillis(200);
        Duration stockDelay = Duration.ofMillis(200);
        Duration reviewsDelay = Duration.ofMillis(200);
        boolean stockFails;
        final AtomicBoolean reviewsInterrupted = new AtomicBoolean();
        final CountDownLatch reviewsFinished = new CountDownLatch(1);

        @Override
        public Product product(String id) throws Exception {
            Thread.sleep(productDelay);
            return new Product(id, "Mechanical keyboard");
        }

        @Override
        public int stock(String id) throws Exception {
            Thread.sleep(stockDelay);
            if (stockFails) {
                throw new IllegalStateException("inventory service unavailable");
            }
            return 7;
        }

        @Override
        public List<String> reviews(String id) throws Exception {
            try {
                Thread.sleep(reviewsDelay);
                return List.of("Great", "Loud but nice");
            } catch (InterruptedException e) {
                reviewsInterrupted.set(true);
                throw e;
            } finally {
                reviewsFinished.countDown();
            }
        }

        @Override
        public String priceFrom(String mirror) throws Exception {
            Thread.sleep(switch (mirror) {
                case "fast" -> 50;
                case "medium" -> 300;
                default -> 1_000;
            });
            return mirror + ": 89.90";
        }
    }

    private final FakeBackends backends = new FakeBackends();
    private final ProductPageService service = new ProductPageService(backends);

    @Test
    void callsRunInParallel() throws Exception {
        long start = System.nanoTime();

        var page = service.load("p-1");

        assertThat(page.stock()).isEqualTo(7);
        assertThat(page.reviews()).hasSize(2);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(500)); // not 600
    }

    @Test
    void firstFailureCancelsTheSiblingsAndPropagates() throws Exception {
        backends.stockFails = true;
        backends.stockDelay = Duration.ofMillis(50);
        backends.reviewsDelay = Duration.ofSeconds(10);

        assertThatThrownBy(() -> service.load("p-1"))
                .isInstanceOf(StructuredTaskScope.FailedException.class)
                .hasRootCauseMessage("inventory service unavailable");

        assertThat(backends.reviewsFinished.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(backends.reviewsInterrupted).isTrue(); // the slow call did not keep running
    }

    @Test
    void timeoutAppliesToTheWholeOperation() {
        backends.reviewsDelay = Duration.ofSeconds(10);

        assertThatThrownBy(() -> service.load("p-1", Duration.ofMillis(300)))
                .isInstanceOf(StructuredTaskScope.TimeoutException.class);
    }

    @Test
    void raceReturnsTheFirstSuccessfulAnswer() throws Exception {
        long start = System.nanoTime();

        String price = service.fastestPrice(List.of("slow", "medium", "fast"));

        assertThat(price).isEqualTo("fast: 89.90");
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofMillis(800));
    }
}
