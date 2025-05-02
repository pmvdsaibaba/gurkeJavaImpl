package com.myproject.TestTree;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeV2;
import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeDK;
import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;

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
        
        for (int i = 0; i < Treesize; i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            PkList.add(nikeGenKeyPair.getEk());
            skList.add(nikeGenKeyPair.getDk());
        }

        // System.out.println("PkList:");
        // for (byte[] ek : PkList) {
        //     printByteArray(ek);
        // }

        // System.out.println("skList:");
        // for (byte[] dk : skList) { 
        //     printByteArray(dk);
        // }

        TreeEK ek = Tree1.setNodes(PkList);
        List<byte[]> skListLeaf = new ArrayList<>();
        TreeDK dk = Tree1.setPath(1,skList);

        List<Integer> pathList = Tree1.T_path(1);
        List<TreeDK> dkList = new ArrayList<>();

        for (int i = 1; i <= groupMem; i++) {
            pathList = Tree1.T_path(i);
            System.out.println("Path: ");
            printIntList(pathList);
            for (int j = 0; j< pathList.size(); j++)
            {
                skListLeaf.add(skList.get(pathList.get(j)));
            }
            // System.out.println("Size of the skListLeaf: ");
            // System.out.println(skListLeaf.size());
            skListLeaf.clear();
            dk = Tree1.setPath(i,skListLeaf);
            dkList.add(dk);

        }
            // System.out.println("Size of the dkList: ");
            // System.out.println(dkList.size());

// sk0 = skList.get(0);
// YourType sk4 = skList.get(4);




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
