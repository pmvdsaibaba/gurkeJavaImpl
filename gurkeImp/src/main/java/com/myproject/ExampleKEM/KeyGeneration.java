package com.myproject.ExampleKEM;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

public class KeyGeneration {

    // KeyPairHolder class to hold public and private keys
    public static class KeyPairHolder {
        private RSAPublicKey publicKey;
        private RSAPrivateKey privateKey;

        // Constructor to initialize KeyPairHolder with public and private keys
        public KeyPairHolder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        // Getter for the public key
        public RSAPublicKey getPublicKey() {
            return publicKey;
        }

        // Getter for the private key
        public RSAPrivateKey getPrivateKey() {
            return privateKey;
        }

        // Method to display the keys as Base64 strings
        public void displayKeys() {
            System.out.println("Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            System.out.println("Private Key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        }

        // Method to return the public key as a Base64 string
        public String getPublicKeyBase64() {
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        }

        // Method to return the private key as a Base64 string
        public String getPrivateKeyBase64() {
            return Base64.getEncoder().encodeToString(privateKey.getEncoded());
        }
    }

    // Key generation without seed
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGenerator.initialize(2048); // 2048-bit RSA key size
        
        // Generate the key pair
        return keyPairGenerator.generateKeyPair();
    }

    // Key generation with byte array seed
    public static KeyPair generateKeyPairWithSeed(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        // Add Bouncy Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the KeyPairGenerator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");

        // Initialize with a specific byte array seed
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed); // Setting the seed as a byte array
        keyPairGenerator.initialize(2048, secureRandom); // 2048-bit RSA key size

        // Generate the key pair
        return keyPairGenerator.generateKeyPair();
    }

    public static void main(String[] args) {
        try {
            // Generate key pair without seed
            KeyPair keyPair = generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            // Create KeyPairHolder for the generated keys
            KeyPairHolder keyPairHolder = new KeyPairHolder(publicKey, privateKey);
            System.out.println("Generated RSA Key Pair (Without Seed):");
            keyPairHolder.displayKeys(); // Display the keys

            // Generate key pair with byte array seed
            byte[] seed = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };  // Example byte array seed
            keyPair = generateKeyPairWithSeed(seed);
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();

            // Create KeyPairHolder for the generated keys with seed
            KeyPairHolder keyPairHolderWithSeed = new KeyPairHolder(publicKey, privateKey);
            System.out.println("\nGenerated RSA Key Pair (With Seed):");
            keyPairHolderWithSeed.displayKeys(); // Display the keys

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
