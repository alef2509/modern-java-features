/// Compact source files can still use any API: `java.base` is imported
/// automatically (JEP 511 + 512), so `Files`, `Path`, `Collectors`... need no imports.
///
///     java scripts/TopWords.java README.md 5
void main(String[] args) throws IOException {
    if (args.length < 1) {
        IO.println("usage: java scripts/TopWords.java <file> [limit]");
        return;
    }
    int limit = args.length > 1 ? Integer.parseInt(args[1]) : 10;

    Map<String, Long> counts = Files.readString(Path.of(args[0])).toLowerCase(Locale.ROOT)
            .lines()
            .flatMap(line -> Arrays.stream(line.split("[^\\p{L}]+")))
            .filter(word -> word.length() > 3)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
            .limit(limit)
            .forEach(entry -> IO.println("%5d  %s".formatted(entry.getValue(), entry.getKey())));
}
