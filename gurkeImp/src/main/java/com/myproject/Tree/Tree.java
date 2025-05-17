package com.myproject.Tree;

import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeAddEkReturn;
import com.myproject.Tree.TreeAddDkReturn;
import com.myproject.Tree.TreeGetPathReturn;
import com.myproject.Tree.TreeDkReturn;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Tree {
    private int numLeaves;
    private int treeSize;
    private TreeEK treeEk;

    // node indexes as 1,2,3,.... N
    private List<Integer> nodeIndexes;
    List<Node> internalNode = new ArrayList<>();

    // // Hold the single instance here
    // private static Tree instance;


    // variables used in T.Add dk
    private int node1Index;
    private int node2Index;

    public static class Node {
        // Use wrapper types so fields can be null (optional)
        private int index;
        private int rootNode;
        private int childLeftNode;
        private int childRightNode;
        private int nodeLevel;
        private byte[] pk;
        private byte[] sk;
        private boolean isLeaf;

        // Private constructor — only accessible through the builder
        private Node(Builder builder) {
            this.index = builder.index;
            this.rootNode = builder.rootNode;
            this.childLeftNode = builder.childLeftNode;
            this.childRightNode = builder.childRightNode;
            this.nodeLevel = builder.nodeLevel;
            this.pk = builder.pk;
            this.sk = builder.sk;
            this.isLeaf = builder.isLeaf;
        }

        // Builder class
        public static class Builder {
            private int index = -1;
            private int rootNode = -1;
            private int childLeftNode = -1;
            private int childRightNode = -1;
            private int nodeLevel = -1;
            private byte[] pk = null;
            private byte[] sk = null;
            private boolean isLeaf = false;

            public Builder setRootnode(int rootNode) {
                this.rootNode = rootNode;
                return this;
            }

            public Builder setindex(int index) {
                this.index = index;
                return this;
            }

            public Builder setChildLeftnode(int childLeftNode) {
                this.childLeftNode = childLeftNode;
                return this;
            }

            public Builder setChildRightnode(int childRightNode) {
                this.childRightNode = childRightNode;
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
        public int getRootnode() { return rootNode; }
        public int getChildLeftnode() { return childLeftNode; }
        public int getChildRightnode() { return childRightNode; }
        public int getnodeLevel() { return nodeLevel; }
        public byte[] getPk() { return pk; }
        public byte[] getSk() { return sk; }
        public boolean isLeaf() { return isLeaf; }

        public void setindex(int index) {this.index = index; }
        public void setRootnode(int rootNode) {this.rootNode = rootNode; }
        public void setChildLeftnode(int childLeftNode) {this.childLeftNode = childLeftNode; }
        public void setChildRightnode(int childRightNode) { this.childRightNode = childRightNode; }
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
        tree.treeSize = 2 * n - 1;

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
                node.setChildLeftnode(2*i);
            }
            else
            {
                node.setIsLeaf(true);
            }

            if(((2* i) +1) < (2*n))
            {
                node.setChildRightnode((2*i) + 1);
            }

            tree.addNode(node);
        }

        return tree;
    }

    public int getSize() {
        return treeSize;
    }
    public int getNumOfLeaf() {
        return numLeaves;
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

    public TreeGetPathReturn getPath(TreeDkReturn dk) {
        return new TreeGetPathReturn(dk.getDataSk(), dk.getLeaf());
    }


    public TreeEK setNodes(List<byte[]> pkList) {
        TreeEK treeek = new TreeEK();

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

    public TreeDkReturn setPath(int leaf, List<byte[]> skList) {
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

        return new TreeDkReturn(skList, leaf);
    }


    private void treeAddInternal(){ 
        // List to store the levels of all leaf nodes
        List<Integer> nodeLevelList = new ArrayList<>();

        // Loop through all internal nodes and collect the level of each leaf node
        for (int i = 0; i < getNodesInternal().size(); i++) {
            Tree.Node node = getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                nodeLevelList.add(node.getnodeLevel());
            }
        }

        // Sort the levels to find the leaf node(s) with the lowest level
        Collections.sort(nodeLevelList);

        // List to store indexes of all leaf nodes that have the minimum level
        List<Integer> nodeIndexList = new ArrayList<>();
        for (int i = 0; i < getNodesInternal().size(); i++) {
            Tree.Node node = getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                if (node.getnodeLevel() == nodeLevelList.get(0)) {
                    nodeIndexList.add(node.getindex());
                }
            }
        }

        // Sort the node indexes to pick the leftmost (smallest index) leaf node with the lowest level
        Collections.sort(nodeIndexList);
        System.out.print("leftmost (smallest index) leaf node: ");
        System.out.println(nodeIndexList);

        // Find the position of that node in the internal node list
        int nodeIndex = -1;
        for (int i = 0; i < getNodesInternal().size(); i++) {
            Tree.Node node = getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                if (node.getindex() == nodeIndexList.get(0)) {
                    nodeIndex = i;
                }
            }
        }

        System.out.print("position of the node: ");
        System.out.println(nodeIndex);

        // Get the actual leaf node object
        Tree.Node currentLeafNode = getNodesInternal().get(nodeIndex);

        // Find the position of that node in the internal node list
        int parentNodeIndex = -1;
        for (int i = 0; i < getNodesInternal().size(); i++) {
            Tree.Node node = getNodesInternal().get(i);

            if (node.getindex() == currentLeafNode.getRootnode()) {
                parentNodeIndex = i;
            }
        }

        Tree.Node currentLeafNodeParent = getNodesInternal().get(parentNodeIndex);


        // Create a new internal node (node1) to replace the current leaf node
        Tree.Node node1 = new Tree.Node.Builder()
            .setindex(getNodesInternal().size() + 1)
            .build();

        if (nodeIndex != -1) {
            // Set properties of the new internal node based on the current leaf node
            node1.setRootnode(currentLeafNode.getRootnode());  // Inherit parent
            node1.setnodeLevel(currentLeafNode.getnodeLevel());  // Same level as leaf
            node1.setChildLeftnode(currentLeafNode.getindex());  // Left child is current leaf
            node1.setChildRightnode(getNodesInternal().size() + 2);  // Right child will be the new leaf
        }


        addNode(node1);

        // Create a new leaf node (node2) as the right child of node1
        Tree.Node node2 = new Tree.Node.Builder()
            .setindex(getNodesInternal().size() + 1)  // New index
            .build();

        node2.setRootnode(getNodesInternal().size());  // Its parent is the newly added node1
        node2.setnodeLevel(currentLeafNode.getnodeLevel() + 1);  // Level is increased
        node2.setIsLeaf(true);  // It's a leaf

        // Update the original leaf node to now be an internal node with updated level and parent
        currentLeafNode.setRootnode(getNodesInternal().size());  // New parent
        currentLeafNode.setnodeLevel(node2.getnodeLevel());  // Updated level

        if (currentLeafNodeParent.getChildLeftnode() == currentLeafNode.getindex()) {
            currentLeafNodeParent.setChildLeftnode(node1.getindex());
        } else if (currentLeafNodeParent.getChildRightnode() == currentLeafNode.getindex()) {
             currentLeafNodeParent.setChildRightnode(node1.getindex());
        }

        // Add the new leaf node to the tree
        addNode(node2);

        treeSize++;
        treeSize++;
        numLeaves++;

        node1Index = node1.getindex();
        node2Index = node2.getindex();
    }

    public TreeAddEkReturn T_add_Ek(TreeEK ek)
    {
        treeAddInternal();

        return new TreeAddEkReturn(ek.getDataPk(), T_path(numLeaves), T_co_path(numLeaves), numLeaves);
    }

    // public TreeAddDkReturn T_add_dk(TreeDK dk)
    // {
    //     treeAddInternal();
    //     TreeDK.DkData dkData = dk.DkData();
    //     return new TreeAddDkReturn(dkData.getDataSk(), node2Index, node1Index);
    // }


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


    public List<Integer> T_co_path( int Leaf_i) {
        List<Integer> copath = new ArrayList<>();

        // Step 1: Get path from selected leaf to root
        List<Integer> path  = T_path(Leaf_i);


        if (path.size() < 2) return copath; // no co-path if only one node

        // Start from 2nd node in path (index 1), because first is the leaf
        for (int i = 1; i < path.size(); i++) {
            int childIndex = path.get(i - 1);  // 3
            int parentIndex = path.get(i);   // 8

            Node node = getNodesInternal().get(parentIndex-1);

            int leftChild = node.getChildLeftnode();   // Assuming you have these methods
            int rightChild = node.getChildRightnode();

            if (leftChild == childIndex && rightChild != -1) {
                copath.add(rightChild);
            } else if (rightChild == childIndex && leftChild != -1) {
                copath.add(leftChild);
            }
        }

        return copath;
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
