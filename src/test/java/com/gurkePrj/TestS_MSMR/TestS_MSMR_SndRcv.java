package com.gurkePrj.TestS_MSMR;

import com.gurkePrj.s_MSMR.S_MSMR;
import com.gurkePrj.s_MSMR.S_MSMR.*;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestS_MSMR_SndRcv {

    @Test
    public void testProcSndAndRcv() throws Exception {
        int nS = 3;
        int nR = 5;

        // Step 1: Initialize
        InitResult initResult = S_MSMR.procInit(nS, nR);
        SenderState senderState = initResult.senderStates.get(0);
        ReceiverState receiverState = initResult.receiverStates.get(0);

        // Step 2: Generate Associated Data (AD)
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Measure state sizes
        System.out.println("=== S_MSMR Size Measurements ===");
        System.out.println("Configuration: nS=" + nS + ", nR=" + nR);
        System.out.println("SenderState size: " + calculateSenderStateSize(senderState) + " bytes");
        System.out.println("  - ek size: " + (senderState.ek != null ? senderState.ek.length : 0) + " bytes");
        System.out.println("ReceiverState size: " + calculateReceiverStateSize(receiverState) + " bytes");
        System.out.println("  - Contains info for " + (receiverState.senderInfoList != null ? receiverState.senderInfoList.size() : 0) + " senders");
        
        // Calculate total dk sizes in ReceiverState
        if (receiverState != null && receiverState.senderInfoList != null) {
            int totalDkSize = 0;
            for (ReceiverState.ReceiverInfo info : receiverState.senderInfoList) {
                totalDkSize += (info.dk != null ? info.dk.length : 0);
            }
            System.out.println("  - Total dk size (all senders): " + totalDkSize + " bytes");
        }

        // Repeat send/receive/validate steps multiple times
        for (int i = 0; i < 1000; i++) {
            // Step 3: Execute procSnd
            ProcSndResult sndResult = S_MSMR.procSnd(senderState, ad);
            assertNotNull(sndResult.ciphertext);
            assertNotNull(sndResult.key);
            assertNotNull(sndResult.kid);
            
            // Measure ciphertext size (only on first iteration)
            if (i == 0) {
                System.out.println("Ciphertext size: " + calculateCiphertextSize(sndResult.ciphertext) + " bytes");
                System.out.println("========================\n");
            }

            // Step 4: Execute procRcv with the resulting ciphertext
            Object rcvOutput = S_MSMR.procRcv(receiverState, ad, sndResult.ciphertext);
            ProcRcvResult rcvResult = (ProcRcvResult) rcvOutput;

            // Step 5: Validate output
            assertNotNull(rcvResult.k);
            assertArrayEquals(sndResult.key, rcvResult.k, "Shared keys should match");
            assertEquals(sndResult.kid.senderId, rcvResult.kid.senderId, "Sender ID must match");
            assertEquals(sndResult.kid.nS, rcvResult.kid.nS, "nS must match");
            assertEquals(sndResult.kid.nR, rcvResult.kid.nR, "nR must match");
        }
        // System.out.println("Repeated procSnd and procRcv succeeded.");

        S_MSMR.printAndResetBKEncStats();
        S_MSMR.printAndResetBKDecStats();
        S_MSMR.printAndResetBKFinStats();
    }

    // Manual size calculation for SenderState
    private int calculateSenderStateSize(SenderState state) {
        int size = 0;
        size += 4; // int id
        size += 4; // int nS
        size += 4; // int nR
        size += (state.ek != null ? state.ek.length : 0); // byte[] ek
        size += (state.ssk != null ? state.ssk.length : 0); // byte[] ssk
        return size;
    }

    // Manual size calculation for ReceiverState
    private int calculateReceiverStateSize(ReceiverState state) {
        int size = 0;
        size += 4; // int nS
        size += 4; // int nR
        // List of ReceiverInfo
        if (state.senderInfoList != null) {
            for (ReceiverState.ReceiverInfo info : state.senderInfoList) {
                size += (info.dk != null ? info.dk.length : 0); // byte[] dk
                size += (info.svk != null ? info.svk.length : 0); // byte[] svk
            }
        }
        return size;
    }

    // Manual size calculation for Ciphertext
    private int calculateCiphertextSize(Ciphertext ciphertext) {
        int size = 0;
        size += 4; // int senderId
        size += (ciphertext.cPrime != null ? ciphertext.cPrime.length : 0); // byte[] cPrime
        size += (ciphertext.svkPrime != null ? ciphertext.svkPrime.length : 0); // byte[] svkPrime
        size += (ciphertext.signature != null ? ciphertext.signature.length : 0); // byte[] signature
        return size;
    }

    // Utility to print byte arrays
    private void printByteArray(byte[] bytes) {


        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }
}
