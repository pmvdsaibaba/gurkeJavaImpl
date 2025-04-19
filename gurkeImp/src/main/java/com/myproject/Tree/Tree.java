package com.myproject.Tree;

import com.myproject.Tree.TreeEK;
import java.util.ArrayList;
import java.util.List;




public class Tree {
    private int numLeaves;
    private int size;
    private TreeEK treeEk;

    // // Hold the single instance here
    // private static Tree instance;

    public static Tree init(int n) {
        Tree tree = new Tree();
        tree.numLeaves = n;
        tree.size = 2 * n - 1;
        return tree;
    }

    public int getSize() {
        return size;
    }

    public List<Integer> nodes() {
        List<Integer> nodeIndexes = new ArrayList<>();
        for (int i = 1; i < 2 * numLeaves; i++) {
            nodeIndexes.add(i);
        }
        return nodeIndexes;
    }

    public List<byte[]> getNodes(TreeEK ek) {
        return ek.getDataPk();
    }

    public TreeEK setNodes(List<byte[]> pkList) {
        TreeEK treeek = new TreeEK();
        // for (int i = 0; i < ekList.size(); i++) {
        //     ek.set(i, ekList.get(i));
        // }
        treeek.setDataPk(pkList);
        this.treeEk = treeek;

        return treeek;
    }

    public static TreeDK setPath(int leaf, List<byte[]> skList) {
        // List<Integer> pathIndices = Pathable.path(leaf);
        TreeDK treeDk = new TreeDK();
        // List<Object> dkBranch = new ArrayList<>();
        // for (Integer index : pathIndices) {
        //     dkBranch.add(dks.get(index));
        // }

        treeDk.setDataSk(skList);
        treeDk.setLeaf(leaf);
        return treeDk;
    }

    public static List<Integer> rmEK(TreeEK ek, int i) {
        List<Integer> p = ek.path(i);
        List<Integer> cp = ek.copath(i);
        List<Integer> result = new ArrayList<>();
        result.addAll(p);
        result.addAll(cp);
        return result;
    }

    public static int intersectionDepth(int node1, int node2) {
        List<Integer> path1 = Pathable.path(node1);
        List<Integer> path2 = Pathable.path(node2);
        int i = 0;
        while (i < path1.size() && i < path2.size() && path1.get(i).equals(path2.get(i))) {
            i++;
        }
        return i - 1;
    }

    public static int rmDK(TreeDK dk, int iprime) {
        List<Integer> path1 = dk.path();
        List<Integer> path2 = Pathable.path(iprime);
        int i = 0;
        while (i < path1.size() && path1.get(i).equals(path2.get(i))) {
            i++;
        }
        return i - 1;
    }

    public static Object add(TreeEK ek) {
        // Logic for adding a new node and splitting leaves goes here
        return null; // Placeholder
    }

    public static Object addDK(TreeDK dk, int i) {
        // Logic for adding a new decapsulation key
        return null; // Placeholder
    }
    public List<Integer> T_path( int i) {
        // Logic for adding a new decapsulation key
        // int leaf = numLeaves +  i -1 ;
        return Pathable.path(numLeaves +  i -2); // Placeholder
    }

    // public static Tree getInstance() {
    // if (instance == null) {
    //     // throw new IllegalStateException("Tree is not initialized. Call Tree.init(n) first.");
    // }
    // return instance;
    // }
}

class Pathable {
    public static List<Integer> path(int leaf) {
        return Path(leaf);
    }

    public static List<Integer> copath(int leaf) {
        return Copath(leaf);
    }

    // Static methods for path and copath logic
    public static List<Integer> Path(int leaf) {
        List<Integer> path = new ArrayList<>();
        int a = leaf + 1;
        while (a != 1) {
            // System.out.println(a-1);
            path.add(a - 1);
            a /= 2;
        }
        path.add(0);
        path.sort((x, y) -> y - x); // Reverse the list
        return path;
    }

    public static List<Integer> Copath(int leaf) {
        List<Integer> path = Path(leaf);
        List<Integer> copath = new ArrayList<>();
        for (int i = 1; i < path.size(); i++) {
            int p = path.get(i);
            int cop = (p % 2 != 0) ? p + 1 : p - 1;
            copath.add(cop);
        }
        return copath;
    }
}



// Utility functions for tree structure
class TreeUtils {

    public static int parent(int node) {
        return ((node + 1) / 2) - 1;
    }

    public static int leftChild(int node) {
        return (node + 1) * 2 - 1;
    }

    public static int rightChild(int node) {
        return (node + 1) * 2;
    }

    public static int sibling(int node) {
        return (node % 2 == 1) ? node + 1 : node - 1;
    }

    public static int depth(int node) {
        return Integer.toBinaryString(node + 1).length() - 1;
    }

    // this is bit unclear for now. 
    public static int newLeaf(int leaf, int deletionDepth) {
        int e = depth(leaf) - deletionDepth;
        int gs = (int) Math.pow(2, e - 1);
        int gn = (leaf + 1) % gs;
        int pl = parent(leaf) + 1;
        int pg = pl - (pl % gs);
        return pg + gn - 1;
    }

    public static void move(List<Object> tree, int target, int home) {
        int at = target;
        int ah = home;
        List<Integer> stack1 = new ArrayList<>();
        List<Integer> stack2 = new ArrayList<>();

        while (true) {
            while (ah < tree.size() && tree.get(ah) != null) {
                stack1.add(ah);
                stack2.add(at);
                tree.set(at, tree.get(ah));
                tree.set(ah, null);
                ah = leftChild(ah);
                at = leftChild(at);
            }
            while (!stack1.isEmpty()) {
                int p = stack1.remove(stack1.size() - 1);
                int t = stack2.remove(stack2.size() - 1);
                if (p % 2 == 1) {
                    break;
                }
            }
            if (stack1.isEmpty()) {
                break;
            }
            ah = stack1.remove(stack1.size() - 1) + 1;
            at = stack2.remove(stack2.size() - 1) + 1;
        }
    }
}
