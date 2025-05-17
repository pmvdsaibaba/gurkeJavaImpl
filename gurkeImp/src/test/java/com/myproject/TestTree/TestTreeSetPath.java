package com.myproject.TestTree;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeV2;
import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeDk;
import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Collections;
import java.util.List;

public class TestTreeSetPath {

    @Test
    public void testTreeSetPath() throws Exception {
        
        int groupMem = 7;
        Tree Tree1 = Tree.init(groupMem);
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
        
        for (int i = 1; i <= Treesize; i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            PkList.add(nikeGenKeyPair.getEk());
            skList.add(nikeGenKeyPair.getDk());
        }

        TreeEK ek = Tree1.setNodes(PkList);
        List<byte[]> skListLeaf = new ArrayList<>();
        TreeDk dk;

        List<Integer> pathList = new ArrayList<>();
        List<TreeDk> dkList = new ArrayList<>();

        for (int i = 1; i <= groupMem; i++) 
        {
            pathList = Tree1.T_path(i);
            System.out.println("Path: ");
            printIntList(pathList);

            for (int j = 0; j< pathList.size(); j++)
            {
                skListLeaf.add(skList.get((pathList.get(j)) - 1));
            }

            dk = Tree1.setPath(i,skListLeaf);
            skListLeaf.clear();
            dkList.add(dk);

        }

        for (int i = 0; i < Tree1.getNodesInternal().size(); i++) {
            Tree.Node node = Tree1.getNodesInternal().get(i);
            System.out.println("Node " + (i + 1) + ":");
            System.out.println("  index: " + node.getindex());
            System.out.println("  level: " + node.getnodeLevel());
            System.out.println("  rootNode: " + node.getRootnode());
            System.out.println("  childLeftNode: " + node.getChildLeftnode());
            System.out.println("  childRightNode: " + node.getChildRightnode());
            System.out.println("  isLeaf: " + node.isLeaf());
            System.out.println("  pk: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  sk: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
            // printByteArray(node.getPk());
        }

        // List<Integer> nodeLevelList = new ArrayList<>();
        // for (int i = 0; i < Tree1.getNodesInternal().size(); i++) {
        //     Tree.Node node = Tree1.getNodesInternal().get(i);

        //     if(node.isLeaf() == true) {
        //         nodeLevelList.add(node.getnodeLevel());
        //     }
        // }

        // Collections.sort(nodeLevelList);

        // for (Integer level : nodeLevelList) {
        //     System.out.println(level);
        // }

        // System.out.println(nodeLevelList);

        // List<Integer> nodeIndexList = new ArrayList<>();
        // for (int i = 0; i < Tree1.getNodesInternal().size(); i++) {
        //     Tree.Node node = Tree1.getNodesInternal().get(i);

        //     if(node.isLeaf() == true) {
        //         if(node.getnodeLevel() == nodeLevelList.get(0)){
        //             nodeIndexList.add(node.getindex());
        //         }
        //     }
        // }

        // System.out.println(nodeIndexList);

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
