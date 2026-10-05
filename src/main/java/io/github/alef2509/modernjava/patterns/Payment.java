package io.github.alef2509.modernjava.patterns;

import java.math.BigDecimal;
import java.util.Objects;

public record Payment(PaymentMethod method, BigDecimal amount) {

    public Payment {
        Objects.requireNonNull(method);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
