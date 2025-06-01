package com.myproject.TestTree;


import com.myproject.Tree.TreeAddEkReturn;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeV2;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;



import java.util.ArrayList;
import java.util.Arrays;

import java.security.NoSuchProviderException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Collections;
import java.util.List;

public class TestTreeAddEk {

    @Test
    public void testTreeAddEk() throws Exception {

        int groupMem = 10;
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
        List<Integer> copathList = new ArrayList<>();
        List<TreeDk> dkList = new ArrayList<>();

        for (int i = 1; i <= groupMem; i++) 
        {
            pathList = Tree1.T_path(i);
            // System.out.println("Path: ");
            // printIntList(pathList);

            for (int j = 0; j< pathList.size(); j++)
            {
                skListLeaf.add(skList.get((pathList.get(j)) - 1));
            }

            dk = Tree1.setPath(i,skListLeaf);
            skListLeaf.clear();
            dkList.add(dk);

        }

        for (int i = 1; i <= (groupMem); i++) {
            pathList = Tree1.T_path(i);
            System.out.println("Path: ");
            printIntList(pathList);

            copathList = Tree1.T_co_path(i);
            System.out.println("co Path: ");
            printIntList(copathList);
        }
        printTreeDiagram(Tree1);

        printTreeStateAfterAddEk(Tree1, ek);
        printTreeStateAfterAddEk(Tree1, ek);
        printTreeStateAfterAddEk(Tree1, ek);
        printTreeStateAfterAddEk(Tree1, ek);
        printTreeStateAfterAddEk(Tree1, ek);


        nodes = Tree1.nodes(); 

        System.out.println("Nodes in the Tree ");
        printIntList(nodes);
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

    private void printTreeStateAfterAddEk(Tree tree, TreeEK ek) throws Exception {
        TreeAddEkReturn addEkReturn = Tree.T_add_Ek(ek);

        List<Integer> pathList;
        List<Integer> copathList;

        System.out.println();
        System.out.println("******************************************************************");
        System.out.println("Add a leaf");
        System.out.println("******************************************************************");
        System.out.println();

        for (int i = 0; i < tree.getNodesInternal().size(); i++) {
            Tree.Node node = tree.getNodesInternal().get(i);
            System.out.println("Node " + (i + 1) + ":");
            System.out.println("  nodeIndex: " + node.getNodeIndex());
            System.out.println("  level: " + node.getNodeLevel());
            System.out.println("  rootNode: " + node.getRootnode());
            System.out.println("  childLeftNode: " + node.getChildLeftnode());
            System.out.println("  childRightNode: " + node.getChildRightnode());
            System.out.println("  isLeaf: " + node.isLeaf());
            System.out.println("  leafIndex: " + node.getLeafIndex());
            System.out.println("  pk: " + (node.getPk() != null ? Arrays.toString(node.getPk()) : "null"));
            System.out.println("  sk: " + (node.getSk() != null ? Arrays.toString(node.getSk()) : "null"));
            System.out.println();
        }

        for (int i = 1; i <= addEkReturn.getLeafsCount(); i++) {
            pathList = tree.T_path(i);
            System.out.print("Path of the leaf " + (i) + ": ");
            printIntList(pathList);

            copathList = tree.T_co_path(i);
            System.out.print("CoPath of the leaf " + (i) + ": ");
            printIntList(copathList);
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

        System.out.println("Path of the newly added leaf: " + addEkReturn.getPathList());
        System.out.println("CoPath of the newly added leaf: " + addEkReturn.getCoPathList());
        System.out.println("Number of leafs: " + addEkReturn.getLeafsCount());
        System.out.println();

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
