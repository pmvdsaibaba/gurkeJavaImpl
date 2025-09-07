package com.myproject.signatureScheme;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class SignatureScheme {

    // static {
    //     Security.addProvider(new BouncyCastleProvider());
    // }

    // --- S.gen : ∅ →$ VK × SK ---
    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException
    {
        Security.addProvider(new BouncyCastleProvider());
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");

        SecureRandom random = new SecureRandom(); // no fixed seed!
        keyGen.initialize(new ECGenParameterSpec("secp384r1"), random);

        // keyGen.initialize(new ECGenParameterSpec("secp384r1"), new SecureRandom());
        java.security.KeyPair kp = keyGen.generateKeyPair();

        // Convert the keys to byte arrays (encoded format)
        byte[] publicKeyBytes = kp.getPublic().getEncoded();
        byte[] privateKeyBytes = kp.getPrivate().getEncoded();

        return new KeyPair(publicKeyBytes, privateKeyBytes);
    }

    public static class KeyPair {
        byte[] vk; // public key in bytes
        byte[] sk; // private key in bytes

        public KeyPair(byte[] vk, byte[] sk) {
            this.vk = vk;
            this.sk = sk;
        }

        public byte[] getVk() {
            return vk;
        }

        public byte[] getSk() {
            return sk;
        }
    }

    // --- S.sgn : SK × M →$ S ---
    public static byte[] sgn(byte[] sk, byte[] message) throws Exception {
        // Convert byte[] back to PrivateKey
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(sk);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Sign the message using the private key
        Signature signature = Signature.getInstance("SHA512withECDSA", "BC");
        // signature.initSign(privateKey, new SecureRandom());
        signature.initSign(privateKey);
        signature.update(message);
        return signature.sign(); // S
    }

    // --- S.vfy : VK × M × S → {0, 1} ---
    public static boolean vfy(byte[] vk, byte[] message, byte[] sig) throws Exception {
        // Convert byte[] back to PublicKey
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(vk);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // Verify the signature using the public key
        Signature signature = Signature.getInstance("SHA512withECDSA", "BC");
        signature.initVerify(publicKey);
        signature.update(message);
        return signature.verify(sig);
    }
}
