package io.github.alef2509.modernjava.sequencedcollections;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

/// **Sequenced Collections** (JEP 431, final in Java 21).
///
/// `List`, `Deque`, `LinkedHashSet`, `LinkedHashMap`, `TreeSet`... now share
/// `SequencedCollection` / `SequencedMap`, with uniform `getFirst()`, `getLast()`,
/// `addFirst()`, `removeLast()` and a `reversed()` *view*. Before, getting the last
/// element of a `LinkedHashSet` meant iterating over the whole set.
///
/// Example: a "recently viewed products" list, most recent first, without duplicates.
public final class RecentlyViewed<T> {

    private final SequencedSet<T> items = new LinkedHashSet<>();
    private final int capacity;

    public RecentlyViewed(int capacity) {
        this.capacity = capacity;
    }

    public void view(T item) {
        items.addFirst(item); // LinkedHashSet.addFirst moves an existing element to the front
        if (items.size() > capacity) {
            items.removeLast();
        }
    }

    public T mostRecent() {
        return items.getFirst();
    }

    public List<T> newestFirst() {
        return List.copyOf(items);
    }

    public List<T> oldestFirst() {
        return List.copyOf(items.reversed());
    }
}
