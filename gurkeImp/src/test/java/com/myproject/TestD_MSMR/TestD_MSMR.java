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

        System.out.println("\n\n**********************************************");
        System.out.println("* D_MSMR Init Test ");
        System.out.println("**********************************************");

        // Step 1: Initialize
        InitResult initResult = d_MSMR.procInit(nS, nR);

        assertNotNull(initResult.senderStates);
        assertEquals(nS, initResult.senderStates.size(), "There should be nS sender states");

        assertNotNull(initResult.receiverStatesMap);
        assertEquals(nR, initResult.receiverStatesMap.size(), "There should be nR receiver entries in the map");

        // Verify sender state (sample)
        SenderState senderState = initResult.senderStates.get(0);
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

        for (SenderState sender : initResult.senderStates) {
            assertEquals(expectedMemS, sender.memS, "Sender memS should contain all senders");
            assertEquals(expectedMemR, sender.memR, "Sender memR should contain all receivers");
        }

        // Step 3: Verify receiver state structure and memS/memR
        for (int receiverId = 1; receiverId <= nR; receiverId++) {
            List<ReceiverState> receiverStates = initResult.receiverStatesMap.get(receiverId);
            assertNotNull(receiverStates, "Receiver " + receiverId + " should have a list of states");
            assertEquals(nS, receiverStates.size(), "Receiver " + receiverId + " must have one state per sender");

            for (ReceiverState rs : receiverStates) {
                assertEquals(expectedMemS, rs.memS, "Receiver memS should contain all senders");
                assertEquals(expectedMemR, rs.memR, "Receiver memR should contain all receivers");

                // 🌳 Only one entry per map (since each receiverState represents 1 sender)
                assertEquals(1, rs.dkMap.size(), "Each ReceiverState should have 1 dkMap entry");
                assertEquals(1, rs.svkMap.size(), "Each ReceiverState should have 1 svkMap entry");
                assertEquals(1, rs.trMap.size(), "Each ReceiverState should have 1 trMap entry");
            }
        }

        // Step 4: Check sender IDs are sequential and correct
        for (int i = 0; i < nS; i++) {
            SenderState s = initResult.senderStates.get(i);
            assertEquals(i + 1, s.i, "SenderState.i should be sequential starting from 1");
        }

        // Step 5: Check that each receiver's receiverState for a sender has matching svk
        for (SenderState sender : initResult.senderStates) {
            int senderId = sender.i;
            byte[] expectedSvk = sender.svk;

            for (int receiverId = 1; receiverId <= nR; receiverId++) {
                List<ReceiverState> receiverStates = initResult.receiverStatesMap.get(receiverId);
                assertNotNull(receiverStates, "Receiver " + receiverId + " should exist in the map");

                ReceiverState state = receiverStates.get(senderId - 1);  // List is in sender order
                assertArrayEquals(expectedSvk, state.svkMap.get(senderId),
                    "svk in receiver must match sender's svk for sender " + senderId + ", receiver " + receiverId);
            }
        }

        System.out.println("procInit test passed successfully");
    }

//     @Test
//     public void testProcSndAndRcv() throws Exception {
//         int nR = 10;

// System.out.println(" ");
// System.out.println(" ");
// System.out.println("**********************************************");
// System.out.println("* D_MSMR Snd Rcv Test ");
// System.out.println("**********************************************");
//         // Step 1: Initialize
//         InitResult initResult = d_MSMR.procInit(nR);
//         senderState senderState = initResult.senderState;
//         ReceiverState receiverState = initResult.receiverStates.get(0);

//         // Step 2: Generate Associated Data (AD)
//         byte[] ad = new byte[16];
//         new Random().nextBytes(ad);

//         // Step 3: Execute procSnd
//         SendResult sndResult = d_MSMR.procSnd(senderState, ad);
//         assertNotNull(sndResult.ciphertext);
//         assertNotNull(sndResult.key);
//         assertNotNull(sndResult.kid);
//         assertNotNull(sndResult.updatedState);

//         System.out.println("Serializing object of type: " + sndResult.ciphertext.getClass().getName());
//         // Step 4: Execute procRcv with the resulting ciphertext
//         Object rcvOutput = d_MSMR.procRcv(receiverState, ad, sndResult.ciphertext);
        
//         // Verify it's a successful receive (not a failure)
//         assertFalse(rcvOutput instanceof ReceiveFailure, "Receive should not fail");
//         assertTrue(rcvOutput instanceof ReceiveResult, "Should be a successful receive");
        
//         ReceiveResult rcvResult = (ReceiveResult) rcvOutput;

