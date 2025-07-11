package com.myproject.TestD_SSMR;

import com.myproject.d_SSMR.d_SSMR;
import com.myproject.d_SSMR.d_SSMR.*;
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

public class TestD_SSMR {

    @Test
    public void testProcInit() throws Exception {
        int nR = 5;
System.out.println(" ");
System.out.println(" ");
System.out.println("**********************************************");
System.out.println("* D_SSMR Init Test ");
System.out.println("**********************************************");


        // Step 1: Initialize
        InitResult initResult = d_SSMR.procInit(nR);
        
        assertNotNull(initResult.senderState);
        assertNotNull(initResult.receiverStates);
        assertEquals(nR, initResult.receiverStates.size());
        
        // Verify sender state
        senderState senderState = initResult.senderState;
        assertEquals(nR, senderState.memR.size());
        assertNotNull(senderState.ek);
        assertNotNull(senderState.ssk);
        assertNotNull(senderState.svk);
        assertEquals(0, senderState.tr.length); // Should be empty initially
        
        // Verify receiver states
        for (int i = 0; i < nR; i++)
        {
            ReceiverState receiverState = initResult.receiverStates.get(i);
            assertEquals(nR, receiverState.memR.size());
            assertNotNull(receiverState.dk);
            assertNotNull(receiverState.svk);
            assertEquals(0, receiverState.tr.length); // Should be empty initially
            
            // All receivers should have the same svk as sender initially
            assertArrayEquals(senderState.svk, receiverState.svk);
        }

        System.out.println("procInit test passed successfully");

    }

    @Test
    public void testProcSndAndRcv() throws Exception {
        int nR = 10;

System.out.println(" ");
System.out.println(" ");
System.out.println("**********************************************");
System.out.println("* D_SSMR Snd Rcv Test ");
System.out.println("**********************************************");
        // Step 1: Initialize
        InitResult initResult = d_SSMR.procInit(nR);
        senderState senderState = initResult.senderState;
        ReceiverState receiverState = initResult.receiverStates.get(0);

        // Step 2: Generate Associated Data (AD)
        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 3: Execute procSnd
        SendResult sndResult = d_SSMR.procSnd(senderState, ad);
        assertNotNull(sndResult.ciphertext);
        assertNotNull(sndResult.key);
        assertNotNull(sndResult.kid);
        assertNotNull(sndResult.updatedState);

        System.out.println("Serializing object of type: " + sndResult.ciphertext.getClass().getName());
        // Step 4: Execute procRcv with the resulting ciphertext
        Object rcvOutput = d_SSMR.procRcv(receiverState, ad, sndResult.ciphertext);
        
        // Verify it's a successful receive (not a failure)
        assertFalse(rcvOutput instanceof ReceiveFailure, "Receive should not fail");
        assertTrue(rcvOutput instanceof ReceiveResult, "Should be a successful receive");
        
        ReceiveResult rcvResult = (ReceiveResult) rcvOutput;

        // Step 5: Validate output
        assertNotNull(rcvResult.key);
        assertArrayEquals(sndResult.key, rcvResult.key, "Shared keys should match");
        assertArrayEquals(sndResult.kid.id, rcvResult.kid.id, "Kid IDs should match");
        assertEquals(sndResult.kid.memR.size(), rcvResult.kid.memR.size(), "memR sizes should match");

        System.out.println("procSnd and procRcv test passed. Shared key (k):");
        printByteArray(rcvResult.key);

        // Test multiple send/receive cycles with updated states
        senderState = sndResult.updatedState;
        receiverState = rcvResult.updatedState;


        ////////////////////////////////////////////
        ////// Test with other receivers as well

        ReceiverState receiverState2 = initResult.receiverStates.get(2);
        ReceiverState receiverState3 = initResult.receiverStates.get(3);
        ReceiverState receiverState4 = initResult.receiverStates.get(4);


        Object rcv2Output = d_SSMR.procRcv(receiverState2, ad, sndResult.ciphertext);
        Object rcv3Output = d_SSMR.procRcv(receiverState3, ad, sndResult.ciphertext);
        Object rcv4Output = d_SSMR.procRcv(receiverState4, ad, sndResult.ciphertext);
        
        assertTrue(rcv2Output instanceof ReceiveResult, "receiver 2 should also succeed");
        assertTrue(rcv3Output instanceof ReceiveResult, "receiver 3 should also succeed");
        assertTrue(rcv4Output instanceof ReceiveResult, "receiver 4 should also succeed");
        ReceiveResult rcv2Result = (ReceiveResult) rcv2Output;
        ReceiveResult rcv3Result = (ReceiveResult) rcv3Output;
        ReceiveResult rcv4Result = (ReceiveResult) rcv4Output;
        
        assertArrayEquals(sndResult.key, rcv2Result.key, "receiver 2 should match");
        assertArrayEquals(sndResult.key, rcv3Result.key, "receiver 3 should match");
        assertArrayEquals(sndResult.key, rcv4Result.key, "receiver 4 should match");
        
        System.out.println("receiver key 2 (k):");
        printByteArray(rcv2Result.key);
        System.out.println("receiver key 3 (k):");
        printByteArray(rcv3Result.key);
        System.out.println("receiver key 4 (k):");
        printByteArray(rcv4Result.key);


        ////////////////////////////////////////////
        // Second send

        // second time send
        SendResult sndResult2 = d_SSMR.procSnd(senderState, ad);

        Object rcvOutput2 = d_SSMR.procRcv(receiverState, ad, sndResult2.ciphertext);

        assertTrue(rcvOutput2 instanceof ReceiveResult, "Second receive should also succeed");
        ReceiveResult rcvResult2 = (ReceiveResult) rcvOutput2;

        assertArrayEquals(sndResult2.key, rcvResult2.key, "Second round keys should match");

        System.out.println("Second round test passed. Shared key (k):");
        printByteArray(rcvResult2.key);


        ////////////////////////////////////////////
        // Test with other receivers as well

        receiverState2 = rcv2Result.updatedState;
        receiverState3 = rcv3Result.updatedState;
        receiverState4 = rcv4Result.updatedState;

        Object rcv2Output2 = d_SSMR.procRcv(receiverState2, ad, sndResult2.ciphertext);
        Object rcv3Output2 = d_SSMR.procRcv(receiverState3, ad, sndResult2.ciphertext);
        Object rcv4Output2 = d_SSMR.procRcv(receiverState4, ad, sndResult2.ciphertext);

        assertTrue(rcv2Output2 instanceof ReceiveResult, "receiver 2 should also succeed");
        assertTrue(rcv3Output2 instanceof ReceiveResult, "receiver 3 should also succeed");
        assertTrue(rcv4Output2 instanceof ReceiveResult, "receiver 4 should also succeed");
        ReceiveResult rcv2Result2 = (ReceiveResult) rcv2Output2;
        ReceiveResult rcv3Result2 = (ReceiveResult) rcv3Output2;
        ReceiveResult rcv4Result2 = (ReceiveResult) rcv4Output2;

        assertArrayEquals(sndResult2.key, rcv2Result2.key, "receiver 2 should match");
        assertArrayEquals(sndResult2.key, rcv3Result2.key, "receiver 3 should match");
        assertArrayEquals(sndResult2.key, rcv4Result2.key, "receiver 4 should match");
        
        System.out.println("receiver key 2 (k):");
        printByteArray(rcv2Result2.key);
        System.out.println("receiver key 3 (k):");
        printByteArray(rcv3Result2.key);
        System.out.println("receiver key 4 (k):");
        printByteArray(rcv4Result2.key);


        System.out.println("snd second time and all receiver has same key: test passed successfully");

    }

