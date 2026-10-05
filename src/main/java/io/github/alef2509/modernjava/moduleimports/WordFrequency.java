package io.github.alef2509.modernjava.moduleimports;

import module java.base;

/// **Module Import Declarations** (JEP 511, final in Java 25).
///
/// `import module java.base;` imports every public type of the packages exported
/// by that module: `List`, `Map`, `Collectors`, `Function`, `Pattern`... with a
/// single line. Handy for scripts, prototypes and teaching; in production code
/// explicit imports remain clearer.
public final class WordFrequency {

    private static final Pattern WORD = Pattern.compile("\\p{L}+");

    private WordFrequency() {
    }

    /// The `limit` most frequent words, most frequent first, ties in alphabetical order.
    public static List<Map.Entry<String, Long>> top(String text, int limit) {
        Map<String, Long> counts = WORD.matcher(text.toLowerCase(Locale.ROOT)).results()
                .map(MatchResult::group)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .toList();
    }
}
