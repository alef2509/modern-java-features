package io.github.alef2509.modernjava.patterns;

import io.github.alef2509.modernjava.patterns.PaymentMethod.BankTransfer;
import io.github.alef2509.modernjava.patterns.PaymentMethod.Card;
import io.github.alef2509.modernjava.patterns.PaymentMethod.DigitalWallet;

import java.math.BigDecimal;
import java.math.RoundingMode;

/// Business rules expressed as data-oriented pattern matching.
///
/// Features on display:
/// - **Record patterns** (JEP 440): `Payment(Card(var network, var n), var amount)`
///   deconstructs nested records in one step.
/// - **Pattern matching for switch** (JEP 441) with `when` guards.
/// - **Unnamed patterns** (JEP 456): `_` for components we do not care about.
/// - **Exhaustiveness**: no `default` branch, because `PaymentMethod` is sealed.
///   Before Java 21 this would have been a chain of `instanceof` checks and casts,
///   or a visitor.
public final class FeeCalculator {

    static final BigDecimal BANK_TRANSFER_FEE = new BigDecimal("0.50");

    private FeeCalculator() {
    }

    public static BigDecimal feeFor(Payment payment) {
        BigDecimal fee = switch (payment) {
            case Payment(Card(var network, var installments), var amount) when installments > 1 ->
                    percent(amount, network.equals("AMEX") ? "4.5" : "3.5")
                            .add(new BigDecimal("0.10").multiply(BigDecimal.valueOf(installments)));
            case Payment(Card(var network, _), var amount) -> percent(amount, network.equals("AMEX") ? "3.0" : "2.0");
            case Payment(BankTransfer _, _) -> BANK_TRANSFER_FEE;
            case Payment(DigitalWallet(var provider), _) when provider.equals("PARTNER_WALLET") ->
                    BigDecimal.ZERO;
            case Payment(DigitalWallet _, var amount) -> percent(amount, "1.5");
        };
        return fee.setScale(2, RoundingMode.HALF_EVEN);
    }

    /// Type patterns on an open type (`Object`): here a `default` is mandatory.
    public static String describe(Object value) {
        return switch (value) {
            case null -> "nothing";
            case Integer i when i < 0 -> "a negative integer";
            case Integer i -> "the integer " + i;
            case String s when s.isBlank() -> "a blank string";
            case String s -> "the text \"" + s + "\"";
            case PaymentMethod method -> "a payment method: " + method.getClass().getSimpleName();
            default -> "something else: " + value.getClass().getSimpleName();
        };
    }

    private static BigDecimal percent(BigDecimal amount, String rate) {
        return amount.multiply(new BigDecimal(rate)).movePointLeft(2);
    }
}
