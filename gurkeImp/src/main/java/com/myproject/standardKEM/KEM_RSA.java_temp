package com.myproject.standardKEM;

import java.security.SecureRandom;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;

import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;

import java.util.Base64;

public class KEM_RSA {

    // This method simulates the generation of encapsulation and decapsulation keys
    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(256); // 256-bit RSA key size

        // Generate the key pair
        java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }
    
    // KeyPair class to hold the encapsulation key and decapsulation key
    public static class KeyPair {
        private byte[] publicKey; // Store the public key as a byte array
        private byte[] privateKey; // Store the private key as a byte array

        // Constructor to initialize KeyPairHolder with public and private keys as byte arrays
        public KeyPair(byte[] ek, byte[] dk) {
            this.publicKey = ek;
            this.privateKey = dk;
        }

            public byte[] getEk() {
                return publicKey;
            }
            
            public byte[] getDk() {
                return privateKey;
            }
    }

    // This method simulates K.gen(s) to generate an encapsulation key (ek) based on the seed (s)
    public static KeyPair gen(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");

        // Initialize with a specific byte array seed
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed); // Setting the seed as a byte array
        keyPairGenerator.initialize(256, secureRandom); // 256-bit RSA key size

        java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }
    
    // This method simulates the enc function to generate a key and ciphertext
    public static EncapsulationResult enc(byte[] ek) throws Exception {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Convert the byte array to an RSAPublicKey
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ek);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

        // Initialize RSA cipher for encryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Generate a random secret key (for example, 256 bytes)
        // here issue with the key size. 
        byte[] secretKey = new byte[128]; // A dummy secret key (this should be generated securely in a real-world application)
        new SecureRandom().nextBytes(secretKey);

        // Encrypt the secret key using the RSA public key
        byte[] encryptedKey = cipher.doFinal(secretKey);

        // Return the result as an EncapsulationResult object containing both the original and encrypted keys
        return new EncapsulationResult(secretKey, encryptedKey);
    }

    // EncapsulationResult class to hold key and ciphertext
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
    // This method simulates the dec  function to generate a key and ciphertext
    public static DecapsulationResult dec(byte[] dk, byte[] c) throws Exception {

       // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Convert the byte array to an RSAPrivateKey
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(dk);
        RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        // Initialize RSA cipher for decryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        // Decrypt the encapsulated key (the secret key)
        byte[] decryptedKey = cipher.doFinal(c);

        // Return the result as a DecapsulationResult containing the decrypted secret key
        return new DecapsulationResult(decryptedKey);
    }

    // DecapsulationResult class to hold key and ciphertext
    public static class DecapsulationResult {
        byte[] k;

        public DecapsulationResult(byte[] k) {
            this.k = k;
        }

        public byte[] getK() {
            return k;
        }
    }

}


