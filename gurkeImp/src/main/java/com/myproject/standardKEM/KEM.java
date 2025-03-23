package com.myproject.standardKEM;

import java.security.SecureRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.util.encoders.Base64;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import javax.crypto.Cipher;


import org.bouncycastle.crypto.engines.ElGamalEngine;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.ElGamalKeyParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;


import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class KEM {

    static {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    // This method generates an X25519 key pair (public and private keys) using BouncyCastle
    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException {
        // Create a SecureRandom instance
        SecureRandom random = new SecureRandom();

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Initialize with X25519 parameters (SecureRandom instance)
        X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(random);
        keyPairGenerator.init(params);

        // Generate the key pair
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        // Extract the public and private keys from the key pair
        X25519PublicKeyParameters publicKeyParameters = (X25519PublicKeyParameters) keyPair.getPublic();
        X25519PrivateKeyParameters privateKeyParameters = (X25519PrivateKeyParameters) keyPair.getPrivate();

        // Convert the keys to byte arrays (encoded format)
        byte[] publicKeyBytes = publicKeyParameters.getEncoded();
        byte[] privateKeyBytes = privateKeyParameters.getEncoded();

        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }

    // KeyPair class to hold the encapsulation key and decapsulation key
    public static class KeyPair {
        private byte[] publicKey; // Store the public key as a byte array
        private byte[] privateKey; // Store the private key as a byte array

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

    // This method generates an X25519 key pair based on the provided seed (for deterministic generation)
    public static KeyPair gen(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        // Initialize the SecureRandom instance with the provided seed
        SecureRandom random = new SecureRandom(seed);

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Initialize with X25519 parameters (using the seeded SecureRandom instance)
        X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(random);
        keyPairGenerator.init(params);

        // Generate the key pair
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Extract the public and private keys from the key pair
        X25519PublicKeyParameters publicKeyParameters = (X25519PublicKeyParameters) keyPair.getPublic();
        X25519PrivateKeyParameters privateKeyParameters = (X25519PrivateKeyParameters) keyPair.getPrivate();

        // Convert the keys to byte arrays (encoded format)
        byte[] publicKeyBytes = publicKeyParameters.getEncoded();
        byte[] privateKeyBytes = privateKeyParameters.getEncoded();

        // Return the key pair (with public and private keys as byte arrays)
        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }

    public static EncapsulationResult enc(byte[] ek) throws Exception {
        // Load the ElGamal public key
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal", "BC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ek);
        
        // Use BouncyCastle's ElGamalPublicKeyParameters for ElGamal keys
        ElGamalPublicKeyParameters publicKey = (ElGamalPublicKeyParameters) keyFactory.generatePublic(keySpec);

        // Generate a random secret key (for example, 128 bytes)
        byte[] secretKey = new byte[128];  // A dummy secret key (this should be generated securely in a real-world application)
        new SecureRandom().nextBytes(secretKey);

        // ElGamal Encryption: Encrypt the secret key using the public key
        ElGamalEngine engine = new ElGamalEngine();
        engine.init(true, publicKey);  // 'true' for encryption mode

        // Calculate the output block size based on the engine's configuration
        int outputBlockSize = engine.getOutputBlockSize();

        // Prepare the ciphertext array (encrypted key)
        byte[] encryptedKey = new byte[outputBlockSize];
        
        // Encrypt the secret key using processBlock
        byte[] result = engine.processBlock(secretKey, 0, secretKey.length);
        
        // The result of the encryption is the encrypted key
        System.arraycopy(result, 0, encryptedKey, 0, result.length);

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

    public static DecapsulationResult dec(byte[] dk, byte[] c) throws Exception {

        // Load the ElGamal private key
        KeyFactory keyFactory = KeyFactory.getInstance("ElGamal", "BC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(dk);
        ElGamalPrivateKeyParameters privateKey = (ElGamalPrivateKeyParameters) keyFactory.generatePrivate(keySpec);

        // Initialize the ElGamal engine for decryption
        ElGamalEngine engine = new ElGamalEngine();
        engine.init(false, privateKey);  // false indicates we're decrypting

        // Get the input/output block size
        int inputBlockSize = engine.getInputBlockSize();
        int outputBlockSize = engine.getOutputBlockSize();

        // Ensure ciphertext is properly sized
        if (c.length != inputBlockSize) {
            throw new IllegalArgumentException("Invalid ciphertext size.");
        }

        // Decrypt the encapsulated secret key using the ElGamal engine
        byte[] decryptedKey = engine.processBlock(c, 0, c.length);  // Use processBlock for ElGamal

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


