package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;
import com.myproject.RandomOracle.RandomOracle;

import com.myproject.staticUBKem.BKEnc.EncapsulationResult;

import java.util.ArrayList;
import java.util.List;

public class BKFin {

    // This method implements the BK.fin function
    public static FinResult fin(EncapsulationResult u, byte[] ad) {
        // Step 06: Extract k′ and c′ from u
        byte[] kPrime = u.getK(); // Extract k′ (key)
        byte[] cPrime = u.getC(); // Extract c′ (ciphertext)

        // Step 07: Use RandomOracle H(c′, k′, ad) to get s (seed) and k (key)
        RandomOracle.RandomOracleResult oracleResult = RandomOracle.H(cPrime, kPrime, ad);
        byte[] s = oracleResult.getS(); // Seed (s)
        byte[] k = oracleResult.getK(); // Key (k)

        // Step 08: Use K.gen(s) to get the encapsulation key (ek)
        byte[] ek = KEM.gen(s); // Generate encapsulation key (ek) using the seed s

        // Step 09: Return both ek and k
        return new FinResult(ek, k); // Return encapsulation key (ek) and key (k)
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
