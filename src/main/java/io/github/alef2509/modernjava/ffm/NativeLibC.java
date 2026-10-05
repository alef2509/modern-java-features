package io.github.alef2509.modernjava.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/// **Foreign Function & Memory API** (JEP 454, final in Java 22).
///
/// The modern replacement for JNI: call native code and manage off-heap memory
/// from pure Java, with no C glue code, and with memory that is freed
/// deterministically when its `Arena` closes. Run with
/// `--enable-native-access=ALL-UNNAMED` to avoid the restricted-method warning.
public final class NativeLibC {

    private static final Linker LINKER = Linker.nativeLinker();

    /// `size_t strlen(const char *s)` from the C standard library.
    private static final MethodHandle STRLEN = LINKER.downcallHandle(
            LINKER.defaultLookup().find("strlen").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

    private NativeLibC() {
    }

    public static long strlen(String text) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cString = arena.allocateFrom(text); // UTF-8, NUL-terminated, off-heap
            return (long) STRLEN.invokeExact(cString);
        } catch (Throwable e) {
            throw new IllegalStateException("native call failed", e);
        }
    }

    /// Off-heap array: allocated outside the Java heap (no GC pressure), freed when the arena closes.
    public static long sumOffHeap(int[] values) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_INT, values);
            long sum = 0;
            for (long i = 0; i < values.length; i++) {
                sum += segment.getAtIndex(ValueLayout.JAVA_INT, i);
            }
            return sum;
        }
    }
}
