package io.github.alef2509.modernjava.scopedvalues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/// **Scoped Values** (JEP 506, final in Java 25).
///
/// A safer, cheaper alternative to `ThreadLocal` for passing context (request id,
/// tenant, authenticated user) down a call chain without adding parameters:
/// - the value is **immutable** and bound only for the duration of a call;
/// - it is unbound automatically when the call returns, so nothing leaks;
/// - child threads forked in a `StructuredTaskScope` inherit it at no cost,
///   which matters when there are millions of virtual threads.
public final class RequestContext {

    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private final List<String> auditLog = Collections.synchronizedList(new ArrayList<>());

    /// Entry point, e.g. a servlet filter: binds the request id for this call only.
    public String handle(String requestId, String payload) {
        return ScopedValue.where(REQUEST_ID, requestId).call(() -> process(payload));
    }

    private String process(String payload) {
        // Deep in the call chain, no parameter needed.
        audit("processing " + payload);
        return payload.toUpperCase();
    }

    /// Context is inherited by subtasks forked inside the scope.
    public List<String> handleInParallel(String requestId, List<String> payloads) throws InterruptedException {
        return ScopedValue.where(REQUEST_ID, requestId).call(() -> {
            try (var scope = StructuredTaskScope.open()) {
                var subtasks = payloads.stream().map(p -> scope.fork(() -> process(p))).toList();
                scope.join();
                return subtasks.stream().map(StructuredTaskScope.Subtask::get).toList();
            }
        });
    }

    private void audit(String message) {
        String requestId = REQUEST_ID.isBound() ? REQUEST_ID.get() : "no-request";
        auditLog.add("[" + requestId + "] " + message);
    }

    public List<String> auditLog() {
        return List.copyOf(auditLog);
    }
}
