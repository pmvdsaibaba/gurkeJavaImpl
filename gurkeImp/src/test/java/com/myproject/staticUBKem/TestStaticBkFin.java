package com.myproject.staticUBKem.test;

import com.myproject.staticUBKem.BKFin;
import com.myproject.staticUBKem.BKFin.FinResult;
import org.junit.jupiter.api.Test;

import com.myproject.staticUBKem.BKEnc.EncapsulationResult;  // Import EncapsulationResult from BKEnc
import com.myproject.standardKEM.KEM;  // Import K class


import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

public class TestStaticBkFin {

    @Test
    public void testBKFin() throws Exception {
        // Step 1: Generate a sample EncapsulationResult (u)
        SecureRandom random = new SecureRandom();

        // Generate random k' (key) and c' (ciphertext)
        byte[] kPrime = new byte[16];
        byte[] cPrime = new byte[16];
        random.nextBytes(kPrime);
        random.nextBytes(cPrime);

        // Create EncapsulationResult (u)
        EncapsulationResult u = new EncapsulationResult(kPrime, cPrime);

        // Step 2: Generate associated data (ad)
        byte[] ad = new byte[16]; // Example associated data size (16 bytes)
        random.nextBytes(ad);     // Randomly generate associated data

        // Step 3: Call BKFin.fin() with the EncapsulationResult (u) and associated data (ad)
        FinResult result = BKFin.fin(u, ad);

        // Step 4: Ensure the result is not null
        assertNotNull(result, "Result should not be null");

        // Step 5: Extract ek and k from the result
        byte[] ek = result.getEk();
        byte[] k = result.getK();

        // Step 6: Ensure ek and k are not null
        assertNotNull(ek, "Encapsulation key (ek) should not be null");
        assertNotNull(k, "Key (k) should not be null");

        // Step 7: Optionally, print the results for verification
        System.out.println("Generated Encapsulation Key (ek): ");
        printByteArray(ek);
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
