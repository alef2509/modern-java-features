package io.github.alef2509.modernjava.kdf;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.AlgorithmParameterSpec;

/// **Key Derivation Function API** (JEP 510, final in Java 25).
///
/// A standard JCA API for KDFs such as HKDF (RFC 5869): derive several
/// independent keys (encryption, MAC, per-tenant keys...) from one master secret,
/// without hand-rolling HMAC chains.
public final class KeyDerivation {

    private KeyDerivation() {
    }

    public static byte[] hkdfSha256(byte[] inputKeyMaterial, byte[] salt, byte[] info, int length)
            throws GeneralSecurityException {
        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        AlgorithmParameterSpec params = HKDFParameterSpec.ofExtract()
                .addIKM(inputKeyMaterial)
                .addSalt(salt)
                .thenExpand(info, length);
        return hkdf.deriveData(params);
    }

    /// Derives a ready-to-use AES-256 key for a given purpose (the HKDF "info").
    public static SecretKey aesKeyFor(byte[] masterSecret, byte[] salt, String purpose)
            throws GeneralSecurityException {
        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        AlgorithmParameterSpec params = HKDFParameterSpec.ofExtract()
                .addIKM(masterSecret)
                .addSalt(salt)
                .thenExpand(purpose.getBytes(StandardCharsets.UTF_8), 32);
        return hkdf.deriveKey("AES", params);
    }
}
