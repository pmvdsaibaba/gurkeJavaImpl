package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

class TreeDK {
    // A UBKEM decapsulation key
    private List<Object> dataSk;
    private Integer leaf;

    public TreeDK(int initialDepth, List<Object> dkList, Integer leaf) {
        if (dkList != null) {
            this.dataSk = dkList;
        } else {
            this.dataSk = new ArrayList<>();
            for (int i = 0; i < initialDepth; i++) {
                this.dataSk.add(null);
            }
        }
        this.leaf = leaf;
    }

    public Object pop(int index) {
        return dataSk.remove(index);
    }

    public Object get(int index) {
        return dataSk.get(index);
    }

    public void set(int index, Object value) {
        dataSk.set(index, value);
    }

    public int size() {
        return dataSk.size();
    }

    public void append(Object item) {
        dataSk.add(item);
    }

    public List<Integer> path() {
        return Pathable.path(this.leaf);
    }
}



class TreeEK {
    private int depth;
    private List<byte[]> dataPk;

    public TreeEK() {
        this.depth = 3;
        // this.dataPk = new ArrayList<>();
        // for (int i = 0; i < (int) Math.pow(2, depth) - 1; i++) {
        //     this.dataPk.add(null);
        // }
    }

    public void setDataPk(List<byte[]> dataPk) {
        this.dataPk = dataPk;
    }

    public List<byte[]> getDataPk() {
        return dataPk;
    }

    public Object get(int index) {
        return dataPk.get(index);
    }

    // public void set(int index, Object value) {
    //     if (index >= this.size()) {
    //         // Expanding the list as needed
    //         int newDepth = (int) Math.log(index + 1) + 1;
    //         for (int i = this.size(); i < (int) Math.pow(2, newDepth) - 1; i++) {
    //             dataPk.add(null);
    //         }
    //         this.depth = newDepth;
    //     }
    //     dataPk.set(index, value);
    // }

    public int size() {
        return dataPk.size();
    }

    public List<Integer> path(int i) {
        return Pathable.Path(i);
    }

    public List<Integer> copath(int i) {
        return Pathable.Copath(i);
    }

    public int getDepth() {
        return depth;
    }

    public int getSize() {
        return (int) Math.pow(2, depth) - 1;
    }
}

public class Tree {
    private int numLeaves;
    private int size;
    private TreeEK treeEk;

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

    public Object[] getNodes(TreeEK ek) {
        return new Object[]{ek}; // Example, replace with actual logic
    }

    public TreeEK setNodes(List<byte[]> pkList) {
        TreeEK ek = new TreeEK();
        // for (int i = 0; i < ekList.size(); i++) {
        //     ek.set(i, ekList.get(i));
        // }
        ek.setDataPk(pkList);
        this.treeEk = ek;

        return ek;
    }

    public static TreeDK setPath(int leaf, List<Object> dks) {
        List<Integer> pathIndices = Pathable.path(leaf);
        List<Object> dkBranch = new ArrayList<>();
        for (Integer index : pathIndices) {
            dkBranch.add(dks.get(index));
        }
        return new TreeDK(0, dkBranch, leaf); // 0 depth as placeholder
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
