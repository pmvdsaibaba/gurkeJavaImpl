package com.myproject.TestS_MSMR;

import com.myproject.s_MSMR.S_MSMR;
import com.myproject.s_MSMR.S_MSMR.*;
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

        // Step 3: Execute procSnd
        ProcSndResult sndResult = S_MSMR.procSnd(senderState, ad);
        assertNotNull(sndResult.ciphertext);
        assertNotNull(sndResult.key);
        assertNotNull(sndResult.kid);

        // Step 4: Execute procRcv with the resulting ciphertext
        Object rcvOutput = S_MSMR.procRcv(receiverState, ad, sndResult.ciphertext);
        ProcRcvResult rcvResult = (ProcRcvResult) rcvOutput;

        // Step 5: Validate output
        assertNotNull(rcvResult.k);
        assertArrayEquals(sndResult.key, rcvResult.k, "Shared keys should match");
        assertEquals(sndResult.kid.senderId, rcvResult.kid.senderId, "Sender ID must match");
        assertEquals(sndResult.kid.nS, rcvResult.kid.nS, "nS must match");
        assertEquals(sndResult.kid.nR, rcvResult.kid.nR, "nR must match");

        System.out.println("procSnd and procRcv succeeded. Shared key (k):");
        printByteArray(rcvResult.k);
        printByteArray(sndResult.key);
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