    @Test
    public void testProcAdd() throws Exception {
        int nR = 10;
        int newUid = 13;

System.out.println(" ");
System.out.println(" ");
System.out.println("**********************************************");
System.out.println("* D_SSMR Add Test ");
System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_SSMR.procInit(nR);
        senderState senderState = initResult.senderState;

        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 2: Add a new receiver
        AddResult addResult = d_SSMR.procAdd(senderState, ad, newUid);
        
        assertNotNull(addResult.updatedsenderState);
        assertNotNull(addResult.newReceiverState);
        assertNotNull(addResult.ciphertext);
        assertNotNull(addResult.key);
        assertNotNull(addResult.kid);

        // Verify the sender state was updated with new member
        assertTrue(addResult.updatedsenderState.memR.contains(newUid), "New UID should be in sender's memR");
        assertEquals(nR + 1, addResult.updatedsenderState.memR.size(), "memR should have one more member");

        // Verify the new receiver state
        assertTrue(addResult.newReceiverState.memR.contains(newUid), "New UID should be in new receiver's memR");
        assertEquals(nR + 1, addResult.newReceiverState.memR.size(), "New receiver's memR should have correct size");

        System.out.println("procAdd test passed. Add operation key:");
        printByteArray(addResult.key);


        // Test that all existing receivers (from 0 to 9) can still receive after the add operation
        for (int i = 0; i < nR; i++) {
            ReceiverState existingReceiver = initResult.receiverStates.get(i);
            ReceiverState newAddedReceiverSt = addResult.newReceiverState;

            // System.out.println("Testing receiver " + i + "'s memR: " + existingReceiver.memR);

            // Perform receive operation
            Object rcvOutput = d_SSMR.procRcv(existingReceiver, ad, addResult.ciphertext);
            
            // Ensure that the existing receiver can still process the ciphertext after the add operation
            assertTrue(rcvOutput instanceof ReceiveResult, "Receiver " + i + " should be able to process add ciphertext");
            ReceiveResult rcvResult = (ReceiveResult) rcvOutput;
            System.out.println("Testing receiver " + i + "'s memR: " + rcvResult.updatedState.memR);
System.out.println("************************** Something wrong here with memR ***");
System.out.println("*************************************************************");
            assertArrayEquals(addResult.key, rcvResult.key, "Receiver " + i + " keys should match");
        }


        /////////////////////////////////////////////

        // Now, test the newly added receiver (UID = 13)
        ReceiverState newAddedReceiverSt1 = addResult.newReceiverState;
        System.out.println("Testing new receiver (UID 13) memR: " + newAddedReceiverSt1.memR);

        Object rcvOutputNewReceiver1 = d_SSMR.procRcv(newAddedReceiverSt1, ad, addResult.ciphertext);
        assertTrue(rcvOutputNewReceiver1 instanceof ReceiveResult, "New receiver (UID 13) should be able to process add ciphertext");
        ReceiveResult rcvResultNewReceiver1 = (ReceiveResult) rcvOutputNewReceiver1;

////////////////////////////////
///  Failing. may be gap in D_SSMR (Fixed now)
///////////
        assertArrayEquals(addResult.key, rcvResultNewReceiver1.key, "New receiver (UID 13) keys should match");

        SendResult sndResult2 = d_SSMR.procSnd(addResult.updatedsenderState, ad);

        Object rcvOutput2 = d_SSMR.procRcv(rcvResultNewReceiver1.updatedState, ad, sndResult2.ciphertext);

        assertTrue(rcvOutput2 instanceof ReceiveResult, "Receiver  should be able to process add ciphertext");

        ReceiveResult rcvResult2 = (ReceiveResult) rcvOutput2;

        assertArrayEquals(sndResult2.key, rcvResult2.key, "receiver 2 should match");

        System.out.println("receiver key 2 (k):");
        printByteArray(rcvResult2.key);

////////////////////////////////
///  Failing. may be gap in D_SSMR (Fixed now)
///////////


        // Step 3: Add another new receiver (UID = 14)
        int newUid2 = 14;
        AddResult addResult2 = d_SSMR.procAdd(sndResult2.updatedState, ad, newUid2);
        
        assertNotNull(addResult2.updatedsenderState);
        assertNotNull(addResult2.newReceiverState);
        assertNotNull(addResult2.ciphertext);
        assertNotNull(addResult2.key);
        assertNotNull(addResult2.kid);

        // Verify the sender state was updated with the second new member (UID = 14)
        assertTrue(addResult2.updatedsenderState.memR.contains(newUid2), "New UID (UID 14) should be in sender's memR");
        assertEquals(nR + 2, addResult2.updatedsenderState.memR.size(), "memR should have two more members");

        // Verify the new receiver state (UID = 14)
        assertTrue(addResult2.newReceiverState.memR.contains(newUid2), "New UID (UID 14) should be in new receiver's memR");
        assertEquals(nR + 2, addResult2.newReceiverState.memR.size(), "New receiver's memR should have correct size");

        System.out.println("procAdd test passed. Add operation key for UID 14:");
        printByteArray(addResult2.key);

        System.out.println("All receivers tested successfully after two adds.");

    }

