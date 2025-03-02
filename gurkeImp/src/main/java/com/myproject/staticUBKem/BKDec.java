package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;
import com.myproject.RandomOracle.RandomOracle;

public class BKDec {

    // This method implements the BK.dec function
    public static DecResult dec(byte[] dk, byte[] ad, byte[] c) throws Exception {
        // Step 10: Use K.dec(dk, c) to get k' (key)
        KEM.DecapsulationResult kPrime = KEM.dec(dk, c); // K.dec returns k'

        // Step 11: Use RandomOracle H(c, k', ad) to get s (seed) and k (key)
        RandomOracle.RandomOracleResult oracleResult = RandomOracle.H(c, kPrime.getK(), ad);
        byte[] s = oracleResult.getS(); // Seed (s)
        byte[] k = oracleResult.getK(); // Key (k)

        // Step 12: Use K.gen(s) to get the decapsulation key (dk)
        KEM.KeyPair keyPair = KEM.gen(s); // Generate new decapsulation key (dk) using the seed s

        byte[] new_ek = keyPair.getEk(); // Extract encapsulation key
        byte[] new_dk = keyPair.getDk(); // Extract decapsulation key

        // Step 13: Return both dk and k
        return new DecResult(new_dk, k); // Return decapsulation key (dk) and key (k)
    }

    // DecResult class to hold the results of BK.dec (dk and k)
    public static class DecResult {
        byte[] dk; // Decapsulation key
        byte[] k;  // Key

        public DecResult(byte[] dk, byte[] k) {
            this.dk = dk;
            this.k = k;
        }

        public byte[] getDk() {
            return dk;
        }

        public byte[] getK() {
            return k;
        }
    }
}
