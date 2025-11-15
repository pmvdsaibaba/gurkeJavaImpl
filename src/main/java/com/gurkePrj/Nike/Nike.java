package com.gurkePrj.Nike;

import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;

import javax.crypto.Cipher;

import java.util.Base64;

import java.security.KeyPairGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.prng.FixedSecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import org.bouncycastle.jcajce.spec.XDHParameterSpec;


import org.bouncycastle.crypto.agreement.X25519Agreement;
// import org.bouncycastle.crypto.params.CipherParameters;

public class Nike {

    // Add Bouncy Castle as a Security Provider
    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    // This method simulates the generation of encapsulation and decapsulation keys
    public static KeyPair gen() throws NoSuchAlgorithmException, NoSuchProviderException {

        // Security.addProvider(new BouncyCastleProvider());
        

// Version 1
// this version is generating ek and dk of different size. This is because of some encoding. 
        // // X25519 KeyPair Generator
        // KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519", "BC");

        //  // No need to initialize with XDHParameterSpec, just call generateKeyPair()
        // keyPairGenerator.init(new XDHParameterSpec(XDHParameterSpec.X25519));
        // // keyPairGenerator.initialize(new XDHParameterSpec(XDHParameterSpec.X25519));

        // // Generate the key pair
		// java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();


        // byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        // byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        
// Version 2
// This is (Low-Level Bouncy Castle API) so that there is no encoding as previous version.

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

    // N.gen(s) : Generate the public and private keys with a given seed (SK -> PK)
    public static KeyPair gen(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException {
        // Add the BouncyCastle provider
        // Security.addProvider(new BouncyCastleProvider());

        // Initialize the SecureRandom instance with the provided seed
        // SecureRandom random = new SecureRandom(seed);

        FixedSecureRandom random = new FixedSecureRandom(seed); // Pass the seed directly

        // Initialize the X25519 key pair generator
        X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();

        // Example of testing without SecureRandom
        // byte[] fixedRandomBytes = new byte[32]; // Same 32-byte array every time
        // for (int i = 0; i < fixedRandomBytes.length; i++) {
        //     fixedRandomBytes[i] = (byte) 0xEF;  // Fill with 0xEF (EFEFEFE pattern)
        // }
        // X25519KeyGenerationParameters params = new X25519KeyGenerationParameters(new SecureRandom(fixedRandomBytes));


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

    // N.key : Compute the shared key using secret key and public key (SK, PK -> K)
    public static byte[] key(byte[] secretKey, byte[] publicKey) throws Exception {
        // Convert byte arrays into X25519 Private and Public Key parameters
        X25519PrivateKeyParameters privateKeyParameters = new X25519PrivateKeyParameters(secretKey, 0);
        X25519PublicKeyParameters publicKeyParameters = new X25519PublicKeyParameters(publicKey, 0);

        // Initialize the X25519Agreement with the private key
        X25519Agreement agreement = new X25519Agreement();
        agreement.init(privateKeyParameters);

        // Prepare a buffer to hold the shared key
        byte[] sharedKey = new byte[agreement.getAgreementSize()];

        // Calculate the shared agreement
        agreement.calculateAgreement(publicKeyParameters, sharedKey, 0);

        // Return the shared key
        return sharedKey;
    }

}


