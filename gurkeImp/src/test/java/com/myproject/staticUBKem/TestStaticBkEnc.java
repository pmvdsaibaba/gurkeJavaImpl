
package com.myproject.staticUBKem.test;

import com.myproject.staticUBKem.BKEnc;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

public class TestStaticBkEnc {

    @Test
    public void testBKEnc() {
        // Simulate the encapsulation key (ek), in this case, we just use random bytes
        SecureRandom random = new SecureRandom();
        byte[] ek = new byte[16];  // Example encapsulation key size (16 bytes)
        random.nextBytes(ek);      // Randomly generate the encapsulation key

        // Call BK.enc with the encapsulation key (ek)
        BKEnc.EncapsulationReturn result = BKEnc.enc(ek);
        
        // Ensure the result is not null
        assertNotNull(result);

        // Extract the EncapsulationResult (u) and ciphertext (c)
        BKEnc.EncapsulationResult u = result.getU();
        byte[] c = result.getC();

        // Ensure that u and c are not null
        assertNotNull(u);
        assertNotNull(c);

        // Optionally, print the key and ciphertext
        System.out.println("Generated Encapsulation Result (u) - Key (k): ");
        printByteArray(u.getK());
        System.out.println("Generated Encapsulation Result (u) - Ciphertext (c): ");
        printByteArray(u.getC());
        System.out.println("Ciphertext (c) directly from EncapsulationReturn: ");
        printByteArray(c);
    }

    // Utility method to print byte arrays in a readable format
    public static void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
        }
        System.out.println(sb.toString());
    }
}

