package com.myproject.Tree;

import com.myproject.Tree.TreeEK;
import java.util.ArrayList;
import java.util.List;


public class Tree {
    private int numLeaves;
    private int size;
    private TreeEK treeEk;

    // node indexes as 1,2,3,.... N
    private List<Integer> nodeIndexes;
    List<Node> internalNode = new ArrayList<>();

    // // Hold the single instance here
    // private static Tree instance;

    public static class Node {
        // Use wrapper types so fields can be null (optional)
        private int index;
        private int rootnode;
        private int leftnode;
        private int rightnode;
        private int nodeLevel;
        private byte[] pk;
        private byte[] sk;
        private boolean isLeaf;

        // Private constructor — only accessible through the builder
        private Node(Builder builder) {
            this.index = builder.index;
            this.rootnode = builder.rootnode;
            this.leftnode = builder.leftnode;
            this.rightnode = builder.rightnode;
            this.nodeLevel = builder.nodeLevel;
            this.pk = builder.pk;
            this.sk = builder.sk;
            this.isLeaf = builder.isLeaf;
        }

        // Builder class
        public static class Builder {
            private int index = -1;
            private int rootnode = -1;
            private int leftnode = -1;
            private int rightnode = -1;
            private int nodeLevel = -1;
            private byte[] pk = null;
            private byte[] sk = null;
            private boolean isLeaf = false;

            public Builder setRootnode(int rootnode) {
                this.rootnode = rootnode;
                return this;
            }

            public Builder setindex(int index) {
                this.index = index;
                return this;
            }

            public Builder setLeftnode(int leftnode) {
                this.leftnode = leftnode;
                return this;
            }

            public Builder setRightnode(int rightnode) {
                this.rightnode = rightnode;
                return this;
            }

            public Builder setnodeLevel(int nodeLevel) {
                this.nodeLevel = nodeLevel;
                return this;
            }

            public Builder setPk(byte[] pk) {
                this.pk = pk;
                return this;
            }

            public Builder setSk(byte[] sk) {
                this.sk = sk;
                return this;
            }
            
            public Builder setIsLeaf(boolean isLeaf) {
                this.isLeaf = isLeaf;
                return this;
            }

            public Node build() {
                return new Node(this);
            }
        }

        public int getindex() { return index; }
        public int getRootnode() { return rootnode; }
        public int getLeftnode() { return leftnode; }
        public int getRightnode() { return rightnode; }
        public int getnodeLevel() { return nodeLevel; }
        public byte[] getPk() { return pk; }
        public byte[] getSk() { return sk; }
        public boolean isLeaf() { return isLeaf; }

        public void setindex(int index) {this.index = index; }
        public void setRootnode(int rootnode) {this.rootnode = rootnode; }
        public void setLeftnode(int leftnode) {this.leftnode = leftnode; }
        public void setRightnode(int rightnode) { this.rightnode = rightnode; }
        public void setnodeLevel(int nodeLevel) { this.nodeLevel = nodeLevel; }
        public void setPk(byte[] pk) { this.pk = pk; }
        public void setSk(byte[] sk) { this.sk = sk;  }
        public void setIsLeaf(boolean isLeaf) {   this.isLeaf = isLeaf;  }
    }

    public void addNode(Node node) {
        internalNode.add(node);
    }

    public List<Node> getNodesInternal() {
        return internalNode;
    }

    public static Tree init(int n) {
        Tree tree = new Tree();
        tree.numLeaves = n;
        tree.size = 2 * n - 1;

        tree.nodeIndexes = new ArrayList<>();

        for (int i = 1; i < 2 * n; i++) {

            tree.nodeIndexes.add(i);

            Tree.Node node = new Tree.Node.Builder()
                .setindex(i)
                .build();

            if( i != 1)
            {
                node.setRootnode(i/2);
                node.setnodeLevel((int) (Math.log(i) / Math.log(2)));
            }

            if((2* i) < (2*n))
            {
                node.setLeftnode(2*i);
            }
            else
            {
                node.setIsLeaf(true);
            }

            if(((2* i) +1) < (2*n))
            {
                node.setRightnode((2*i) + 1);
            }

            tree.addNode(node);
        }

        return tree;
    }

    public int getSize() {
        return size;
    }

    public List<Integer> nodes() {
        List<Integer> nodeIndexesTemp = new ArrayList<>();
        // for (int i = 1; i < 2 * numLeaves; i++) {
        //     nodeIndexesTemp.add(i);
        // }
        nodeIndexesTemp.addAll(this.nodeIndexes);
        return nodeIndexesTemp;
    }

    public List<byte[]> T_getNodes(TreeEK ek) {
        return ek.getDataPk();
    }

    public TreeDK.DkData getPath(TreeDK dk) {
        return dk.getDkPath();
    }


    public TreeEK setNodes(List<byte[]> pkList) {
        TreeEK treeek = new TreeEK();
        // for (int i = 0; i < ekList.size(); i++) {
        //     ek.set(i, ekList.get(i));
        // }
        treeek.setDataPk(pkList);
        this.treeEk = treeek;

        if(pkList.size() == getNodesInternal().size() )
        {
            for (int i = 0; i < getNodesInternal().size(); i++) {
                Tree.Node node = getNodesInternal().get(i);
                node.setPk(pkList.get(i));
            }
        }


        return treeek;
    }

    public TreeDK setPath(int leaf, List<byte[]> skList) {
        // List<Integer> pathIndices = Pathable.path(leaf);
        TreeDK treeDk = new TreeDK();
        // List<Object> dkBranch = new ArrayList<>();
        // for (Integer index : pathIndices) {
        //     dkBranch.add(dks.get(index));
        // }

        List<Integer> leafPath = T_path(leaf);

        for (int i = 0; i < getNodesInternal().size(); i++) 
        {
            Tree.Node node = getNodesInternal().get(i);

            for (int j = 0; j < leafPath.size(); j++)
            {
                if(node.getindex() == leafPath.get(j) )
                {
                    node.setSk(skList.get(j));
                }
            }
        }

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

    public static Object T_add_Ek(TreeEK ek) {
        // Logic for adding a new node and splitting leaves goes here
        return null; // Placeholder
    }

    public static Object T_add_DK(TreeDK dk, int i) {
        // Logic for adding a new decapsulation key
        return null; // Placeholder
    }

    public List<Integer> T_path( int Leaf_i) {
        int leafcount = 0;
        int leafindex;
        int currentIndex = -1;
        List<Integer> returnlist = new ArrayList<>();

        for (int i = 0; i < getNodesInternal().size(); i++)
        {
            Tree.Node node = getNodesInternal().get(i);
            if (node.isLeaf() == true)
            {
                leafcount++;

                if(leafcount == Leaf_i)
                {
                    leafindex = node.getindex();
                    currentIndex = leafindex;
                }
            }
        }

        while (currentIndex != -1)
        {
            Node currentNode = null;

            for (Node node : getNodesInternal()) {
                if (node.getindex() == currentIndex) {
                    currentNode = node;
                    break;
                }
            }

            if (currentNode == null)
            {
                break;
            }
            returnlist.add(currentIndex);
            currentIndex = currentNode.getRootnode();
        }

        // return Pathable.path(numLeaves +  Leaf_i -2);
        return returnlist;
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