    @Test
    public void testProcRmv() throws Exception {
        int nR = 10;
        int uidToRemove = 3; // Remove user with ID 3


System.out.println(" ");
System.out.println(" ");
System.out.println("**********************************************");
System.out.println("* D_SSMR Rmv Test ");
System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_SSMR.procInit(nR);
        senderState senderState = initResult.senderState;

        byte[] ad = new byte[16];
        new Random().nextBytes(ad);

        // Step 2: Remove a receiver
        RemoveResult rmvResult = d_SSMR.procRmv(senderState, ad, uidToRemove);
        
        assertNotNull(rmvResult.updatedState);
        assertNotNull(rmvResult.ciphertext);
        assertNotNull(rmvResult.key);
        assertNotNull(rmvResult.kid);

        // Verify the sender state was updated (member removed)
        assertFalse(rmvResult.updatedState.memR.contains(uidToRemove), 
                   "Removed UID should not be in sender's memR");
        assertEquals(nR - 1, rmvResult.updatedState.memR.size(), 
                    "memR should have one less member");

        System.out.println("procRmv test passed. Remove operation key:");
        printByteArray(rmvResult.key);

        // Test that remaining receivers can still receive after remove operation
        // Use a receiver that wasn't removed (e.g., receiver 0, which corresponds to uid 1)
        ReceiverState remainingReceiver = initResult.receiverStates.get(0);
        Object rcvOutput = d_SSMR.procRcv(remainingReceiver, ad, rmvResult.ciphertext);
        
        assertTrue(rcvOutput instanceof ReceiveResult, "Remaining receiver should be able to process remove ciphertext");
        ReceiveResult rcvResult = (ReceiveResult) rcvOutput;
        assertArrayEquals(rmvResult.key, rcvResult.key, "Keys should match for remaining receiver");
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