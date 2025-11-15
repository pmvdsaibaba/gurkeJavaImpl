package com.gurkePrj.TestStandardSignature;

import com.gurkePrj.signatureScheme.SignatureScheme;
import com.gurkePrj.signatureScheme.SignatureScheme.KeyPair;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class KeyGenTest {

    @Test
    public void testSignatureGenerationAndVerification() throws Exception {
        // Generate key pair
        KeyPair keyPair1 = SignatureScheme.gen();
        KeyPair keyPair2 = SignatureScheme.gen();

        assertNotNull(keyPair1);
        assertNotNull(keyPair2);

        // Confirm keys are different
        assertFalse(Arrays.equals(keyPair1.getSk(), keyPair2.getSk()), "Private keys should differ");
        assertFalse(Arrays.equals(keyPair1.getVk(), keyPair2.getVk()), "Public keys should differ");

        // Extract keys as byte arrays
        byte[] sk = keyPair1.getSk();
        byte[] vk = keyPair1.getVk();

        // Message to sign
        String msg = "This is a secure message.";
        byte[] message = msg.getBytes(StandardCharsets.UTF_8);

        // Sign the message
        byte[] signature = SignatureScheme.sgn(sk, message);
        assertNotNull(signature, "Signature should not be null");

        // Verify signature
        boolean isValid = SignatureScheme.vfy(vk, message, signature);
        assertTrue(isValid, "Signature must be valid");

        // Tampered message test
        byte[] tamperedMessage = "This is a *tampered* message.".getBytes(StandardCharsets.UTF_8);
        boolean tamperedValid = SignatureScheme.vfy(vk, tamperedMessage, signature);
        assertFalse(tamperedValid, "Tampered signature must be invalid");

        // Optional: Print results
        // System.out.println("Original Message: " + msg);
        // System.out.println("Verification return value: " + isValid);
        // System.out.println("Signature (hex): " + bytesToHex(signature));
        System.out.println("Public key (hex): " + bytesToHex(vk));
        System.out.println("Private key (hex): " + bytesToHex(sk));
        System.out.println("Public key (hex): " + bytesToHex(keyPair2.getSk()));
        System.out.println("Private key (hex): " + bytesToHex(keyPair2.getSk()));
    }

    // Utility function to print byte[] as hex
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
