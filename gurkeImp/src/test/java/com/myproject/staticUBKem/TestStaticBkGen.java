package com.myproject.staticUBKem.test;

import com.myproject.staticUBKem.BKGen;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TestStaticBkGen {

    @Test
    public void testBKGen() {
        int n = 5;  // Example value for n
        
        // Generate (ek, dk1, dk2, ..., dkn) using BK.gen(n)
        List<byte[]> result = BKGen.gen(n);
        
        // Verify that the encapsulation key and decapsulation keys are returned correctly
        assertNotNull(result);
        assertEquals(n + 1, result.size());  // One encapsulation key + n decapsulation keys
        
        // Verify that the first element is the encapsulation key
        byte[] ek = result.get(0);
        assertNotNull(ek);
        System.out.println("Encapsulation Key (ek): ");
        printByteArray(ek);
        
        // Verify that the decapsulation keys are the same
        for (int i = 1; i < result.size(); i++) {
            byte[] dk = result.get(i);
            assertNotNull(dk);
            System.out.println("Decapsulation Key (dk" + i + "): ");
            printByteArray(dk);
            
            // Check that all decapsulation keys are the same
            if (i > 1) {
                assertArrayEquals(dk, result.get(i - 1));  // Ensure dk[i] == dk[i-1]
            }
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
