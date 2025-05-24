package com.myproject.TestS_MSMR;

import com.myproject.s_MSMR.S_MSMR;
import com.myproject.s_MSMR.S_MSMR.InitResult;
import com.myproject.s_MSMR.S_MSMR.SenderState;
import com.myproject.s_MSMR.S_MSMR.ReceiverState;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestS_MSMR_init {

    @Test
    public void testProcInit() throws Exception {
        int nS = 3;  // Number of senders
        int nR = 2;  // Number of receivers

        // Call procInit
        InitResult result = S_MSMR.procInit(nS, nR);

        // Validate sender states
        assertNotNull(result.senderStates);
        assertEquals(nS, result.senderStates.size());
        for (SenderState sender : result.senderStates) {
            assertNotNull(sender.ek);
            assertNotNull(sender.ssk);
            System.out.println("Sender " + sender.id + " EK: ");
            printByteArray(sender.ek);
        }

        // Validate receiver states
        assertNotNull(result.receiverStates);
        assertEquals(nR, result.receiverStates.size());
        for (int j = 0; j < nR; j++) {
            ReceiverState receiver = result.receiverStates.get(j);
            assertNotNull(receiver.senderInfoList);
            assertEquals(nS, receiver.senderInfoList.size());

            System.out.println("Receiver " + j + " Info:");
            for (int i = 0; i < receiver.senderInfoList.size(); i++) {
                byte[] dk = receiver.senderInfoList.get(i).dk;
                byte[] svk = receiver.senderInfoList.get(i).svk;

                assertNotNull(dk);
                assertNotNull(svk);

                System.out.println("  From Sender " + i + " - DK: ");
                printByteArray(dk);
                System.out.println("  From Sender " + i + " - SVK: ");
                printByteArray(svk);
            }
        }
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
