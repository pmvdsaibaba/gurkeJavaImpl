package com.myproject.staticUBKem.test;

import com.myproject.staticUBKem.BKDec;  // Import the BKDec class
import com.myproject.standardKEM.KEM;      // Import the K class
import com.myproject.RandomOracle.RandomOracle;  // Import the RandomOracle class
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

public class TestStaticBkDec {

    @Test
    public void testBKDec() throws Exception {
        // Step 1: Generate sample data for dk, ad, and c
        SecureRandom random = new SecureRandom();

        // Generate random decapsulation key dk (16 bytes for example)
        byte[] dk = new byte[16];
        random.nextBytes(dk);

        // Generate associated data (ad)
        byte[] ad = new byte[16]; // Example associated data size (16 bytes)
        random.nextBytes(ad);

        // Generate random ciphertext (c)
        byte[] c = new byte[16]; // Example ciphertext size (16 bytes)
        random.nextBytes(c);

        // Step 2: Call BKDec.dec() with the dk, ad, and c
        BKDec.DecResult result = BKDec.dec(dk, ad, c);

        // Step 3: Ensure the result is not null
        assertNotNull(result, "Result should not be null");

        // Step 4: Extract dk and k from the result
        byte[] newDk = result.getDk();
        byte[] k = result.getK();

        // Step 5: Ensure dk and k are not null
        assertNotNull(newDk, "Decapsulation key (dk) should not be null");
        assertNotNull(k, "Key (k) should not be null");

        // Step 6: Optionally, print the results for verification
        System.out.println("Generated Decapsulation Key (dk): ");
        printByteArray(newDk);
        System.out.println("Generated Key (k): ");
        printByteArray(k);
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
