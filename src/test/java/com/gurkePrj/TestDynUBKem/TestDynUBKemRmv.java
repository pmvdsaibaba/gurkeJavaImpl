package com.gurkePrj.TestDynUBKem;

import com.gurkePrj.dynamicUBKem.UB_KEM;
import com.gurkePrj.Tree.TreeDk;
import com.gurkePrj.Tree.TreeEK;
import com.gurkePrj.Tree.Tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


public class TestDynUBKemRmv {

    @Test
    public void testUBKemRmv() throws Exception {

        int n = 5;

        UB_KEM ubKem = new UB_KEM();

        UB_KEM.BKGenResult result = ubKem.gen(n);
        TreeEK ek = result.ek;
        List<TreeDk> dkList = result.dkList;

        byte[] ad = new byte[32];
        Arrays.fill(ad, (byte) 0xAB);

        // Encrypt and derive key
        UB_KEM.BKEncResult encResult = ubKem.enc(ek);
        UB_KEM.FinResult finResult = ubKem.fin(encResult.u, ad);

        // Pick a non-target user to validate post-removal (e.g., user 0)
        TreeDk dkBeforeRemove = dkList.get(0);
        UB_KEM.DecResult decResultBefore = ubKem.dec(dkBeforeRemove, ad, encResult.c);

        System.out.println("Shared key before removal:");
        printByteArray(finResult.k);
        printByteArray(decResultBefore.k);

        printTreeStateAfterRemoveEk(finResult.ek.getTree());

        int removeIndex = 2;
        UB_KEM.BKRemoveResult removeResult = ubKem.rmv(finResult.ek, removeIndex);

        TreeEK newEk = removeResult.ek;

        UB_KEM.BKEncResult encResultAfter = ubKem.enc(newEk);
        UB_KEM.FinResult finResultAfter = ubKem.fin(encResultAfter.u, ad);

        // Decrypt with a user who was NOT removed (e.g., same user as before)
        UB_KEM.DecResult decResultAfter = ubKem.dec(dkBeforeRemove, ad, encResultAfter.c);

        System.out.println("Shared key after removal:");
        printByteArray(finResultAfter.k);
        printByteArray(decResultAfter.k);

        // assertArrayEquals(finResultAfter.k, decResultAfter.k, "Shared key mismatch after user removal");

        printTreeStateAfterRemoveEk(newEk.getTree());
    }

    private void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }

    public static void printIntList(List<Integer> intList) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : intList) {
            sb.append(i).append(" ");
        }
        System.out.println(sb.toString().trim());
    }

    private void printTreeStateAfterRemoveEk(Tree tree) throws Exception {
        System.out.println("\n******************************************************************");
        System.out.println("Tree State After Removal:");
        System.out.println("******************************************************************");

        for (Tree.Node node : tree.getNodesInternal()) {
            System.out.println("Node " + node.getNodeIndex() + ":");
            System.out.println("  Level: " + node.getNodeLevel());
            System.out.println("  Root: " + node.getRootnode());
            System.out.println("  Left: " + node.getChildLeftnode());
            System.out.println("  Right: " + node.getChildRightnode());
            System.out.println("  Is Leaf: " + node.isLeaf());
            System.out.println("  Leaf Index: " + node.getLeafIndex());
            System.out.println("  PK: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  SK: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
        }

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
}