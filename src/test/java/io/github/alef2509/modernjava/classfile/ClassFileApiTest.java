package io.github.alef2509.modernjava.classfile;

import io.github.alef2509.modernjava.patterns.Payment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassFileApiTest {

    @Test
    void readsMethodsFromBytecode() throws Exception {
        // A record's accessors, equals/hashCode/toString and constructor are all in the bytecode
        assertThat(BytecodeInspector.declaredMethods(Payment.class))
                .contains("<init>", "method", "amount", "equals", "hashCode", "toString");
    }

    @Test
    void generatesLoadsAndInvokesAClassAtRuntime() throws Throwable {
        assertThat(BytecodeInspector.generateAndInvoke()).isEqualTo(42);
    }
}
