package com.myproject.TestDynUBKem;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import com.myproject.Tree.Tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


public class TestDynUBKemAdd {

    @Test
    public void testUBKemAdd() throws Exception {

        int n = 5;

        UB_KEM ubKem = new UB_KEM();

        UB_KEM.BKGenResult result = ubKem.gen(n);
        TreeEK ek = result.ek;
        Map<Integer, byte[]> pkMap = ek.getDataPk();
        TreeDk dk = result.dkList.get(0);

        printTreeStateAfterAddEk(ek.getTree());

        UB_KEM.BKEncResult encResult = ubKem.enc(ek);

        byte[] ad = new byte[32];
        Arrays.fill(ad, (byte) 0xEF);

        UB_KEM.FinResult finResult = ubKem.fin(encResult.u, ad);
        UB_KEM.DecResult decResult = ubKem.dec(dk, ad, encResult.c);

        printTreeStateAfterAddEk(ek.getTree());

        System.out.println("ubKem.dec and ubKem.fin succeeded. Shared key (k):");
        printByteArray(decResult.k);
        printByteArray(finResult.k);

        UB_KEM.BKAddResult addResult = ubKem.add(finResult.ek);
        TreeEK newEk = addResult.ek;
        TreeDk newDk = addResult.dk;

        printTreeStateAfterAddEk(ek.getTree());

        UB_KEM.BKEncResult encResultAfterAdd = ubKem.enc(newEk);
        UB_KEM.FinResult finResultAfterAdd = ubKem.fin(encResultAfterAdd.u, ad);
        UB_KEM.DecResult decResultAfterAdd = ubKem.dec(newDk, ad, encResultAfterAdd.c);

        assertArrayEquals(finResultAfterAdd.k, decResultAfterAdd.k, "Shared key mismatch after adding user");

        printTreeStateAfterAddEk(ek.getTree());

        System.out.println("Shared key after add:");
        printByteArray(finResultAfterAdd.k);
        printByteArray(decResultAfterAdd.k);
    }

    // Utility method to print byte arrays in a readable format
    private void printByteArray(byte[] byteArray) {
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

    private void printTreeStateAfterAddEk(Tree tree) throws Exception {

        List<Integer> pathList;
        List<Integer> copathList;

        System.out.println();
        System.out.println("******************************************************************");
        System.out.println("nodes in Tree:");
        System.out.println("******************************************************************");
        System.out.println();

        for (int i = 0; i < tree.getNodesInternal().size(); i++) {
            Tree.Node node = tree.getNodesInternal().get(i);
            System.out.println("Node " + (i + 1) + ":");
            // System.out.println("  nodeIndex: " + node.getNodeIndex());
            // System.out.println("  level: " + node.getNodeLevel());
            // System.out.println("  rootNode: " + node.getRootnode());
            // System.out.println("  childLeftNode: " + node.getChildLeftnode());
            // System.out.println("  childRightNode: " + node.getChildRightnode());
            // System.out.println("  isLeaf: " + node.isLeaf());
            // System.out.println("  leafIndex: " + node.getLeafIndex());
            System.out.println("  pk: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  sk: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
        }

        System.out.print("Size of tree: ");
        System.out.println(tree.getSize());
        System.out.print("Number of leafs: ");
        System.out.println(tree.getNumOfLeaf());
        System.out.print("Node indexes Max: ");
        System.out.println(tree.getNodeIndexMax());
        System.out.print("Node indexes: ");
        printIntList(tree.getNodeIndexes());
        System.out.print("Leaf indexes Max: ");
        System.out.println(tree.getLeafIndexMax());
        System.out.print("Leaf indexes: ");
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
            if (node.getRootnode() == -1) { // rootNode == -1 or similar logic
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
}
