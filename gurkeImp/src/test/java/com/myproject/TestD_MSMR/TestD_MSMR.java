package com.myproject.TestD_MSMR;

import com.myproject.d_MSMR.d_MSMR;
import com.myproject.d_MSMR.d_MSMR.*;
import org.junit.jupiter.api.Test;

import com.myproject.Tree.*;

import java.util.Random;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


import static org.junit.jupiter.api.Assertions.*;



public class TestD_MSMR {

    @Test
    public void testProcInit() throws Exception {
        int nR = 5;
        int nS = 5;

        System.out.println("\n**********************************************");
        System.out.println("* D_MSMR Init Test ");
        System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);

        assertNotNull(initResult.senderStates);
        assertEquals(nS, initResult.senderStates.size(), "There should be nS sender states");

        assertNotNull(initResult.receiverStatesMap);
        assertEquals(nR, initResult.receiverStatesMap.size(), "There should be nR receiver entries in the map");

        // Verify sender state (sample)
        SenderState senderState = initResult.senderStates.get(1); // sender IDs start from 1
        assertEquals(nR, senderState.memR.size());
        assertNotNull(senderState.ek);
        assertNotNull(senderState.ssk);
        assertNotNull(senderState.svk);
        assertEquals(0, senderState.tr.length, "Sender's tr should be initially empty");

        // Step 2: Check expected full memS and memR sets
        Set<Integer> expectedMemS = new HashSet<>();
        Set<Integer> expectedMemR = new HashSet<>();
        for (int i = 1; i <= nS; i++) expectedMemS.add(i);
        for (int i = 1; i <= nR; i++) expectedMemR.add(i);

        for (SenderState sender : initResult.senderStates.values()) {
            assertEquals(expectedMemS, sender.memS, "Sender memS should contain all senders");
            assertEquals(expectedMemR, sender.memR, "Sender memR should contain all receivers");
        }

        // Step 3: Verify receiver state structure and memS/memR
        for (int receiverId = 1; receiverId <= nR; receiverId++) {
            d_MSMR.ReceiverEntry receiverEntry = initResult.receiverStatesMap.get(receiverId);
            assertNotNull(receiverEntry, "ReceiverEntry for " + receiverId + " should exist");
            Map<Integer, SenderStateInReceiver> receiverStates = receiverEntry.stateMap;
            assertNotNull(receiverStates, "Receiver " + receiverId + " should have a state map");
            assertEquals(nS, receiverStates.size(), "Receiver " + receiverId + " must have one state per sender");

            for (SenderStateInReceiver rs : receiverStates.values()) {
                assertEquals(expectedMemS, rs.memS, "Receiver memS should contain all senders");
                assertEquals(expectedMemR, rs.memR, "Receiver memR should contain all receivers");

                // 🌳 Only one entry per map (since each receiverState represents 1 sender)
                assertEquals(1, rs.dkMap.size(), "Each ReceiverState should have 1 dkMap entry");
                assertEquals(1, rs.svkMap.size(), "Each ReceiverState should have 1 svkMap entry");
                assertEquals(1, rs.trMap.size(), "Each ReceiverState should have 1 trMap entry");
            }
        }

        // Step 4: Check sender IDs are sequential and correct
        for (int i = 1; i <= nS; i++) {
            SenderState s = initResult.senderStates.get(i);
            assertEquals(i, s.i, "SenderState.i should be sequential starting from 1");
        }

        // Step 5: Check that each receiver's receiverState for a sender has matching svk
        for (SenderState sender : initResult.senderStates.values()) {
            int senderId = sender.i;
            byte[] expectedSvk = sender.svk;

            for (int receiverId = 1; receiverId <= nR; receiverId++) {
                d_MSMR.ReceiverEntry receiverEntry = initResult.receiverStatesMap.get(receiverId);
                assertNotNull(receiverEntry, "ReceiverEntry for " + receiverId + " should exist");
                Map<Integer, SenderStateInReceiver> receiverStates = receiverEntry.stateMap;
                assertNotNull(receiverStates, "Receiver " + receiverId + " should exist in the map");

                SenderStateInReceiver state = receiverStates.get(senderId);  // Map keyed by senderId
                assertNotNull(state, "State for sender " + senderId + " should exist in receiver " + receiverId);
                assertArrayEquals(expectedSvk, state.svkMap.get(senderId),
                    "svk in receiver must match sender's svk for sender " + senderId + ", receiver " + receiverId);

                // svk in receiver state is exactly the same as in sender state
                assertArrayEquals(sender.svk, state.svkMap.get(senderId),
                    "svk in receiver state must be identical to sender's svk after init for sender " + senderId + ", receiver " + receiverId);
            }
        }


