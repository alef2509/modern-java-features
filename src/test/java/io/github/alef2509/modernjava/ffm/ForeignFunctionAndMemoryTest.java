package io.github.alef2509.modernjava.ffm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForeignFunctionAndMemoryTest {

    @Test
    void callsNativeStrlen() {
        assertThat(NativeLibC.strlen("Hello, FFM")).isEqualTo(10);
        assertThat(NativeLibC.strlen("café")).isEqualTo(5); // bytes in UTF-8, not chars
    }

    @Test
    void readsAndWritesOffHeapMemory() {
        assertThat(NativeLibC.sumOffHeap(new int[]{1, 2, 3, 4, 5})).isEqualTo(15);
    }
}
