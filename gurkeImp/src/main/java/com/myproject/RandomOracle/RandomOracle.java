package com.myproject.RandomOracle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class RandomOracle {

    // Generalized method with dynamic output splitting
    public static byte[][] H(byte[]... inputs) {
        try {
            // Calculate total length of all inputs
            int totalLength = 0;
            for (byte[] input : inputs) {
                totalLength += input.length;
            }

            // Concatenate all inputs
            byte[] concatenated = new byte[totalLength];
            int pos = 0;
            for (byte[] input : inputs) {
                System.arraycopy(input, 0, concatenated, pos, input.length);
                pos += input.length;
            }

            // Hash the concatenated input
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(concatenated);

            // Make sure hash is long enough to split (if not, hash again)
            while (hash.length < totalLength) {
                // Extend hash deterministically (e.g., rehash current hash)
                hash = Arrays.copyOf(hash, hash.length + 64);
                byte[] extra = digest.digest(hash);  // hash again
                System.arraycopy(extra, 0, hash, hash.length - 64, 64);
            }

            // Split hash into outputs matching each input's length
            byte[][] outputs = new byte[inputs.length][];
            int offset = 0;
            for (int i = 0; i < inputs.length; i++) {
                int len = inputs[i].length;
                outputs[i] = Arrays.copyOfRange(hash, offset, offset + len);
                offset += len;
            }

            return outputs;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }
}
