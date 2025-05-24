package com.myproject.RandomOracle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RandomOracle {

    // Generalized method to accept any number of byte[] inputs
    public static RandomOracleResult H(byte[]... inputs) {
        try {
            // Calculate total length
            int totalLength = 0;
            for (byte[] input : inputs) {
                totalLength += input.length;
            }

            // Concatenate all inputs
            byte[] concatenated = new byte[totalLength];
            int currentPos = 0;
            for (byte[] input : inputs) {
                System.arraycopy(input, 0, concatenated, currentPos, input.length);
                currentPos += input.length;
            }

            // Hash the concatenated input
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(concatenated);

            // Split into seed and key
            byte[] s = Arrays.copyOfRange(hash, 0, 32);
            byte[] k = Arrays.copyOfRange(hash, 32, 64);

            return new RandomOracleResult(s, k);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }

    // Result holder class
    public static class RandomOracleResult {
        byte[] s;
        byte[] k;

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
