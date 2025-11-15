package com.myproject.standardKEM;

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
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import java.util.Arrays;

public class KEM {

    static {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException {

        SecureRandom random = new SecureRandom();

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Initialize with X25519 parameters (SecureRandom instance)
        X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(random);
        keyPairGenerator.init(params);

        // Generate the key pair
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

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

    public static KeyPair gen(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        FixedSecureRandom random = new FixedSecureRandom(seed); // Pass the seed directly

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Initialize with X25519 parameters (using the seeded FixedSecureRandom instance)
        X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(random);
        keyPairGenerator.init(params);

        // Generate the key pair
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

        X25519PublicKeyParameters publicKeyParameters = (X25519PublicKeyParameters) keyPair.getPublic();
        X25519PrivateKeyParameters privateKeyParameters = (X25519PrivateKeyParameters) keyPair.getPrivate();

        // Convert the keys to byte arrays (encoded format)
        byte[] publicKeyBytes = publicKeyParameters.getEncoded();
        byte[] privateKeyBytes = privateKeyParameters.getEncoded();

        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }

    public static EncapsulationResult enc(byte[] ek) throws Exception {

        X25519PublicKeyParameters publicKey = new X25519PublicKeyParameters(ek, 0);

        byte[] seed = new byte[32]; // 256-bit seed
        Arrays.fill(seed, (byte) 0xAC); // Fill with 0xEF

        FixedSecureRandom random = new FixedSecureRandom(seed); // Pass the seed directly

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Initialize with X25519 parameters (using the seeded FixedSecureRandom instance)
        X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(random);
        keyPairGenerator.init(params);

        // Generate the key pair
        AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

        X25519PublicKeyParameters publicKeyParameters = (X25519PublicKeyParameters) keyPair.getPublic();
        X25519PrivateKeyParameters privateKeyParameters = (X25519PrivateKeyParameters) keyPair.getPrivate();

        byte[] ciphertext = publicKeyParameters.getEncoded();
        byte[] privateKeyBytes = privateKeyParameters.getEncoded();

        // Generate private key for encapsulation (X25519)
        X25519PrivateKeyParameters privateKey = new X25519PrivateKeyParameters(privateKeyBytes, 0);

        // Use X25519Agreement to perform the key agreement
        X25519Agreement agreement = new X25519Agreement();
        agreement.init(privateKey);

        byte[] secretKey = new byte[agreement.getAgreementSize()];
        agreement.calculateAgreement(publicKey, secretKey, 0);

        return new EncapsulationResult(secretKey, ciphertext);
    }

    // EncapsulationResult class to hold key and ciphertext
    public static class EncapsulationResult {
        public byte[] k;
        public byte[] c;

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
        X25519PrivateKeyParameters privateKey = new X25519PrivateKeyParameters(dk, 0);

        // Use X25519Agreement to perform the key agreement
        X25519Agreement agreement = new X25519Agreement();
        agreement.init(privateKey);

        byte[] decryptedKey = new byte[agreement.getAgreementSize()];
        agreement.calculateAgreement(new X25519PublicKeyParameters(c, 0), decryptedKey, 0);

        return new DecapsulationResult(decryptedKey);
    }

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
