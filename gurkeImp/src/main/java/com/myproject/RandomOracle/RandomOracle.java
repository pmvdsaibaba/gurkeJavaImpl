package com.myproject.RandomOracle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RandomOracle {

    // This method simulates the random oracle H(c′, k′, ad) which produces s (seed) and k (key)
    public static RandomOracleResult H(byte[] c, byte[] k, byte[] ad) {
        try {
            // Concatenate c, k, and ad
            byte[] input = new byte[c.length + k.length + ad.length];
            System.arraycopy(c, 0, input, 0, c.length);
            System.arraycopy(k, 0, input, c.length, k.length);
            System.arraycopy(ad, 0, input, c.length + k.length, ad.length);

            // Perform SHA-256 hashing
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);

            // Split the hash into two parts: s (seed) and k (key)
            byte[] s = Arrays.copyOfRange(hash, 0, 16); // First 16 bytes as the seed
            byte[] kResult = Arrays.copyOfRange(hash, 16, 32); // Next 16 bytes as the key

            return new RandomOracleResult(s, kResult);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // RandomOracleResult class to hold the seed (s) and key (k)
    public static class RandomOracleResult {
        byte[] s;  // seed
        byte[] k;  // key

        public RandomOracleResult(byte[] s, byte[] k) {
            this.s = s;
            this.k = k;
        }

        public byte[] getS() {
            return s;
        }

        public byte[] getK() {
            return k;
        }
    }
}
