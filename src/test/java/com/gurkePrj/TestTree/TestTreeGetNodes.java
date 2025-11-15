package com.gurkePrj.TestTree;

import com.gurkePrj.Tree.Tree;
import com.gurkePrj.Tree.TreeEK;
import com.gurkePrj.Tree.TreeGetNodesReturn;
import com.gurkePrj.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.HashMap;

import java.util.List;

public class TestTreeGetNodes {

    @Test
    public void testTreeGetNodes() throws Exception {
        
        Tree Tree1 = Tree.init(13);
        int Treesize = Tree1.getSize();
        List<Integer> nodes = Tree1.nodes(); 

        assertNotNull(nodes);

        System.out.println("Size of the Tree: ");
        System.out.println(Treesize);


        System.out.println("Nodes in the Tree ");
        printIntList(nodes);

        System.out.println("Tree Nodes:");

        Map<Integer, byte[]> pkMap = new HashMap<>();
        Map<Integer, byte[]> skMap = new HashMap<>();

        Nike.KeyPair NikeGenKeyPair;
        
        for (int i = 0; i < Treesize; i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            pkMap.put(i + 1, nikeGenKeyPair.getEk());
            skMap.put(i + 1, nikeGenKeyPair.getDk());
        }

        System.out.println("pkMap:");
        // for (byte[] ek : PkList) {
        //     printByteArray(ek);
        // }

        for (Map.Entry<Integer, byte[]> entry : pkMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] pkj = entry.getValue();
            printByteArray(pkj);
        }

        System.out.println("skMap:");
        // for (byte[] dk : skList) { 
        //     printByteArray(dk);
        // }

        for (Map.Entry<Integer, byte[]> entry : skMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] skj = entry.getValue();
            printByteArray(skj);
        }


        TreeEK ek2 = Tree1.setNodes(pkMap);

        TreeGetNodesReturn getnodesreturn = Tree.getNodes(ek2);
        Map<Integer, byte[]> PkMapGet = getnodesreturn.getDataPk();


        System.out.println("PkListGet:");
        // for (byte[] ek : PkListGet) {
        //     printByteArray(ek);
        // }

        for (Map.Entry<Integer, byte[]> entry : PkMapGet.entrySet()) {
            int nodeId = entry.getKey();
            byte[] pkj = entry.getValue();
            printByteArray(pkj);
        }

        for (int i = 0; i < Tree1.getNodesInternal().size(); i++) {
            Tree.Node node = Tree1.getNodesInternal().get(i);
            System.out.println("Node " + (i + 1) + ":");
            System.out.println("  nodeIndex: " + node.getNodeIndex());
            System.out.println("  level: " + node.getNodeLevel());
            System.out.println("  rootNode: " + node.getRootnode());
            System.out.println("  childLeftNode: " + node.getChildLeftnode());
            System.out.println("  childRightNode: " + node.getChildRightnode());
            System.out.println("  isLeaf: " + node.isLeaf());
            System.out.println("  pk: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  sk: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
        }



    }
    
    // Utility method to print byte arrays in a readable format
    public static void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
        }
        System.out.println(sb.toString());
    }

    // Utility method to print List of integers
    public static void printIntList(List<Integer> intList) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : intList) {
            sb.append(String.format("%d ", i));  // Convert to decimal representation
        }
        System.out.println(sb.toString().trim());  // Remove the trailing space
    }
}
