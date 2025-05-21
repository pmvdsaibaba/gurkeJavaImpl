package com.myproject.signatureScheme;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.security.*;
import java.security.spec.ECGenParameterSpec;


public class SignatureScheme {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // --- S.gen : ∅ →$ VK × SK ---
    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
        keyGen.initialize(new ECGenParameterSpec("secp384r1"), new SecureRandom());
        java.security.KeyPair kp = keyGen.generateKeyPair();
        return new KeyPair(kp.getPublic(), kp.getPrivate());
    }

    public static class KeyPair {
        PublicKey vk;
        PrivateKey sk;

        public KeyPair(PublicKey vk, PrivateKey sk) {
            this.vk = vk;
            this.sk = sk;
        }

        public PublicKey getVk() {
            return vk;
        }

        public PrivateKey getSk() {
            return sk;
        }
    }

    // --- S.sgn : SK × M →$ S ---
    public static byte[] sgn(PrivateKey sk, byte[] message) throws Exception {
        Signature signature = Signature.getInstance("SHA512withECDSA", "BC");
        signature.initSign(sk, new SecureRandom());
        signature.update(message);
        return signature.sign(); // S
    }


    // --- S.vfy : VK × M × S → {0, 1} ---
    public static boolean vfy(PublicKey vk, byte[] message, byte[] sig) throws Exception {
        Signature signature = Signature.getInstance("SHA512withECDSA", "BC");
        signature.initVerify(vk);
        signature.update(message);
        return signature.verify(sig);
    }

}
