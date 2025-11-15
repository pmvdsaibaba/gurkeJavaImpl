package com.gurkePrj.RandomOracle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;

import java.util.HashMap;
import java.util.Map;


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
            // byte[] s = Arrays.copyOfRange(hash, 0, 32);
            // byte[] k = Arrays.copyOfRange(hash, 32, 64);
            byte[] seed = "seed".getBytes();
            byte[] key_k = "key_k".getBytes();
            byte[] s = deriveKey(hash, seed);
            byte[] k = deriveKey(hash, key_k);;

            return new RandomOracleResult(s, k);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }


    public static byte[] Hash2(byte[]... inputs) {
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

            return hash;

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


    private static byte[] deriveKey(byte[] masterKey, byte[] info)
    {
        byte[] salt = new byte[] {0x01, 0x02, 0x03, 0x04};
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        HKDFParameters params = new HKDFParameters(masterKey, salt, info);
        hkdf.init(params);

        byte[] derivedKey = new byte[64]; // 64 bytes = 512-bit key
        hkdf.generateBytes(derivedKey, 0, derivedKey.length);
        return derivedKey;
    }
}
