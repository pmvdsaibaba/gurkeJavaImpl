package com.myproject.ExampleKEM;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import javax.crypto.Cipher;
import java.util.Base64;

public class BouncyCastleKeyEncapsulation {

    public static void main(String[] args) {
        try {
            // Add Bouncy Castle as a Security Provider
            Security.addProvider(new BouncyCastleProvider());

            // Step 1: Key Pair Generation
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            System.out.println("Generated RSA Key Pair:");
            System.out.println("Public Key: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            System.out.println("Private Key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

            // Step 2: Key Encapsulation
            byte[] encapsulatedKey = keyEncapsulation(publicKey);
            System.out.println("\nEncapsulated Key (Encrypted Secret Key): " + Base64.getEncoder().encodeToString(encapsulatedKey));

            // Step 3: Key Decapsulation
            byte[] decapsulatedKey = keyDecapsulation(encapsulatedKey, privateKey);
            System.out.println("\nDecapsulated Key (Decrypted Secret Key): " + Base64.getEncoder().encodeToString(decapsulatedKey));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Key Encapsulation: Encrypt a random secret key with the RSA public key
    public static byte[] keyEncapsulation(RSAPublicKey publicKey) throws Exception {
        // Initialize RSA cipher for encryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        
        // Generate a random secret key (for example, 256 bytes)
        byte[] secretKey = new byte[256]; // A dummy secret key (this should be generated securely in a real-world application)
        new SecureRandom().nextBytes(secretKey);
        
        // Encrypt the secret key using the RSA public key
        byte[] encryptedKey = cipher.doFinal(secretKey);
        return encryptedKey;
    }

    // Key Decapsulation: Decrypt the encapsulated key using the RSA private key
    public static byte[] keyDecapsulation(byte[] encapsulatedKey, RSAPrivateKey privateKey) throws Exception {
        // Initialize RSA cipher for decryption
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        // Decrypt the encapsulated key (the secret key)
        byte[] decryptedKey = cipher.doFinal(encapsulatedKey);
        return decryptedKey;
    }
}
