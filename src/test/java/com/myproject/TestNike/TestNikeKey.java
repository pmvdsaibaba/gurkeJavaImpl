package com.myproject.TestNike.test;

import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;

import java.util.List;

public class TestNikeKey {

    @Test
    public void testNikeKey() throws Exception {
        
        
        byte[] seed1 = new byte[32];  // 32-byte seed (X25519 requires a 32-byte seed)
        for (int i = 0; i < seed1.length; i++) {
            seed1[i] = (byte) 0xEF;
        }

        Nike.KeyPair result = Nike.gen(seed1);
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
        for (int i = 0; i < seed1.length; i++) {
            seed1[i] = (byte) 0xFE;
        }



        Nike.KeyPair result2 = Nike.gen(seed1);
        byte[] ek1 = result2.getEk();
        byte[] dk1 = result2.getDk(); 


        assertNotNull(ek1);
        System.out.println("Nike Encapsulation Key (ek1): ");
        printByteArray(ek1);

        assertNotNull(dk1);
        System.out.println("Nike decapsulation Key (dk1): ");
        printByteArray(dk1);


        byte[] sharedKEy1 = Nike.key(dk, ek1);
        byte[] sharedKEy2 = Nike.key(dk1, ek);

        assertNotNull(sharedKEy1);
        System.out.println("shared key (sharedKEy1): ");
        printByteArray(sharedKEy1);

        assertNotNull(sharedKEy2);
        System.out.println("shared key (sharedKEy2): ");
        printByteArray(sharedKEy2);

        assertArrayEquals(sharedKEy1,sharedKEy2, "Check if shared keys are equal");


        for (int i = 0; i < 3; i++) {
            result2 = Nike.gen(seed1);
             ek1 = result2.getEk();
             dk1 = result2.getDk(); 


            // System.out.println("seed:  ");
            // printByteArray(seed1);
            
            // assertNotNull(ek1);
            // System.out.println("Nike Encapsulation Key (ek1): ");
            // printByteArray(ek1);

            assertNotNull(dk1);
            System.out.println("Nike decapsulation Key (dk1): ");
            printByteArray(dk1);
        }

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
