package com.myproject.TestNike.test;

import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;

import java.util.List;

public class TestNikeGen {

    @Test
    public void testNikeGen() throws Exception {
        
        Nike.KeyPair result = Nike.gen();
        byte[] ek = result.getEk();
        byte[] dk = result.getDk(); 

        // Verify that the encapsulation key and decapsulation keys are returned correctly
        assertNotNull(ek);

        System.out.println("Nike Encapsulation Key (ek): ");
        printByteArray(ek);

        assertNotNull(dk);

        System.out.println("Nike decapsulation Key (dk): ");
        printByteArray(dk);

        // test Gen with seed

        byte[] seed = new byte[32];  // 32-byte seed (X25519 requires a 32-byte seed)
        for (int i = 0; i < seed.length; i++) {
            seed[i] = (byte) 0xEF;  // Fill with 0xEF (EFEFEFE pattern)
        }
        //         // Define a seed (for reproducible key generation)
        // byte[] seed = new byte[32];  // 32-byte seed (X25519 requires a 32-byte seed)
        // // Example: you can fill the seed with any byte values, or get it from a source
        // new SecureRandom().nextBytes(seed);  

        Nike.KeyPair result2 = Nike.gen(seed);


        ek = result2.getEk();
        dk = result2.getDk(); 

        assertNotNull(ek);
        System.out.println("Nike Encapsulation Key (ek): ");
        printByteArray(ek);

        assertNotNull(dk);
        System.out.println("Nike decapsulation Key (dk): ");
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
