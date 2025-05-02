package com.myproject.TestTree;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;

import java.util.List;

public class TestTreeSetNodes {

    @Test
    public void testTreeSetNodes() throws Exception {
        
        Tree Tree1 = Tree.init(5);
        int Treesize = Tree1.getSize();
        List<Integer> nodes = Tree1.nodes(); 

        assertNotNull(nodes);

        System.out.println("Size of the Tree: ");
        System.out.println(Treesize);


        System.out.println("Nodes in the Tree ");
        printIntList(nodes);

        List<byte[]> PkList = new ArrayList<>();
        List<byte[]> skList = new ArrayList<>();
        Nike.KeyPair NikeGenKeyPair;
        
        for (int i = 0; i < Treesize; i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            PkList.add(nikeGenKeyPair.getEk());
            skList.add(nikeGenKeyPair.getDk());
        }

        System.out.println("PkList:");
        for (byte[] ek : PkList) {
            printByteArray(ek);
        }

        System.out.println("skList:");
        for (byte[] dk : skList) { 
            printByteArray(dk);
        }

        TreeEK ek = Tree1.setNodes(PkList);


        for (int i = 0; i < Tree1.getNodesInternal().size(); i++) {
            Tree.Node node = Tree1.getNodesInternal().get(i);
            System.out.println("Node " + (i + 1) + ":");
            System.out.println("  rootnode: " + node.getRootnode());
            System.out.println("  leftnode: " + node.getLeftnode());
            System.out.println("  rightnode: " + node.getRightnode());
            System.out.println("  isLeaf: " + node.isLeaf());
            System.out.println("  pk: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  sk: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
            // printByteArray(node.getPk());
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
