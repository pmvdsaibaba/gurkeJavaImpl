package com.myproject.nike.test;

import com.myproject.nike.NIKE;  // Import the NIKE class
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class TestNike {

    @Test
    public void testNike() throws Exception {
        // Step 1: Generate sample data for secret key (sk) and public key (pk)
        byte[] secretKey = new byte[32]; // 256-bit secret key for example
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secretKey);

        // Generate public/private key pair (encapsulation key)
        NIKE.KeyPair keyPair = NIKE.gen(secretKey);
        byte[] publicKey = keyPair.getPublicKey();
        byte[] privateKey = keyPair.getPrivateKey();

        // Step 2: Call NIKE.key() to simulate key exchange (derive the shared key)
        byte[] sharedKey = NIKE.keyExchange(secretKey, publicKey);

        // Step 3: Ensure the result is not null
        assertNotNull(sharedKey, "Shared key should not be null");

        // Step 4: Ensure the shared key is the expected length
        assertEquals(32, sharedKey.length, "Shared key should be 256 bits (32 bytes)");

        // Step 5: Optionally, print the results for verification
        System.out.println("Generated Public Key (pk): ");
        printByteArray(publicKey);
        System.out.println("Generated Private Key (sk): ");
        printByteArray(privateKey);
        System.out.println("Derived Shared Key (k): ");
        printByteArray(sharedKey);
    }

    // Utility method to print byte arrays in a readable format
    private void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
        }
        System.out.println(sb.toString());
    }
}
