package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;
import com.myproject.RandomOracle.RandomOracle;

import com.myproject.staticUBKem.BKEnc.EncapsulationResult;

import java.util.ArrayList;
import java.util.List;

public class BKFin {

    // This method implements the BK.fin function
    public static FinResult fin(EncapsulationResult u, byte[] ad) throws Exception {
        // Step 06: Extract k′ and c′ from u
        byte[] kPrime = u.getK(); // Extract k′ (key)
        byte[] cPrime = u.getC(); // Extract c′ (ciphertext)


// System.out.println("C in Fin: ");
// StringBuilder sb = new StringBuilder();
// for (byte b : cPrime) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

// System.out.println("K in Fin: ");
//  sb = new StringBuilder();
// for (byte b : kPrime) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

// System.out.println("ad in Fin: ");
//  sb = new StringBuilder();
// for (byte b : ad) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

        // Step 07: Use RandomOracle H(c′, k′, ad) to get s (seed) and k (key)
        // RandomOracle.RandomOracleResult oracleResult = RandomOracle.H(cPrime, kPrime, ad);
        // byte[] s = oracleResult.getS(); // Seed (s)
        // byte[] k = oracleResult.getK(); // Key (k)

    byte[][] oracleResult = RandomOracle.H(cPrime, kPrime, ad);

    byte[] s = oracleResult[0]; // Corresponds to input 'c'
    byte[] k = oracleResult[1]; // Corresponds to input 'kPrime.getK()'



// System.out.println("Seed in Fin: ");
//  sb = new StringBuilder();
// for (byte b : s) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

        // Step 08: Use K.gen(s) to get the encapsulation key (ek)
        KEM.KeyPair keyPair = KEM.gen(s); // Generate new decapsulation key (dk) using the seed s

        byte[] new_ek = keyPair.getEk(); // Extract encapsulation key
        byte[] new_dk = keyPair.getDk(); // Extract decapsulation key

        // Step 09: Return both ek and k
        return new FinResult(new_ek, k); // Return encapsulation key (ek) and key (k)
    }

    // FinResult class to hold the results of BK.fin (ek and k)
    public static class FinResult {
        byte[] ek; // Encapsulation key
        byte[] k;  // Key

        public FinResult(byte[] ek, byte[] k) {
            this.ek = ek;
            this.k = k;
        }

        public byte[] getEk() {
            return ek;
        }

        public byte[] getK() {
            return k;
        }
    }
}
