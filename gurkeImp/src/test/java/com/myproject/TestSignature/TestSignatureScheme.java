package com.myproject.TestStandardSignature;

import com.myproject.signatureScheme.SignatureScheme;
import com.myproject.signatureScheme.SignatureScheme.KeyPair;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestSignatureScheme {


    @Test
    public void testSignatureGenerationAndVerification() throws Exception {
        // Generate key pair
        KeyPair keyPair = SignatureScheme.gen();
        assertNotNull(keyPair);

        // Extract keys
        PrivateKey sk = keyPair.getSk();
        PublicKey vk = keyPair.getVk();

        // Message to sign
        String msg = "This is a secure message.";
        byte[] message = msg.getBytes(StandardCharsets.UTF_8);

        // Sign the message
        byte[] signature = SignatureScheme.sgn(sk, message);
        assertNotNull(signature, "Signature should not be null");

        // msg = "This is a secure message...";
        // message = msg.getBytes(StandardCharsets.UTF_8);

        // Verify signature
        boolean isValid = SignatureScheme.vfy(vk, message, signature);
        assertTrue(isValid, "Signature must be valid");

        // Tampered message test
        byte[] tamperedMessage = "This is a *tampered* message.".getBytes(StandardCharsets.UTF_8);
        boolean tamperedValid = SignatureScheme.vfy(vk, tamperedMessage, signature);
        assertFalse(tamperedValid, "Tampered signature must be invalid");

        // Optional: Print results
        System.out.println("Original Message: " + msg);
        System.out.println("Original Message: " + isValid);
        System.out.println("Signature (hex): " + bytesToHex(signature));
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
