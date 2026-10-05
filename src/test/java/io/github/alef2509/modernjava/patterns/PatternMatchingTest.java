package io.github.alef2509.modernjava.patterns;

import io.github.alef2509.modernjava.patterns.PaymentMethod.BankTransfer;
import io.github.alef2509.modernjava.patterns.PaymentMethod.Card;
import io.github.alef2509.modernjava.patterns.PaymentMethod.DigitalWallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatternMatchingTest {

    @ParameterizedTest(name = "{0} {1}x on 100.00 -> {2}")
    @CsvSource({
            "VISA, 1, 2.00",
            "AMEX, 1, 3.00",
            "VISA, 3, 3.80",
            "AMEX, 2, 4.70"
    })
    void cardFeesDependOnNetworkAndInstallments(String network, int installments, String expectedFee) {
        Payment payment = new Payment(new Card(network, installments), new BigDecimal("100.00"));

        assertThat(FeeCalculator.feeFor(payment)).isEqualByComparingTo(expectedFee);
    }

    @Test
    void bankTransferHasAFlatFee() {
        Payment payment = new Payment(new BankTransfer("DE89370400440532013000"), new BigDecimal("9999.99"));

        assertThat(FeeCalculator.feeFor(payment)).isEqualByComparingTo("0.50");
    }

    @Test
    void walletFeeIsWaivedForThePartnerOnly() {
        assertThat(FeeCalculator.feeFor(new Payment(new DigitalWallet("PARTNER_WALLET"), new BigDecimal("50"))))
                .isEqualByComparingTo("0.00");
        assertThat(FeeCalculator.feeFor(new Payment(new DigitalWallet("OTHER"), new BigDecimal("50"))))
                .isEqualByComparingTo("0.75");
    }

    @Test
    void compactConstructorsValidate() {
        assertThatThrownBy(() -> new Card("VISA", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Payment(new BankTransfer("x"), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void typePatternsWithGuardsAndNull() {
        assertThat(FeeCalculator.describe(null)).isEqualTo("nothing");
        assertThat(FeeCalculator.describe(-1)).isEqualTo("a negative integer");
        assertThat(FeeCalculator.describe(7)).isEqualTo("the integer 7");
        assertThat(FeeCalculator.describe(" ")).isEqualTo("a blank string");
        assertThat(FeeCalculator.describe("hi")).isEqualTo("the text \"hi\"");
        assertThat(FeeCalculator.describe(new BankTransfer("x"))).isEqualTo("a payment method: BankTransfer");
        assertThat(FeeCalculator.describe(1.5)).isEqualTo("something else: Double");
    }

    @Test
    void resultComposesWithoutExceptions() {
        assertThat(Result.parseInt("20").map(n -> n * 2).orElse(-1)).isEqualTo(40);
        assertThat(Result.parseInt("abc").map(n -> n * 2).orElse(-1)).isEqualTo(-1);
        assertThat(Result.parseInt("10").flatMap(n -> n > 5 ? Result.failure("too big") : Result.success(n)))
                .isEqualTo(Result.failure("too big"));
    }
}