        System.out.println("procInit test passed successfully");
    }

    @Test
    public void testProcSndAndRcv() throws Exception
    {
        int nR = 5;
        int nS = 5;

        System.out.println(" ");
        System.out.println(" ");
        System.out.println("**********************************************");
        System.out.println("* D_MSMR Snd Rcv Test ");
        System.out.println("**********************************************");


        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);
        SenderState senderState = initResult.senderStates.get(1); // senderId = 1
        d_MSMR.ReceiverEntry receiverEntry = initResult.receiverStatesMap.get(1);
        Map<Integer, SenderStateInReceiver> receiverStateMap = receiverEntry.stateMap;
        SenderStateInReceiver receiverState = receiverStateMap.get(1); // senderId = 1

        // Step 2: Generate Associated Data (AD)
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 3: Execute procSnd
        SendResult sndResult = d_MSMR.procSnd(senderState, ad);
        assertNotNull(sndResult.ciphertext);
        assertNotNull(sndResult.key);
        assertNotNull(sndResult.kid);
        assertNotNull(sndResult.updatedState);

        System.out.println("Serializing object of type: " + sndResult.ciphertext.getClass().getName());
        // Step 4: Execute procRcv with the resulting ciphertext
        ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, sndResult.ciphertext);


        // Check that the key in rcvOutput matches the key in sndResult for receiver 1
        assertNotNull(rcvResult.key, "rcvResult.key should not be null");
        assertArrayEquals(sndResult.key, rcvResult.key, "Shared keys should match between sender and receiver 1");

        // Now check that all receivers derive the same key
        for (int receiverId = 2; receiverId <= nR; receiverId++) {
            d_MSMR.ReceiverEntry receiverEntryAll = initResult.receiverStatesMap.get(receiverId);
            Object rcvOutputAll = d_MSMR.procRcv(receiverEntryAll, ad, sndResult.ciphertext);
            assertTrue(rcvOutputAll instanceof ReceiveResult, "rcvOutput for receiver " + receiverId + " should be a ReceiveResult");
            ReceiveResult rcvResultAll = (ReceiveResult) rcvOutputAll;
            assertNotNull(rcvResultAll.key, "rcvResult.key for receiver " + receiverId + " should not be null");
            assertArrayEquals(sndResult.key, rcvResultAll.key, "Shared keys should match between sender and receiver " + receiverId);
        }
        System.out.println("snd first time and all receiver has same key: test passed successfully");

        // --- Additional test: sender 2 sends, all receivers derive same key ---
        SenderState senderState2 = initResult.senderStates.get(2); // senderId = 2
        SendResult sndResult2 = d_MSMR.procSnd(senderState2, ad);
        assertNotNull(sndResult2.ciphertext);
        assertNotNull(sndResult2.key);
        assertNotNull(sndResult2.kid);
        assertNotNull(sndResult2.updatedState);

        // For each receiver, check that derived key matches sender's key
        for (int receiverId = 1; receiverId <= nR; receiverId++) {
            d_MSMR.ReceiverEntry receiverEntryAll = initResult.receiverStatesMap.get(receiverId);
            Object rcvOutputAll = d_MSMR.procRcv(receiverEntryAll, ad, sndResult2.ciphertext);
            assertTrue(rcvOutputAll instanceof ReceiveResult, "rcvOutput for receiver " + receiverId + " (sender 2) should be a ReceiveResult");
            ReceiveResult rcvResultAll = (ReceiveResult) rcvOutputAll;
            assertNotNull(rcvResultAll.key, "rcvResult.key for receiver " + receiverId + " (sender 2) should not be null");
            assertArrayEquals(sndResult2.key, rcvResultAll.key, "Shared keys should match between sender 2 and receiver " + receiverId);
        }
        System.out.println("snd sender 2 and all receiver has same key: test passed successfully");

        // --- Additional test: sender 5 sends, all receivers derive same key ---
        SenderState senderState5 = initResult.senderStates.get(5); // senderId = 5
        SendResult sndResult5 = d_MSMR.procSnd(senderState5, ad);
        assertNotNull(sndResult5.ciphertext);
        assertNotNull(sndResult5.key);
        assertNotNull(sndResult5.kid);
        assertNotNull(sndResult5.updatedState);

        // For each receiver, check that derived key matches sender's key
        for (int receiverId = 1; receiverId <= nR; receiverId++) {
            d_MSMR.ReceiverEntry receiverEntryAll = initResult.receiverStatesMap.get(receiverId);
            Object rcvOutputAll = d_MSMR.procRcv(receiverEntryAll, ad, sndResult5.ciphertext);
            assertTrue(rcvOutputAll instanceof ReceiveResult, "rcvOutput for receiver " + receiverId + " (sender 5) should be a ReceiveResult");
            ReceiveResult rcvResultAll = (ReceiveResult) rcvOutputAll;
            assertNotNull(rcvResultAll.key, "rcvResult.key for receiver " + receiverId + " (sender 5) should not be null");
            assertArrayEquals(sndResult5.key, rcvResultAll.key, "Shared keys should match between sender 5 and receiver " + receiverId);
        }
        System.out.println("snd sender 5 and all receiver has same key: test passed successfully");



        // --- Test: sender 2 sends, receiver 2 receives twice, keys should match (fresh init) ---
        InitResult freshInit = d_MSMR.procInit(nS, nR);
        SenderState sender2 = freshInit.senderStates.get(2); // senderId = 2
        d_MSMR.ReceiverEntry receiverEntry2 = freshInit.receiverStatesMap.get(2);
        Map<Integer, SenderStateInReceiver> receiver2StateMap = receiverEntry2.stateMap;

        // First send/receive
        SendResult snd2First = d_MSMR.procSnd(sender2, ad);
        Object rcv2FirstOutput = d_MSMR.procRcv(receiverEntry2, ad, snd2First.ciphertext);
        assertTrue(rcv2FirstOutput instanceof ReceiveResult, "First rcvOutput for receiver 2 (sender 2) should be a ReceiveResult");
        ReceiveResult rcv2First = (ReceiveResult) rcv2FirstOutput;
        assertNotNull(rcv2First.key, "First rcvResult.key for receiver 2 (sender 2) should not be null");

        // Update sender and receiver state for second round
        SenderState sender2Updated = snd2First.updatedState;
        ReceiverEntry receiver2State = rcv2First.updatedState;


        // Second send/receive
        SendResult snd2Second = d_MSMR.procSnd(sender2Updated, ad);
        Object rcv2SecondOutput = d_MSMR.procRcv(receiver2State, ad, snd2Second.ciphertext);
        assertTrue(rcv2SecondOutput instanceof ReceiveResult, "Second rcvOutput for receiver 2 (sender 2) should be a ReceiveResult");
        ReceiveResult rcv2Second = (ReceiveResult) rcv2SecondOutput;
        assertNotNull(rcv2Second.key, "Second rcvResult.key for receiver 2 (sender 2) should not be null");
        assertArrayEquals(snd2Second.key, rcv2Second.key, "Second round: Shared keys should match between sender 2 and receiver 2");
        System.out.println("snd/rcv sender 2/receiver 2 second time: test passed successfully");

        // Third send/receive
        SenderState sender2Third = snd2Second.updatedState;
        d_MSMR.ReceiverEntry receiver2StateThird = rcv2Second.updatedState;
        SendResult snd2Third = d_MSMR.procSnd(sender2Third, ad);
        Object rcv2ThirdOutput = d_MSMR.procRcv(receiver2StateThird, ad, snd2Third.ciphertext);
        assertTrue(rcv2ThirdOutput instanceof ReceiveResult, "Third rcvOutput for receiver 2 (sender 2) should be a ReceiveResult");
        ReceiveResult rcv2Third = (ReceiveResult) rcv2ThirdOutput;
        assertNotNull(rcv2Third.key, "Third rcvResult.key for receiver 2 (sender 2) should not be null");
        assertArrayEquals(snd2Third.key, rcv2Third.key, "Third round: Shared keys should match between sender 2 and receiver 2");
        System.out.println("snd/rcv sender 2/receiver 2 third time: test passed successfully");

        // Fourth send/receive
        SenderState sender2Fourth = snd2Third.updatedState;
        d_MSMR.ReceiverEntry receiver2StateFourth = rcv2Third.updatedState;
        SendResult snd2Fourth = d_MSMR.procSnd(sender2Fourth, ad);
        Object rcv2FourthOutput = d_MSMR.procRcv(receiver2StateFourth, ad, snd2Fourth.ciphertext);
        assertTrue(rcv2FourthOutput instanceof ReceiveResult, "Fourth rcvOutput for receiver 2 (sender 2) should be a ReceiveResult");
        ReceiveResult rcv2Fourth = (ReceiveResult) rcv2FourthOutput;
        assertNotNull(rcv2Fourth.key, "Fourth rcvResult.key for receiver 2 (sender 2) should not be null");
        assertArrayEquals(snd2Fourth.key, rcv2Fourth.key, "Fourth round: Shared keys should match between sender 2 and receiver 2");
        System.out.println("snd/rcv sender 2/receiver 2 fourth time: test passed successfully");



        System.out.println("**********************************************");

    }



    @Test
    public void testProcAddR_test1() throws Exception {
        int nS = 3; // number of senders
        int nR = 5; // initial number of receivers
        int newUid = 13;

        System.out.println("\n**********************************************");
        System.out.println("* D_MSMR Add Receiver Test 1");
        System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);
        // Pick a sender to perform the add (e.g., sender 1)
        SenderState senderState = initResult.senderStates.get(1);

        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 2: Add a new receiver
        AddRResult addResult = d_MSMR.procAddR(senderState, ad, newUid);

        assertNotNull(addResult.updatedSenderState);
        assertNotNull(addResult.newReceiverEntry);
        assertNotNull(addResult.cS);
        assertNotNull(addResult.cR);
        assertNotNull(addResult.key);
        assertNotNull(addResult.kid);

        // Verify the sender state was updated with new member
        assertTrue(addResult.updatedSenderState.memR.contains(newUid), "New UID should be in sender's memR");
        assertEquals(nR + 1, addResult.updatedSenderState.memR.size(), "memR should have one more member");

        // Verify the new receiver state for the sender
        SenderStateInReceiver newReceiverState = addResult.newReceiverEntry.stateMap.get(1);
        assertNotNull(newReceiverState, "New receiver state for sender 1 should not be null");
        assertTrue(newReceiverState.memR.contains(newUid), "New UID should be in new receiver's memR");
        assertEquals(nR + 1, newReceiverState.memR.size(), "New receiver's memR should have correct size");

        System.out.println("procAddR test passed. Add operation key:");
        printByteArray(addResult.key);

        // Use a working copy of receiverStatesMap for all state updates
        Map<Integer, Map<Integer, SenderStateInReceiver>> currentReceiverStatesMap = new HashMap<>();
        for (Map.Entry<Integer, d_MSMR.ReceiverEntry> entry : initResult.receiverStatesMap.entrySet()) {
            currentReceiverStatesMap.put(entry.getKey(), new HashMap<>(entry.getValue().stateMap));
        }

        /** This commented test code block is working. Added to check if the newly added receiver is able to generate the key */
        // Immediately test that the newly added receiver can process the add ciphertext and derive the same key
        d_MSMR.ReceiverEntry newReceiverEntry = addResult.newReceiverEntry;
        ReceiveResult newRcvResult = d_MSMR.procRcv(newReceiverEntry, ad, addResult.cR);
        assertNotNull(newRcvResult, "New Receiver " + newUid + " should be able to process add ciphertext");
        assertTrue(newRcvResult.success, "New Receiver " + newUid + " should process add ciphertext");
        assertArrayEquals(addResult.key, newRcvResult.key, "New Receiver " + newUid + " key should match sender after add");
        // Update receiver state after receive
        currentReceiverStatesMap.put(newUid, newRcvResult.updatedState.stateMap);

        // Update all other senders' state after procAddR
        Map<Integer, SenderState> currentSenderStates = new HashMap<>(initResult.senderStates);
        currentSenderStates.put(1, addResult.updatedSenderState);
        for (int sid = 2; sid <= nS; sid++) {
            SenderState otherSenderState = currentSenderStates.get(sid);
            SenderState updatedOtherSenderState = d_MSMR.proc(otherSenderState, ad, addResult.cS);
            currentSenderStates.put(sid, updatedOtherSenderState);
        }

        // Test that all existing receivers (from 1 to nR) can still receive after the add operation
        for (int r = 1; r <= nR; r++) {
            // Always use the latest receiver state
            d_MSMR.ReceiverEntry receiverEntry = new d_MSMR.ReceiverEntry(false, currentReceiverStatesMap.get(r));
            ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, addResult.cR);
            assertNotNull(rcvResult, "Receiver " + r + " should be able to process add ciphertext");
            assertTrue(rcvResult.success, "Receiver " + r + " should process add ciphertext successfully");
            assertArrayEquals(addResult.key, rcvResult.key, "Receiver " + r + " keys should match");
            // Update receiver state after receive
            currentReceiverStatesMap.put(r, rcvResult.updatedState.stateMap);
        }

        // --- Add a few more receivers and test cross-sender/receiver key agreement ---
        int[] newUids = {21, 22};

        SenderState currentSenderState = addResult.updatedSenderState;
        Map<Integer, SenderStateInReceiver> latestNewReceiverState = addResult.newReceiverEntry.stateMap;

    for (int i = 0; i < newUids.length; i++) {
            int uid = newUids[i];
            AddRResult addRes = d_MSMR.procAddR(currentSenderState, ad, uid);
            assertNotNull(addRes.updatedSenderState);
            assertNotNull(addRes.newReceiverEntry);
            assertNotNull(addRes.cR);
            assertNotNull(addRes.key);
            // Update sender state
            currentSenderState = addRes.updatedSenderState;
            // Update all other senders' state after procAddR
            currentSenderStates.put(1, addRes.updatedSenderState);
            for (int sid = 2; sid <= nS; sid++) {
                SenderState otherSenderState = currentSenderStates.get(sid);
                SenderState updatedOtherSenderState = d_MSMR.proc(otherSenderState, ad, addRes.cS);
                currentSenderStates.put(sid, updatedOtherSenderState);
            }
            // Insert new receiver state for the new uid
            currentReceiverStatesMap.put(uid, addRes.newReceiverEntry.stateMap);
            latestNewReceiverState = addRes.newReceiverEntry.stateMap;
            // After each add, update all existing receivers with the add ciphertext
            for (int r = 1; r <= nR; r++) {
                d_MSMR.ReceiverEntry receiverEntry = new d_MSMR.ReceiverEntry(false, currentReceiverStatesMap.get(r));
                ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, addRes.cR);
                assertNotNull(rcvResult, "Receiver " + r + " should be able to process add ciphertext for new UID " + uid);
                assertTrue(rcvResult.success, "Receiver " + r + " should process add ciphertext for new UID " + uid);
                assertArrayEquals(addRes.key, rcvResult.key, "Receiver " + r + " keys should match for new UID " + uid);
                currentReceiverStatesMap.put(r, rcvResult.updatedState.stateMap);
            }
        }

        System.out.println("************** End of test 1 *******************");
    }

    @Test
    public void testProcAddR_test2() throws Exception {
        int nS = 3; // number of senders
        int nR = 14; // initial number of receivers
        int newUid = 15;

        System.out.println("\n**********************************************");
        System.out.println("* D_MSMR Add Receiver Test 2");
        System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);
        // Pick a sender to perform the add (e.g., sender 1)
        SenderState senderState = initResult.senderStates.get(1);

        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 2: Add a new receiver
        AddRResult addResult = d_MSMR.procAddR(senderState, ad, newUid);

        SenderStateInReceiver newReceiverState = addResult.newReceiverEntry.stateMap.get(1);

        // Use a working copy of receiverStatesMap for all state updates
        Map<Integer, d_MSMR.ReceiverEntry> currentReceiverStatesMap = new HashMap<>();
        for (Map.Entry<Integer, d_MSMR.ReceiverEntry> entry : initResult.receiverStatesMap.entrySet()) {
            // Deep copy if needed, but here we just copy the ReceiverEntry reference
            currentReceiverStatesMap.put(entry.getKey(), new d_MSMR.ReceiverEntry(entry.getValue().isNewAddRcvr, new HashMap<>(entry.getValue().stateMap)));
        }

        currentReceiverStatesMap.put(newUid, new d_MSMR.ReceiverEntry(addResult.newReceiverEntry.isNewAddRcvr, new HashMap<>(addResult.newReceiverEntry.stateMap)));

        // Update all other senders' state after procAddR
        Map<Integer, SenderState> currentSenderStates = new HashMap<>(initResult.senderStates);
        currentSenderStates.put(1, addResult.updatedSenderState);
        for (int sid = 2; sid <= nS; sid++) {
            SenderState otherSenderState = currentSenderStates.get(sid);
            SenderState updatedOtherSenderState = d_MSMR.proc(otherSenderState, ad, addResult.cS);
            currentSenderStates.put(sid, updatedOtherSenderState);
        }

        nR++; // Increment nR to account for the newly added receiver

        // Test that all existing receivers (from 1 to nR) can still receive after the add operation
        for (int r = 1; r <= nR; r++) {
            // Always use the latest receiver entry
            d_MSMR.ReceiverEntry receiverEntry = currentReceiverStatesMap.get(r);
            ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, addResult.cR);
            assertNotNull(rcvResult, "Receiver " + r + " should be able to process add ciphertext");
            assertTrue(rcvResult.success, "Receiver " + r + " should process add ciphertext successfully");
            assertArrayEquals(addResult.key, rcvResult.key, "Receiver " + r + " keys should match");
            // Update receiver entry after receive
            currentReceiverStatesMap.put(r, rcvResult.updatedState);
        }

        // --- Add a few more receivers and test cross-sender/receiver key agreement ---
        int[] newUids = {21, 22,23,24,25,26,27,28,29,30,45,66};

        SenderState currentSenderState = addResult.updatedSenderState;
        Map<Integer, SenderStateInReceiver> latestNewReceiverState = addResult.newReceiverEntry.stateMap;

        System.out.println("[Progress] Adding new receivers, newUids: ");
        for (int i = 0; i < newUids.length; i++) {
            System.out.print(java.util.Arrays.toString(new int[]{newUids[i]}));

            int uid = newUids[i];
            AddRResult addRes = d_MSMR.procAddR(currentSenderState, ad, uid);
            assertNotNull(addRes.updatedSenderState);
            assertNotNull(addRes.newReceiverEntry);
            assertNotNull(addRes.cR);
            assertNotNull(addRes.key);
            // Update sender state
            currentSenderState = addRes.updatedSenderState;
            // Update all other senders' state after procAddR
            currentSenderStates.put(1, addRes.updatedSenderState);
            for (int sid = 2; sid <= nS; sid++) {
                SenderState otherSenderState = currentSenderStates.get(sid);
                SenderState updatedOtherSenderState = d_MSMR.proc(otherSenderState, ad, addRes.cS);
                currentSenderStates.put(sid, updatedOtherSenderState);
            }
            // Insert new receiver state for the new uid
            currentReceiverStatesMap.put(uid, addRes.newReceiverEntry);
            latestNewReceiverState = addRes.newReceiverEntry.stateMap;
            // After each add, update all existing receivers with the add ciphertext
            for (int r = 1; r <= nR; r++) {
                d_MSMR.ReceiverEntry receiverEntry = currentReceiverStatesMap.get(r);
                ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, addRes.cR);
                assertNotNull(rcvResult, "Receiver " + r + " should be able to process add ciphertext for new UID " + uid);
                assertTrue(rcvResult.success, "Receiver " + r + " should process add ciphertext for new UID " + uid);
                assertArrayEquals(addRes.key, rcvResult.key, "Receiver " + r + " keys should match for new UID " + uid);
                currentReceiverStatesMap.put(r, rcvResult.updatedState);
            }
            // Check all new receivers so far
            for (int j = 0; j <= i; j++) {
                int checkUid = newUids[j];
                d_MSMR.ReceiverEntry checkReceiverEntry = currentReceiverStatesMap.get(checkUid);
                ReceiveResult checkRcvResult = d_MSMR.procRcv(checkReceiverEntry, ad, addRes.cR);
                assertNotNull(checkRcvResult, "New Receiver " + checkUid + " should be able to process new ciphertext after add");
                assertTrue(checkRcvResult.success, "New Receiver " + checkUid + " should process new ciphertext after add");
                assertArrayEquals(addRes.key, checkRcvResult.key, "New Receiver " + checkUid + " key should match sender after add");
                currentReceiverStatesMap.put(checkUid, checkRcvResult.updatedState);
            }
        }

        // After all adds, do a new send/receive and verify key agreement
        // Sender 1 sends
        SendResult sendRes = d_MSMR.procSnd(currentSenderState, ad);
        assertNotNull(sendRes.ciphertext);
        assertNotNull(sendRes.key);
        // All receivers (original and new) receive
        for (int r = 1; r <= nR; r++) {
            d_MSMR.ReceiverEntry receiverEntry = currentReceiverStatesMap.get(r);
            ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, sendRes.ciphertext);
            assertNotNull(rcvResult, "Receiver " + r + " should be able to process new ciphertext after adds");
            assertTrue(rcvResult.success, "Receiver " + r + " should process new ciphertext after adds");
            assertArrayEquals(sendRes.key, rcvResult.key, "Receiver " + r + " keys should match sender 1 after adds");
            currentReceiverStatesMap.put(r, rcvResult.updatedState);
        }
        for (int i = 0; i < newUids.length; i++) {
            int uid = newUids[i];
            d_MSMR.ReceiverEntry receiverEntry = currentReceiverStatesMap.get(uid);
            ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, sendRes.ciphertext);
            assertNotNull(rcvResult, "New Receiver " + uid + " should be able to process new ciphertext after adds");
            assertTrue(rcvResult.success, "New Receiver " + uid + " should process new ciphertext after adds");
            assertArrayEquals(sendRes.key, rcvResult.key, "New Receiver " + uid + " keys should match sender 1 after adds");
            currentReceiverStatesMap.put(uid, rcvResult.updatedState);
        }
        System.out.println("\n************** End of test 2 *******************");
    }




    @Test
    public void testProcRmvReceiver() throws Exception {
        int nS = 3; // number of senders
        int nR = 10; // initial number of receivers
        int uidToRemove = 5; // Remove receiver with ID 5

        System.out.println("\n**********************************************");
        System.out.println("* D_MSMR Remove Receiver Test");
        System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);
        SenderState senderState = initResult.senderStates.get(1);
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 2: Remove a receiver
        d_MSMR.RmvResult rmvResult = d_MSMR.procRmv(senderState, ad, d_MSMR.TargetType.RECEIVER, uidToRemove);
        assertNotNull(rmvResult.updatedSenderState, "Sender state should be updated after removal");
        assertNotNull(rmvResult.cR, "Receiver ciphertext (cR) should not be null after removal");
        assertNotNull(rmvResult.key, "Key should not be null after removal");
        assertNotNull(rmvResult.kid, "Kid should not be null after removal");

        // Verify the sender state was updated (member removed)
        assertFalse(rmvResult.updatedSenderState.memR.contains(uidToRemove), "Removed UID should not be in sender's memR");
        assertEquals(nR - 1, rmvResult.updatedSenderState.memR.size(), "memR should have one less member");

        // Use a working copy of receiverStatesMap for all state updates
        Map<Integer, d_MSMR.ReceiverEntry> currentReceiverStatesMap = new HashMap<>();
        for (Map.Entry<Integer, d_MSMR.ReceiverEntry> entry : initResult.receiverStatesMap.entrySet()) {
            currentReceiverStatesMap.put(entry.getKey(), new d_MSMR.ReceiverEntry(entry.getValue().isNewAddRcvr, new HashMap<>(entry.getValue().stateMap)));
        }

        // Remove the receiver entry for uidToRemove
        currentReceiverStatesMap.remove(uidToRemove);

        // Update all other senders' state after procRmv
        Map<Integer, SenderState> currentSenderStates = new HashMap<>(initResult.senderStates);
        currentSenderStates.put(1, rmvResult.updatedSenderState);
        for (int sid = 2; sid <= nS; sid++) {
            SenderState otherSenderState = currentSenderStates.get(sid);
            SenderState updatedOtherSenderState = d_MSMR.proc(otherSenderState, ad, rmvResult.cS);
            currentSenderStates.put(sid, updatedOtherSenderState);
        }

        // Test that all remaining receivers (from 1 to nR, except removed) can still receive after the remove operation
        for (int r = 1; r <= nR; r++) {
            // if ((r == uidToRemove) || (r == (uidToRemove+1))) continue;
            if ((r == uidToRemove)) continue;
            d_MSMR.ReceiverEntry receiverEntry = currentReceiverStatesMap.get(r);
            assertNotNull(receiverEntry, "ReceiverEntry for " + r + " should exist");
            d_MSMR.ReceiveResult rcvResult = d_MSMR.procRcv(receiverEntry, ad, rmvResult.cR);
            assertNotNull(rcvResult, "Receiver " + r + " should be able to process remove ciphertext");
            assertTrue(rcvResult.success, "Receiver " + r + " should process remove ciphertext successfully");
            assertArrayEquals(rmvResult.key, rcvResult.key, "Receiver " + r + " keys should match after remove");
            currentReceiverStatesMap.put(r, rcvResult.updatedState);
        }

        // Try to process remove ciphertext for the removed receiver (should fail or return null)
        d_MSMR.ReceiverEntry removedReceiverEntry = initResult.receiverStatesMap.get(uidToRemove);
        if (removedReceiverEntry != null) {
            Object removedRcvResult = d_MSMR.procRcv(removedReceiverEntry, ad, rmvResult.cR);
            if (removedRcvResult instanceof d_MSMR.ReceiveResult) {
                d_MSMR.ReceiveResult failResult = (d_MSMR.ReceiveResult) removedRcvResult;
                assertFalse(failResult.success, "Removed receiver should not process remove ciphertext successfully");
            } else {
                assertNull(removedRcvResult, "Removed receiver should not be able to process remove ciphertext");
            }
        }

        System.out.println("testProcRmvReceiver passed. Key after remove:");
        printByteArray(rmvResult.key);
        System.out.println("************** End of remove receiver test *******************");
    }



    // Utility to print byte arrays
    private void printByteArray(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }


    private void printTreeState(Tree tree) throws Exception {
        System.out.println("\n******************************************************************");
        System.out.println("Tree State:");
        System.out.println("******************************************************************");

        // for (Tree.Node node : tree.getNodesInternal()) {
        //     System.out.println("Node " + node.getNodeIndex() + ":");
        //     System.out.println("  Level: " + node.getNodeLevel());
        //     System.out.println("  Root: " + node.getRootnode());
        //     System.out.println("  Left: " + node.getChildLeftnode());
        //     System.out.println("  Right: " + node.getChildRightnode());
        //     System.out.println("  Is Leaf: " + node.isLeaf());
        //     System.out.println("  Leaf Index: " + node.getLeafIndex());
        //     System.out.println("  PK: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
        //     System.out.println("  SK: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
        //     System.out.println();
        // }

        System.out.println("Tree Size: " + tree.getSize());
        System.out.println("Leaf Count: " + tree.getNumOfLeaf());
        System.out.println("Max Node Index: " + tree.getNodeIndexMax());
        System.out.println("Max Leaf Index: " + tree.getLeafIndexMax());
        System.out.print("Node Indexes: ");
        printIntList(tree.getNodeIndexes());
        System.out.print("Leaf Indexes: ");
        printIntList(tree.getLeafIndexes());

        printTreeDiagram(tree);
    }

    private void printTreeDiagram(Tree tree) {
        Map<Integer, Tree.Node> nodeMap = new HashMap<>();
        for (Tree.Node node : tree.getNodesInternal()) {
            nodeMap.put(node.getNodeIndex(), node);
        }

        Tree.Node root = null;
        for (Tree.Node node : tree.getNodesInternal()) {
            if (node.getRootnode() == -1) {
                root = node;
                break;
            }
        }

        if (root == null) {
            System.out.println("No root node found.");
            return;
        }

        printTreeRecursive(root, nodeMap, "", true);
    }

    private void printTreeRecursive(Tree.Node node, Map<Integer, Tree.Node> nodeMap, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + "[" + node.getNodeIndex() + "]");

        Integer left = node.getChildLeftnode();
        Integer right = node.getChildRightnode();

        List<Tree.Node> children = new ArrayList<>();
        if (left != null && left != -1) children.add(nodeMap.get(left));
        if (right != null && right != -1) children.add(nodeMap.get(right));

        for (int i = 0; i < children.size(); i++) {
            printTreeRecursive(children.get(i), nodeMap, prefix + (isTail ? "    " : "│   "), i == children.size() - 1);
        }
    }

    public static void printIntList(List<Integer> intList)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer i : intList) {
            sb.append(String.format("%d ", i));  // Convert to decimal representation
        }
        System.out.println(sb.toString().trim());  // Remove the trailing space
    }
}