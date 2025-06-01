package com.myproject.TestTree;

import com.myproject.Tree.Tree;

import com.myproject.Tree.TreeGetPathReturn;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeV2;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.HashMap;

import java.util.List;

public class TestTreeGetPath {

    @Test
    public void testTreeGetPath() throws Exception {
        
        int groupMem = 7;
        Tree Tree1 = Tree.init(groupMem);
        int Treesize = Tree1.getSize();
        List<Integer> nodes = Tree1.nodes(); 

        assertNotNull(nodes);

        System.out.println("Size of the Tree: ");
        System.out.println(Treesize);


        System.out.println("Nodes in the Tree ");
        printIntList(nodes);

        Map<Integer, byte[]> pkMap = new HashMap<>();
        Map<Integer, byte[]> skMap = new HashMap<>();
        Nike.KeyPair NikeGenKeyPair;
        
        for (int i = 1; i <= Treesize; i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            pkMap.put(i , nikeGenKeyPair.getEk());
            skMap.put(i, nikeGenKeyPair.getDk());
        }

        System.out.println("PkMap:");

        for (Map.Entry<Integer, byte[]> entry : pkMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] pk = entry.getValue();
            System.out.print("nodeId: " + (nodeId) + " : ");

            printByteArray(pk);
        }

        System.out.println("skMap:");

        for (Map.Entry<Integer, byte[]> entry : skMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] sk = entry.getValue();
            printByteArray(sk);
        }

        TreeEK ek = Tree1.setNodes(pkMap);

        List<Integer> pathList = new ArrayList<>();
        List<Integer> copathList = new ArrayList<>();
        List<TreeDk> dkList = new ArrayList<>();

        for (int i = 1; i <= groupMem; i++) {
            pathList = Tree1.T_path(i);
            System.out.println("Path: ");
            printIntList(pathList);

            copathList = Tree1.T_co_path(i);
            System.out.println("co Path: ");
            printIntList(copathList);

            Map<Integer, byte[]> skMapLeaf = new HashMap<>();

            for (Integer pathNodeIndex : pathList) {
                skMapLeaf.put(pathNodeIndex, skMap.get(pathNodeIndex));
            }

            System.out.println("skMapLeaf contents:");

            for (Map.Entry<Integer, byte[]> entry : skMapLeaf.entrySet()) {
                System.out.println("Key: " + entry.getKey());
                printByteArray(entry.getValue());

            }

            TreeDk dk = Tree1.setPath(i, skMapLeaf);
            dkList.add(dk);

        }

        for (int j = 0; j < dkList.size(); j++)
        {
            TreeGetPathReturn dkGet = Tree.getPath(dkList.get(j));
            Map<Integer, byte[]> dkGetSK = dkGet.getDataSk();
            Integer dkGetLeaf = dkGet.getLeafIndex();

            System.out.println("skListGetPath:");

            for (Map.Entry<Integer, byte[]> entry : dkGetSK.entrySet()) {
                int nodeId = entry.getKey();
                byte[] dk = entry.getValue();
                printByteArray(dk);
            }
            System.out.print("Leaf is: ");
            System.out.println(dkGetLeaf);
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
