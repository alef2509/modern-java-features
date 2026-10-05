package io.github.alef2509.modernjava.scopedvalues;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScopedValuesTest {

    private final RequestContext context = new RequestContext();

    @Test
    void valueIsVisibleDeepInTheCallChainWithoutParameters() {
        String result = context.handle("req-42", "order");

        assertThat(result).isEqualTo("ORDER");
        assertThat(context.auditLog()).containsExactly("[req-42] processing order");
    }

    @Test
    void valueIsUnboundOnceTheCallReturns() {
        context.handle("req-42", "order");

        assertThat(RequestContext.REQUEST_ID.isBound()).isFalse();
    }

    @Test
    void forkedSubtasksInheritTheValue() throws Exception {
        List<String> results = context.handleInParallel("req-7", List.of("a", "b", "c"));

        assertThat(results).containsExactly("A", "B", "C");
        assertThat(context.auditLog()).hasSize(3).allMatch(line -> line.startsWith("[req-7]"));
    }
}
