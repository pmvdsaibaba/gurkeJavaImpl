package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;

import java.util.ArrayList;
import java.util.List;

public class BKGen {

    // This method implements the BK.gen(n) function
    public static List<byte[]> gen(int n) {
        // Step 0: Generate (ek, dk) using K.gen
        KEM.KeyPair keyPair = KEM.gen();  // Get the encapsulation key (ek) and decapsulation key (dk)
        byte[] ek = keyPair.getEk(); // Extract encapsulation key
        byte[] dk = keyPair.getDk(); // Extract decapsulation key
        
        // Step 1: Generate n decapsulation keys, all equal to dk
        List<byte[]> decapsulationKeys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            decapsulationKeys.add(dk);  // All dk values are the same
        }
        
        // Step 2: Return (ek, dk1, dk2, ..., dkn)
        List<byte[]> result = new ArrayList<>();
        result.add(ek); // Add encapsulation key to the result
        result.addAll(decapsulationKeys); // Add the decapsulation keys to the result
        
        return result;
    }
}
