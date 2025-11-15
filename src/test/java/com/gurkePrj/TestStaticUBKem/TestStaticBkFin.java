package com.gurkePrj.TestStaticUBKem;

import com.gurkePrj.staticUBKem.BKFin;
import com.gurkePrj.staticUBKem.BKFin.FinResult;
import org.junit.jupiter.api.Test;

import com.gurkePrj.staticUBKem.BKGen;
import com.gurkePrj.staticUBKem.BKDec;
import com.gurkePrj.staticUBKem.BKEnc;

import com.gurkePrj.staticUBKem.BKEnc.EncapsulationResult;
import com.gurkePrj.standardKEM.KEM;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.List;

public class TestStaticBkFin {

    @Test
    public void testBKFin() throws Exception {
        // // Step 1: Generate a sample EncapsulationResult (u)
        // SecureRandom random = new SecureRandom();

        // // Generate random k' (key) and c' (ciphertext)
        // byte[] kPrime = new byte[16];
        // byte[] cPrime = new byte[16];
        // random.nextBytes(kPrime);
        // random.nextBytes(cPrime);

        // // Create EncapsulationResult (u)
        // EncapsulationResult u = new EncapsulationResult(kPrime, cPrime);

        // // Step 2: Generate associated data (ad)
        // byte[] ad = new byte[16]; // Example associated data size (16 bytes)
        // random.nextBytes(ad);     // Randomly generate associated data

        byte[] ad = "12345".getBytes(StandardCharsets.UTF_8);
        List<byte[]> Keygen = BKGen.gen(1);
        byte[] ek = Keygen.get(0);
        byte[] dk = Keygen.get(1);

        // Call BK.enc with the encapsulation key (ek)
        BKEnc.EncapsulationReturn EncResult = BKEnc.enc(ek);
        byte[] c = EncResult.getC();
        BKEnc.EncapsulationResult u = EncResult.getU();

        BKDec.DecResult DecResult = BKDec.dec(dk, ad, c);
        byte[] DecK = DecResult.getK();


        // Step 3: Call BKFin.fin() with the EncapsulationResult (u) and associated data (ad)
        FinResult result = BKFin.fin(u, ad);

        // Step 4: Ensure the result is not null
        assertNotNull(result, "Result should not be null");

        // Step 5: Extract ek and k from the result
        byte[] newEk = result.getEk();
        byte[] Fink = result.getK();

        // Step 6: Ensure ek and k are not null
        assertNotNull(newEk, "Encapsulation key (ek) should not be null");
        assertNotNull(Fink, "Key (k) should not be null");

        // Step 7: Optionally, print the results for verification
        System.out.println("Generated Encapsulation Key (ek): ");
        printByteArray(newEk);
        System.out.println("FinK Key (k): ");
        printByteArray(Fink);

        System.out.println("DecK Key (k): ");
        printByteArray(DecK);

        assertArrayEquals(DecK,Fink, "Check Dec key and Fin Key");
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
