package com.gurkePrj.TestS_MSMR;

import com.gurkePrj.s_MSMR.S_MSMR;
import com.gurkePrj.s_MSMR.S_MSMR.SenderState;
import com.gurkePrj.s_MSMR.S_MSMR.ProcSndResult;
import com.gurkePrj.s_MSMR.S_MSMR.Ciphertext;
import com.gurkePrj.s_MSMR.S_MSMR.Kid;
import com.gurkePrj.s_MSMR.S_MSMR.InitResult;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TestS_MSMR_procSnd {

    @Test
    public void testProcSnd() throws Exception {
        int nS = 3;
        int nR = 2;

        // Step 1: Initialize state
        InitResult initResult = S_MSMR.procInit(nS, nR);
        SenderState sender = initResult.senderStates.get(0);

        // Step 2: Generate random associated data
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 3: Call procSnd
        ProcSndResult result = S_MSMR.procSnd(sender, ad);

        // Step 4: Validate result structure
        assertNotNull(result.updatedSenderState);
        assertNotNull(result.ciphertext);
        assertNotNull(result.key);
        assertNotNull(result.kid);

        // Step 5: Validate updated sender state
        assertEquals(sender.id, result.updatedSenderState.id);
        assertEquals(sender.nS, result.updatedSenderState.nS);
        assertEquals(sender.nR, result.updatedSenderState.nR);
        assertNotNull(result.updatedSenderState.ek);
        assertNotNull(result.updatedSenderState.ssk);

        // Step 6: Validate ciphertext contents
        Ciphertext ct = result.ciphertext;
        assertEquals(sender.id, ct.senderId);
        assertNotNull(ct.cPrime);
        assertNotNull(ct.svkPrime);
        assertNotNull(ct.signature);

        // Step 7: Validate key and kid
        assertNotNull(result.key);
        Kid kid = result.kid;
        assertEquals(sender.id, kid.senderId);
        assertEquals(sender.nS, kid.nS);
        assertEquals(sender.nR, kid.nR);
        assertNotNull(kid.id);

        // Step 8: Print all values for inspection
        System.out.println("=== procSnd Output ===");
        System.out.println("Sender ID: " + sender.id);
        System.out.print("Ciphertext c': ");
        printByteArray(ct.cPrime);
        System.out.print("Signature σ: ");
        printByteArray(ct.signature);
        System.out.print("SVK': ");
        printByteArray(ct.svkPrime);
        System.out.print("Shared key k: ");
        printByteArray(result.key);
        System.out.print("KID ID: ");
        printByteArray(kid.id);
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
