package io.github.alef2509.modernjava.patterns;

/// A **closed** set of payment methods.
///
/// `sealed` + `permits` (JEP 409) tells the compiler every possible subtype, so a
/// `switch` over a `PaymentMethod` can be checked for exhaustiveness: add a new
/// method here and every switch that forgot it stops compiling.
public sealed interface PaymentMethod permits PaymentMethod.Card, PaymentMethod.BankTransfer,
        PaymentMethod.DigitalWallet {

    record Card(String network, int installments) implements PaymentMethod {

        /// Compact canonical constructor: validation without repeating the field assignments.
        public Card {
            if (installments < 1) {
                throw new IllegalArgumentException("installments must be at least 1");
            }
        }
    }

    record BankTransfer(String iban) implements PaymentMethod {
    }

    record DigitalWallet(String provider) implements PaymentMethod {
    }
}
