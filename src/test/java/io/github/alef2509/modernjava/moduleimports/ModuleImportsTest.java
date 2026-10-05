package io.github.alef2509.modernjava.moduleimports;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleImportsTest {

    @Test
    void countsWordsUsingTypesFromTheImportedModule() {
        String text = "To be, or not to be: that is the question. To BE!";

        assertThat(WordFrequency.top(text, 3))
                .containsExactly(Map.entry("be", 3L), Map.entry("to", 3L), Map.entry("is", 1L));
    }
}