//         // Step 5: Validate output
//         assertNotNull(rcvResult.key);
//         assertArrayEquals(sndResult.key, rcvResult.key, "Shared keys should match");
//         assertArrayEquals(sndResult.kid.id, rcvResult.kid.id, "Kid IDs should match");
//         assertEquals(sndResult.kid.memR.size(), rcvResult.kid.memR.size(), "memR sizes should match");

//         System.out.println("procSnd and procRcv test passed. Shared key (k):");
//         printByteArray(rcvResult.key);

//         // Test multiple send/receive cycles with updated states
//         senderState = sndResult.updatedState;
//         receiverState = rcvResult.updatedState;


//         ////////////////////////////////////////////
//         ////// Test with other receivers as well

//         ReceiverState receiverState2 = initResult.receiverStates.get(2);
//         ReceiverState receiverState3 = initResult.receiverStates.get(3);
//         ReceiverState receiverState4 = initResult.receiverStates.get(4);


//         Object rcv2Output = d_MSMR.procRcv(receiverState2, ad, sndResult.ciphertext);
//         Object rcv3Output = d_MSMR.procRcv(receiverState3, ad, sndResult.ciphertext);
//         Object rcv4Output = d_MSMR.procRcv(receiverState4, ad, sndResult.ciphertext);
        
//         assertTrue(rcv2Output instanceof ReceiveResult, "receiver 2 should also succeed");
//         assertTrue(rcv3Output instanceof ReceiveResult, "receiver 3 should also succeed");
//         assertTrue(rcv4Output instanceof ReceiveResult, "receiver 4 should also succeed");
//         ReceiveResult rcv2Result = (ReceiveResult) rcv2Output;
//         ReceiveResult rcv3Result = (ReceiveResult) rcv3Output;
//         ReceiveResult rcv4Result = (ReceiveResult) rcv4Output;
        
//         assertArrayEquals(sndResult.key, rcv2Result.key, "receiver 2 should match");
//         assertArrayEquals(sndResult.key, rcv3Result.key, "receiver 3 should match");
//         assertArrayEquals(sndResult.key, rcv4Result.key, "receiver 4 should match");
        
//         System.out.println("receiver key 2 (k):");
//         printByteArray(rcv2Result.key);
//         System.out.println("receiver key 3 (k):");
//         printByteArray(rcv3Result.key);
//         System.out.println("receiver key 4 (k):");
//         printByteArray(rcv4Result.key);


//         ////////////////////////////////////////////
//         // Second send

//         // second time send
//         SendResult sndResult2 = d_MSMR.procSnd(senderState, ad);

//         Object rcvOutput2 = d_MSMR.procRcv(receiverState, ad, sndResult2.ciphertext);

//         assertTrue(rcvOutput2 instanceof ReceiveResult, "Second receive should also succeed");
//         ReceiveResult rcvResult2 = (ReceiveResult) rcvOutput2;

//         assertArrayEquals(sndResult2.key, rcvResult2.key, "Second round keys should match");

//         System.out.println("Second round test passed. Shared key (k):");
//         printByteArray(rcvResult2.key);


//         ////////////////////////////////////////////
//         // Test with other receivers as well

//         receiverState2 = rcv2Result.updatedState;
//         receiverState3 = rcv3Result.updatedState;
//         receiverState4 = rcv4Result.updatedState;

//         Object rcv2Output2 = d_MSMR.procRcv(receiverState2, ad, sndResult2.ciphertext);
//         Object rcv3Output2 = d_MSMR.procRcv(receiverState3, ad, sndResult2.ciphertext);
//         Object rcv4Output2 = d_MSMR.procRcv(receiverState4, ad, sndResult2.ciphertext);

//         assertTrue(rcv2Output2 instanceof ReceiveResult, "receiver 2 should also succeed");
//         assertTrue(rcv3Output2 instanceof ReceiveResult, "receiver 3 should also succeed");
//         assertTrue(rcv4Output2 instanceof ReceiveResult, "receiver 4 should also succeed");
//         ReceiveResult rcv2Result2 = (ReceiveResult) rcv2Output2;
//         ReceiveResult rcv3Result2 = (ReceiveResult) rcv3Output2;
//         ReceiveResult rcv4Result2 = (ReceiveResult) rcv4Output2;

//         assertArrayEquals(sndResult2.key, rcv2Result2.key, "receiver 2 should match");
//         assertArrayEquals(sndResult2.key, rcv3Result2.key, "receiver 3 should match");
//         assertArrayEquals(sndResult2.key, rcv4Result2.key, "receiver 4 should match");
        
//         System.out.println("receiver key 2 (k):");
//         printByteArray(rcv2Result2.key);
//         System.out.println("receiver key 3 (k):");
//         printByteArray(rcv3Result2.key);
//         System.out.println("receiver key 4 (k):");
//         printByteArray(rcv4Result2.key);


//         System.out.println("snd second time and all receiver has same key: test passed successfully");

//     }




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