package io.github.alef2509.modernjava.sequencedcollections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

import static org.assertj.core.api.Assertions.assertThat;

class SequencedCollectionsTest {

    @Test
    void uniformAccessToBothEnds() {
        List<String> list = new ArrayList<>(List.of("b", "c"));
        list.addFirst("a");
        list.addLast("d");

        assertThat(list.getFirst()).isEqualTo("a");
        assertThat(list.getLast()).isEqualTo("d");
        assertThat(list.reversed()).containsExactly("d", "c", "b", "a");
    }

    @Test
    void sequencedMapsExposeFirstAndLastEntries() {
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("b", 2);
        map.put("c", 3);
        map.putFirst("a", 1);

        assertThat(map.firstEntry()).isEqualTo(Map.entry("a", 1));
        assertThat(map.pollLastEntry()).isEqualTo(Map.entry("c", 3));
        assertThat(map.reversed().keySet()).containsExactly("b", "a");
    }

    @Test
    void recentlyViewedKeepsMostRecentFirstWithoutDuplicates() {
        RecentlyViewed<String> recent = new RecentlyViewed<>(3);
        List.of("keyboard", "mouse", "monitor", "mouse", "headset").forEach(recent::view);

        assertThat(recent.mostRecent()).isEqualTo("headset");
        assertThat(recent.newestFirst()).containsExactly("headset", "mouse", "monitor");
        assertThat(recent.oldestFirst()).containsExactly("monitor", "mouse", "headset");
    }
}
