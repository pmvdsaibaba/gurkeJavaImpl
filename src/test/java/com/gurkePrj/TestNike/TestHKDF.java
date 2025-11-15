package com.gurkePrj.TestNike.test;

import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class TestHKDF {

    @Test
    public void testHKDFKeyDerivationWithSHA512() {
        byte[] masterKey = "super_secret_key_64_bytes_which_is_sufficient_for_sha512!".getBytes();
        byte[] salt = "fixed_salt_value".getBytes();

        // First derivation
        Map<String, byte[]> keys1 = deriveThreeKeys(masterKey, salt);
        Map<String, byte[]> keys2 = deriveThreeKeys(masterKey, salt);
        Map<String, byte[]> keys3 = deriveThreeKeys(masterKey, salt);

        // Check non-null and print each
        for (String label : new String[]{"encryption", "authentication", "integrity"}) {
            assertNotNull(keys1.get(label));
            assertNotNull(keys2.get(label));
            assertNotNull(keys3.get(label));

            System.out.printf("%s Key [1]: %s%n", label, bytesToHex(keys1.get(label)));
            System.out.printf("%s Key [2]: %s%n", label, bytesToHex(keys2.get(label)));
            System.out.printf("%s Key [3]: %s%n", label, bytesToHex(keys3.get(label)));

            // Ensure deterministic output across calls
            assertArrayEquals(keys1.get(label), keys2.get(label), label + " key not deterministic (1 vs 2)");
            assertArrayEquals(keys1.get(label), keys3.get(label), label + " key not deterministic (1 vs 3)");
        }

        // Optional: Check that keys are distinct from each other
        assertFalse(java.util.Arrays.equals(keys1.get("encryption"), keys1.get("authentication")));
        assertFalse(java.util.Arrays.equals(keys1.get("authentication"), keys1.get("integrity")));
        assertFalse(java.util.Arrays.equals(keys1.get("encryption"), keys1.get("integrity")));
    }

    private Map<String, byte[]> deriveThreeKeys(byte[] masterKey, byte[] salt) {
        Map<String, byte[]> keys = new HashMap<>();
        keys.put("encryption", deriveKey(masterKey, salt, "encryption".getBytes()));
        keys.put("authentication", deriveKey(masterKey, salt, "authentication".getBytes()));
        keys.put("integrity", deriveKey(masterKey, salt, "integrity".getBytes()));
        return keys;
    }

    private byte[] deriveKey(byte[] masterKey, byte[] salt, byte[] info) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        HKDFParameters params = new HKDFParameters(masterKey, salt, info);
        hkdf.init(params);

        byte[] derivedKey = new byte[64]; // 64 bytes = 512-bit key
        hkdf.generateBytes(derivedKey, 0, derivedKey.length);
        return derivedKey;
    }

    public static String bytesToHex(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
