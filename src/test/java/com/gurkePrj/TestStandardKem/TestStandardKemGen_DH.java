package com.gurkePrj.TestStandardKem;

import com.gurkePrj.standardKEM.KEM;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Arrays;
import java.util.List;

public class TestStandardKemGen_DH {

    @Test
    public void testNikeGen() throws Exception {

        byte[] seed = new byte[32]; // 256-bit seed
        Arrays.fill(seed, (byte) 0xEF); // Fill with 0xEF
        // Generate (ek, dk1, dk2, ..., dkn) using BK.gen(n)
        KEM.KeyPair keyPair = KEM.gen(seed);
        byte[] ek = keyPair.getEk(); // Extract encapsulation key
        byte[] dk = keyPair.getDk(); // Extract decapsulation key


        assertNotNull(ek, "ek not be null");
        assertNotNull(dk, "Dk should not be null");


        // Step 6: Optionally, print the results for verification
        System.out.println("ek: ");
        printByteArray(ek);
        System.out.println("dk): ");
        printByteArray(dk);

        // Arrays.fill(seed, (byte) 0xEF); // Fill with 0xEF
        keyPair = KEM.gen(seed);
         ek = keyPair.getEk(); // Extract encapsulation key
         dk = keyPair.getDk(); // Extract decapsulation key


        assertNotNull(ek, "ek not be null");
        assertNotNull(dk, "Dk should not be null");


        // Step 6: Optionally, print the results for verification
        System.out.println("ek: ");
        printByteArray(ek);
        System.out.println("dk): ");
        printByteArray(dk);


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
