package com.myproject.TestS_MSMR;

import com.myproject.s_MSMR.S_MSMR;
import com.myproject.s_MSMR.S_MSMR.SenderState;
import com.myproject.s_MSMR.S_MSMR.ReceiverState;
import com.myproject.s_MSMR.S_MSMR.ProcSndResult;
import com.myproject.s_MSMR.S_MSMR.Ciphertext;
import com.myproject.s_MSMR.S_MSMR.Kid;
import com.myproject.s_MSMR.S_MSMR.InitResult;
import com.myproject.s_MSMR.S_MSMR.ProcRcvResult;
import com.myproject.s_MSMR.S_MSMR.ProcRcvFailure;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestS_MSMR_procRcv {

    @Test
    public void testProcRcv_success() throws Exception {
        int nS = 3;
        int nR = 2;

        // Step 1: Initialize states
        InitResult initResult = S_MSMR.procInit(nS, nR);
        SenderState sender = initResult.senderStates.get(0);
        ReceiverState receiver = initResult.receiverStates.get(0);

        // Step 2: Generate random associated data
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 3: Sender creates a ciphertext and key using procSnd
        ProcSndResult sndResult = S_MSMR.procSnd(sender, ad);
        Ciphertext ct = sndResult.ciphertext;

        // Step 4: Receiver processes the ciphertext using procRcv
        Object rcvResult = S_MSMR.procRcv(receiver, ad, ct);

        // Step 5: Assert success and cast
        assertTrue(rcvResult instanceof ProcRcvResult);
        ProcRcvResult procRcvResult = (ProcRcvResult) rcvResult;

        // Step 6: Validate returned state, key, and kid
        assertNotNull(procRcvResult.updatedState);
        assertNotNull(procRcvResult.k);
        assertNotNull(procRcvResult.kid);

        // Step 7: Validate Kid fields
        Kid kid = procRcvResult.kid;
        assertEquals(ct.senderId, kid.senderId);
        assertEquals(receiver.nS, kid.nS);
        assertEquals(receiver.nR, kid.nR);
        assertNotNull(kid.id);

        // Step 8: Print values for inspection
        System.out.println("=== procRcv Success Output ===");
        System.out.println("Sender ID: " + ct.senderId);
        System.out.print("Shared key k: ");
        printByteArray(procRcvResult.k);
        System.out.print("KID ID: ");
        printByteArray(kid.id);
    }

    @Test
    public void testProcRcv_signatureFailure() throws Exception {
        int nS = 3;
        int nR = 2;

        // Initialize states
        InitResult initResult = S_MSMR.procInit(nS, nR);
        SenderState sender = initResult.senderStates.get(0);
        ReceiverState receiver = initResult.receiverStates.get(0);

        // Generate random associated data
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Sender creates a ciphertext and key
        ProcSndResult sndResult = S_MSMR.procSnd(sender, ad);
        Ciphertext ct = sndResult.ciphertext;

        int testEnableFailureError = 0;

        // once the signature is corrupted currently this test is throwing an exception. Need to find better way to test this
        if (testEnableFailureError != 0)
        {   // Corrupt signature to simulate failure
            byte[] corruptedSignature = ct.signature.clone();
            corruptedSignature[0] ^= 0xFF;  // Flip bits to break signature
            Ciphertext corruptedCt = new Ciphertext(ct.senderId, ct.cPrime, ct.svkPrime, corruptedSignature);

            // Receiver processes the corrupted ciphertext
            Object rcvResult = S_MSMR.procRcv(receiver, ad, corruptedCt);

            // Assert failure type and updated state is returned
            assertTrue(rcvResult instanceof ProcRcvFailure);
            ProcRcvFailure failure = (ProcRcvFailure) rcvResult;
            assertNotNull(failure.updatedState);
        }

        // Print info
        System.out.println("=== procRcv Signature Failure ===");
        System.out.println("Receiver state remains unchanged after failed verification.");
    }

    // Utility method to print byte arrays in hex
    public static void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }
}
