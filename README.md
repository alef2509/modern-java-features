# Modern Java Features (21 → 25 LTS)

[![CI](https://github.com/alef2509/modern-java-features/actions/workflows/ci.yml/badge.svg)](https://github.com/alef2509/modern-java-features/actions/workflows/ci.yml)
![Java 25](https://img.shields.io/badge/Java-25%20LTS-orange)
![License: MIT](https://img.shields.io/badge/License-MIT-blue)

A tour of the Java language and platform features delivered between the **Java 21 and
Java 25 LTS** releases. Every feature has:

- a small but **realistic** example (fees, payments, product pages, request context),
  not a `Foo`/`Bar` snippet;
- **tests that act as executable documentation**, which prove the claims (for
  example, that 10,000 blocking tasks finish in about the time of one);
- a note on **when to use it** in a typical Spring/backend codebase.

## Feature map

| Feature | JEP | Final in | Example | Test |
|---|---|---|---|---|
| Record patterns, pattern matching for `switch`, sealed types | [440](https://openjdk.org/jeps/440), [441](https://openjdk.org/jeps/441), [409](https://openjdk.org/jeps/409) | 21 | [`FeeCalculator`](src/main/java/io/github/alef2509/modernjava/patterns/FeeCalculator.java), [`Result`](src/main/java/io/github/alef2509/modernjava/patterns/Result.java) | [test](src/test/java/io/github/alef2509/modernjava/patterns/PatternMatchingTest.java) |
| Virtual threads | [444](https://openjdk.org/jeps/444) | 21 | [`BlockingWorkload`](src/main/java/io/github/alef2509/modernjava/virtualthreads/BlockingWorkload.java) | [test](src/test/java/io/github/alef2509/modernjava/virtualthreads/VirtualThreadsTest.java) |
| Sequenced collections | [431](https://openjdk.org/jeps/431) | 21 | [`RecentlyViewed`](src/main/java/io/github/alef2509/modernjava/sequencedcollections/RecentlyViewed.java) | [test](src/test/java/io/github/alef2509/modernjava/sequencedcollections/SequencedCollectionsTest.java) |
| Unnamed variables and patterns (`_`) | [456](https://openjdk.org/jeps/456) | 22 | [`FeeCalculator`](src/main/java/io/github/alef2509/modernjava/patterns/FeeCalculator.java), [`Result`](src/main/java/io/github/alef2509/modernjava/patterns/Result.java) | [test](src/test/java/io/github/alef2509/modernjava/patterns/PatternMatchingTest.java) |
| Foreign Function & Memory API | [454](https://openjdk.org/jeps/454) | 22 | [`NativeLibC`](src/main/java/io/github/alef2509/modernjava/ffm/NativeLibC.java) | [test](src/test/java/io/github/alef2509/modernjava/ffm/ForeignFunctionAndMemoryTest.java) |
| Markdown documentation comments (`///`) | [467](https://openjdk.org/jeps/467) | 23 | used across the whole codebase | — |
| Stream gatherers | [485](https://openjdk.org/jeps/485) | 24 | [`StreamGatherers`](src/main/java/io/github/alef2509/modernjava/gatherers/StreamGatherers.java) | [test](src/test/java/io/github/alef2509/modernjava/gatherers/StreamGatherersTest.java) |
| Class-File API | [484](https://openjdk.org/jeps/484) | 24 | [`BytecodeInspector`](src/main/java/io/github/alef2509/modernjava/classfile/BytecodeInspector.java) | [test](src/test/java/io/github/alef2509/modernjava/classfile/ClassFileApiTest.java) |
| Virtual threads no longer pinned by `synchronized` | [491](https://openjdk.org/jeps/491) | 24 | [`BlockingWorkload`](src/main/java/io/github/alef2509/modernjava/virtualthreads/BlockingWorkload.java) | [test](src/test/java/io/github/alef2509/modernjava/virtualthreads/VirtualThreadsTest.java) |
| Scoped values | [506](https://openjdk.org/jeps/506) | 25 | [`RequestContext`](src/main/java/io/github/alef2509/modernjava/scopedvalues/RequestContext.java) | [test](src/test/java/io/github/alef2509/modernjava/scopedvalues/ScopedValuesTest.java) |
| Flexible constructor bodies | [513](https://openjdk.org/jeps/513) | 25 | [`Rectangle`](src/main/java/io/github/alef2509/modernjava/flexibleconstructors/Rectangle.java) | [test](src/test/java/io/github/alef2509/modernjava/flexibleconstructors/FlexibleConstructorBodiesTest.java) |
| Module import declarations | [511](https://openjdk.org/jeps/511) | 25 | [`WordFrequency`](src/main/java/io/github/alef2509/modernjava/moduleimports/WordFrequency.java) | [test](src/test/java/io/github/alef2509/modernjava/moduleimports/ModuleImportsTest.java) |
| Compact source files and instance `main` | [512](https://openjdk.org/jeps/512) | 25 | [`scripts/`](scripts) | run in CI |
| Key Derivation Function API (HKDF) | [510](https://openjdk.org/jeps/510) | 25 | [`KeyDerivation`](src/main/java/io/github/alef2509/modernjava/kdf/KeyDerivation.java) | [test](src/test/java/io/github/alef2509/modernjava/kdf/KeyDerivationTest.java) (RFC 5869 vector) |
| Structured concurrency | [505](https://openjdk.org/jeps/505) | *preview* | [`ProductPageService`](src/main/java/io/github/alef2509/modernjava/structuredconcurrency/ProductPageService.java) | [test](src/test/java/io/github/alef2509/modernjava/structuredconcurrency/StructuredConcurrencyTest.java) |

## Highlights

### Data-oriented programming: sealed types + records + pattern matching

Business rules become a single exhaustive `switch`. If someone adds a new payment
method, **every switch that does not handle it stops compiling**, which no
`instanceof` chain or visitor can guarantee as cheaply.

```java
return switch (payment) {
    case Payment(Card(var network, var installments), var amount) when installments > 1 ->
            percent(amount, network.equals("AMEX") ? "4.5" : "3.5")
                    .add(new BigDecimal("0.10").multiply(BigDecimal.valueOf(installments)));
    case Payment(Card(var network, _), var amount) -> percent(amount, network.equals("AMEX") ? "3.0" : "2.0");
    case Payment(BankTransfer _, _) -> BANK_TRANSFER_FEE;
    case Payment(DigitalWallet(var provider), _) when provider.equals("PARTNER_WALLET") -> BigDecimal.ZERO;
    case Payment(DigitalWallet _, var amount) -> percent(amount, "1.5");
};   // no default: PaymentMethod is sealed
```

### Virtual threads + structured concurrency + scoped values

These three work together: cheap threads that may block, a safe way to fan out
and cancel, and immutable context that child tasks inherit.

```java
try (var scope = StructuredTaskScope.open()) {
    Subtask<Product> product = scope.fork(() -> backends.product(id));
    Subtask<Integer> stock = scope.fork(() -> backends.stock(id));
    Subtask<List<String>> reviews = scope.fork(() -> backends.reviews(id));
    scope.join();          // first failure cancels the other calls and is rethrown
    return new ProductPage(product.get(), stock.get(), reviews.get());
}
```

The tests check the guarantees: the calls run in parallel, a failing call
**interrupts** its slower siblings, and a deadline applies to the whole operation.

### Stream gatherers

```java
items.stream().gather(Gatherers.windowFixed(500))                  // batches for bulk inserts
sales.stream().gather(Gatherers.windowSliding(7))                  // moving averages
movements.stream().gather(Gatherers.scan(() -> opening, BigDecimal::add))  // running balance
ids.stream().gather(Gatherers.mapConcurrent(10, client::fetch))    // bounded parallel I/O, order kept
```

## Benchmarks

[`VirtualThreadsBenchmark`](src/jmh/java/io/github/alef2509/modernjava/benchmarks/VirtualThreadsBenchmark.java)
measures the time to complete N tasks that each block for 10 ms, which is roughly
a fast DB query or HTTP call.

| Strategy | 1,000 tasks | 10,000 tasks |
|---|---:|---:|
| Fixed pool of 200 platform threads | 109 ms | 571 ms |
| One platform thread per task | 228 ms | 3,424 ms |
| **One virtual thread per task** | **16 ms** | **87 ms** |

<sub>Indicative numbers from a laptop (Intel i7-1165G7, 8 logical cores, WSL2, JDK 25.0.4),
1 fork × 3 measurement iterations, so the error margins are wide. The theoretical floor is
10 ms (every task in parallel). What matters is the trend: the bounded pool queues work
(at least N / 200 × 10 ms), and thousands of OS threads are expensive to create and
schedule, while virtual threads stay close to the floor.</sub>

```bash
./mvnw -P benchmarks package -DskipTests
java --enable-preview -jar target/benchmarks.jar
```

## Running

Requires **JDK 25**. Structured concurrency is a preview API, so the build passes
`--enable-preview`.

```bash
./mvnw verify                       # compile + all tests
java scripts/Hello.java             # compact source file, no compilation step
java scripts/TopWords.java README.md 5
```

## When to use what (backend perspective)

| Need | Reach for |
|---|---|
| Many concurrent blocking calls (JDBC, REST clients) | Virtual threads (`spring.threads.virtual.enabled=true` in Spring Boot 3.2+) |
| Call several services in parallel and fail fast | Structured concurrency (preview), or `CompletableFuture` until it is final |
| Request id / tenant / user propagated through the call chain | Scoped values instead of `ThreadLocal` |
| Closed domain hierarchies (payment methods, events, commands) | Sealed interfaces + records + exhaustive `switch` |
| Batching, windows, running totals, bounded parallel map in streams | Gatherers |
| Deriving per-purpose keys from one secret | KDF API (HKDF) |
| Calling a native library | FFM API instead of JNI |

## License

[MIT](LICENSE)
