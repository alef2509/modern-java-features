package io.github.alef2509.modernjava.kdf;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class KeyDerivationTest {

    private static final HexFormat HEX = HexFormat.of();

    /// RFC 5869, Appendix A.1 (Test Case 1).
    @Test
    void matchesTheRfc5869TestVector() throws Exception {
        byte[] ikm = HEX.parseHex("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b");
        byte[] salt = HEX.parseHex("000102030405060708090a0b0c");
        byte[] info = HEX.parseHex("f0f1f2f3f4f5f6f7f8f9");

        byte[] okm = KeyDerivation.hkdfSha256(ikm, salt, info, 42);

        assertThat(HEX.formatHex(okm)).isEqualTo(
                "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865");
    }

    @Test
    void differentPurposesYieldIndependentKeys() throws Exception {
        byte[] master = "a-master-secret-from-a-vault".getBytes();
        byte[] salt = "per-deployment-salt".getBytes();

        SecretKey encryption = KeyDerivation.aesKeyFor(master, salt, "encryption");
        SecretKey encryptionAgain = KeyDerivation.aesKeyFor(master, salt, "encryption");
        SecretKey signing = KeyDerivation.aesKeyFor(master, salt, "signing");

        assertThat(encryption.getAlgorithm()).isEqualTo("AES");
        assertThat(encryption.getEncoded()).hasSize(32)
                .isEqualTo(encryptionAgain.getEncoded())
                .isNotEqualTo(signing.getEncoded());
    }
}
