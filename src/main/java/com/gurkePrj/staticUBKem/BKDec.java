package com.gurkePrj.staticUBKem;
import com.gurkePrj.standardKEM.KEM;
import com.gurkePrj.RandomOracle.RandomOracle;

public class BKDec {

    // This method implements the BK.dec function
    public static DecResult dec(byte[] dk, byte[] ad, byte[] c) throws Exception {

        KEM.DecapsulationResult kPrime = KEM.dec(dk, c);

// System.out.println("C in dec: ");
// StringBuilder sb = new StringBuilder();
// for (byte b : c) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

// System.out.println("K in dec: ");
//  sb = new StringBuilder();
// for (byte b : kPrime.getK()) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

// System.out.println("ad in dec: ");
//  sb = new StringBuilder();
// for (byte b : ad) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

        RandomOracle.RandomOracleResult oracleResult = RandomOracle.H(c, kPrime.getK(), ad);
        byte[] s = oracleResult.getS();
        byte[] k = oracleResult.getK();



// System.out.println("Seed in dec: ");
//  sb = new StringBuilder();
// for (byte b : s) {
//     sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
// }
// System.out.println(sb.toString());

        KEM.KeyPair keyPair = KEM.gen(s); // Generate new decapsulation key (dk) using the seed s

        byte[] new_ek = keyPair.getEk();
        byte[] new_dk = keyPair.getDk();

        return new DecResult(new_dk, k);
    }

    // DecResult class to hold the results of BK.dec (dk and k)
    public static class DecResult {
        byte[] dk;
        byte[] k;

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
