package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;

public class BKEnc {

    // This method implements the BK.enc function
    public static EncapsulationReturn enc(byte[] ek) throws Exception {
        // Step 03: Use KEM.enc(ek) to get the key and ciphertext
        KEM.EncapsulationResult result = KEM.enc(ek);

        byte[] k = result.getK(); // Extract key (k)
        byte[] c = result.getC(); // Extract ciphertext (c)

        // Step 04: Combine k and c into u
        EncapsulationResult u = new EncapsulationResult(k, c);

        // Step 05: Return both u and c (wrapped in a custom object)
        return new EncapsulationReturn(u, c);  // Returning the EncapsulationReturn object containing both u and c
    }

    // EncapsulationResult class to hold the key and ciphertext
    public static class EncapsulationResult {
        byte[] k;
        byte[] c;

        public EncapsulationResult(byte[] k, byte[] c) {
            this.k = k;
            this.c = c;
        }

        public byte[] getK() {
            return k;
        }

        public byte[] getC() {
            return c;
        }
    }

    // EncapsulationReturn class to hold both u and c
    public static class EncapsulationReturn {
        EncapsulationResult u;
        byte[] c;

        public EncapsulationReturn(EncapsulationResult u, byte[] c) {
            this.u = u;
            this.c = c;
        }

        public EncapsulationResult getU() {
            return u;
        }

        public byte[] getC() {
            return c;
        }
    }
}
