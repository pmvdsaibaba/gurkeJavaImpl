package com.gurkePrj.Tree;

import com.gurkePrj.Tree.TreeEK;
import com.gurkePrj.Tree.TreeAddEkReturn;
import com.gurkePrj.Tree.TreeAddDkReturn;
import com.gurkePrj.Tree.TreeGetPathReturn;
import com.gurkePrj.Tree.TreeGetNodesReturn;
import com.gurkePrj.Tree.TreeDk;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.*;


public class Tree {
    private int numLeaves;
    private int treeSize;

    // node indexes as 1,2,3,.... N
    // These Indexes should be unique. So once created, the index will be permanently assigned to a node. Even if the node is deleted that index will not be reused
    List<Integer> nodeIndexes;
    // To store the maximum node index value created
    int nodeIndexMax;

    List<Integer> leafIndexes;
    int leafIndexMax;

    // this holds all the nodes
    List<Node> internalNode = new ArrayList<>();

    // variables used in T.Add dk
    private int TAddnode1Index;
    private int TAddnode2Index;
    private int TAddnode2Index_leaf;
    private int TAddnode1Index_leaf;

    // variables used in T.Rem dk
    private int TRemnode1Index;
    private int TRemnode2Index;
    private int TRemnode2Index_leaf;
    private int TRemnode1Index_leaf;
    private int TRem_AffectedLeaf;

 

    public static class Node {
        private int nodeIndex;
        private int rootNode;
        private int childLeftNode;
        private int childRightNode;
        private int nodeLevel;
        private int leafIndex;
        private byte[] pk;
        private byte[] sk;
        private boolean isLeaf;
        private boolean isValidNode;

        // Private constructor — only accessible through the builder
        private Node(Builder builder) {
            this.nodeIndex = builder.nodeIndex;
            this.rootNode = builder.rootNode;
            this.childLeftNode = builder.childLeftNode;
            this.childRightNode = builder.childRightNode;
            this.nodeLevel = builder.nodeLevel;
            this.leafIndex = builder.leafIndex;
            this.pk = builder.pk;
            this.sk = builder.sk;
            this.isLeaf = builder.isLeaf;
            this.isValidNode = builder.isValidNode;
        }

        // Builder class
        public static class Builder {
            private int nodeIndex = -1;
            private int rootNode = -1;
            private int childLeftNode = -1;
            private int childRightNode = -1;
            private int nodeLevel = -1;
            private int leafIndex = -1;
            private byte[] pk = null;
            private byte[] sk = null;
            private boolean isLeaf = false;
            private boolean isValidNode = false;

            public Builder setRootnode(int rootNode) {
                this.rootNode = rootNode;
                return this;
            }

            public Builder setNodeIndex(int nodeIndex) {
                this.nodeIndex = nodeIndex;
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

            public Builder setNodeLevel(int nodeLevel) {
                this.nodeLevel = nodeLevel;
                return this;
            }

            public Builder setLeafIndex(int leafIndex) {
                this.leafIndex = leafIndex;
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

            public Builder setIsValidNode(boolean isValidNode) {
                this.isValidNode = isValidNode;
                return this;
            }

            public Node build() {
                return new Node(this);
            }
        }

        public int getNodeIndex() { return nodeIndex; }
        public int getRootnode() { return rootNode; }
        public int getChildLeftnode() { return childLeftNode; }
        public int getChildRightnode() { return childRightNode; }
        public int getNodeLevel() { return nodeLevel; }
        public int getLeafIndex() { return leafIndex; }
        public byte[] getPk() { return pk; }
        public byte[] getSk() { return sk; }
        public boolean isLeaf() { return isLeaf; }
        public boolean isValidNode() { return isValidNode; }

        public void setNodeIndex(int nodeIndex) {this.nodeIndex = nodeIndex; }
        public void setRootnode(int rootNode) {this.rootNode = rootNode; }
        public void setChildLeftnode(int childLeftNode) {this.childLeftNode = childLeftNode; }
        public void setChildRightnode(int childRightNode) { this.childRightNode = childRightNode; }
        public void setNodeLevel(int nodeLevel) { this.nodeLevel = nodeLevel; }
        public void setLeafIndex(int leafIndex) { this.leafIndex = leafIndex; }
        public void setPk(byte[] pk) { this.pk = pk; }
        public void setSk(byte[] sk) { this.sk = sk;  }
        public void setIsLeaf(boolean isLeaf) {   this.isLeaf = isLeaf;  }
        public void setIsValidNode(boolean isValidNode) { this.isValidNode = isValidNode;  }
    }


    public Tree deepCopy() {
        Tree copy = new Tree();
        copy.numLeaves = this.numLeaves;
        copy.treeSize = this.treeSize;
        copy.nodeIndexMax = this.nodeIndexMax;
        copy.leafIndexMax = this.leafIndexMax;
        copy.TAddnode1Index = this.TAddnode1Index;
        copy.TAddnode2Index = this.TAddnode2Index;
        copy.TAddnode2Index_leaf = this.TAddnode2Index_leaf;
        copy.TAddnode1Index_leaf = this.TAddnode1Index_leaf;
        copy.TRemnode2Index_leaf = this.TRemnode2Index_leaf;
        copy.TRemnode1Index_leaf = this.TRemnode1Index_leaf;
        copy.TRemnode1Index = this.TRemnode1Index;
        copy.TRemnode2Index = this.TRemnode2Index;
        copy.TRem_AffectedLeaf = this.TRem_AffectedLeaf;

        // Deep copy nodeIndexes and leafIndexes
        copy.nodeIndexes = new ArrayList<>(this.nodeIndexes);
        copy.leafIndexes = new ArrayList<>(this.leafIndexes);

        // Deep copy internalNode list
        copy.internalNode = new ArrayList<>();
        for (Node node : this.internalNode) {
            copy.internalNode.add(deepCopyNode(node));
        }

        return copy;
    }

    private static Node deepCopyNode(Node node) {
        return new Node.Builder()
            .setNodeIndex(node.getNodeIndex())
            .setRootnode(node.getRootnode())
            .setChildLeftnode(node.getChildLeftnode())
            .setChildRightnode(node.getChildRightnode())
            .setNodeLevel(node.getNodeLevel())
            .setLeafIndex(node.getLeafIndex())
            .setPk(node.getPk() != null ? Arrays.copyOf(node.getPk(), node.getPk().length) : null)
            .setSk(node.getSk() != null ? Arrays.copyOf(node.getSk(), node.getSk().length) : null)
            .setIsLeaf(node.isLeaf())
            .setIsValidNode(node.isValidNode())
            .build();
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
        int leafCount = 1;

        tree.nodeIndexes = new ArrayList<>();
        tree.leafIndexes = new ArrayList<>();

        for (int i = 1; i < 2 * n; i++) {

            tree.nodeIndexes.add(i);

            Tree.Node node = new Tree.Node.Builder()
                .setNodeIndex(i)
                .build();

            if( i != 1)
            {
                node.setRootnode(i/2);
                node.setNodeLevel((int) (Math.log(i) / Math.log(2)));
            }

            if((2* i) < (2*n))
            {
                node.setChildLeftnode(2*i);
            }
            else
            {
                node.setIsLeaf(true);
                node.setLeafIndex(leafCount);
                tree.leafIndexes.add(leafCount);
                tree.leafIndexMax = leafCount;
                leafCount++;
            }

            if(((2* i) +1) < (2*n))
            {
                node.setChildRightnode((2*i) + 1);
            }
            node.setIsValidNode(true);
            tree.addNode(node);
        }
        tree.nodeIndexMax = (2*n) - 1;

        return tree;
    }

    public int getSize() { return treeSize; }
    public int getNumOfLeaf() { return numLeaves; }
    public int getNodeIndexMax() { return nodeIndexMax; }
    public List<Integer> getNodeIndexes() { return nodeIndexes; }
    public int getLeafIndexMax() { return leafIndexMax; }
    public List<Integer> getLeafIndexes() { return leafIndexes; }


    public void setNumLeaves(int numLeaves) { this.numLeaves = numLeaves; }
    public void setTreeSize(int treeSize) { this.treeSize = treeSize; }
    public void setNodeIndexMax(int nodeIndexMax) { this.nodeIndexMax = nodeIndexMax; }
    public void setLeafIndexMax(int leafIndexMax) { this.leafIndexMax = leafIndexMax; }
    public void setNodeIndexes(List<Integer> nodeIndexes) { this.nodeIndexes = nodeIndexes; }
    public void setLeafIndexes(List<Integer> leafIndexes) { this.leafIndexes = leafIndexes; }
    public void setInternalNode(List<Node> internalNode) { this.internalNode = internalNode; }

    /* T.nodes(τ ) → (tj)j∈[N]  */
    public List<Integer> nodes() {
        List<Integer> nodeIndexesTemp = new ArrayList<>();

        for (int i = 0; i < getNodesInternal().size(); i++) 
        {
            if ( getNodesInternal().get(i).isValidNode() == true)
            {
                nodeIndexesTemp.add(getNodesInternal().get(i).getNodeIndex());
            }
        }
        // nodeIndexesTemp.addAll(this.nodeIndexes);
        return nodeIndexesTemp;
    }

    /* T.getnodes(ek) → (τ, (pkj)j∈[N]) */
    public static TreeGetNodesReturn getNodes(TreeEK ek) {
        return new TreeGetNodesReturn(ek.getDataPk(), ek.getTree());
    }

    /* T.getpath(dk) → (τ, i, (skl)l∈[L]) */
    public static TreeGetPathReturn getPath(TreeDk dk) {
        return new TreeGetPathReturn(dk.getDataSk(), dk.getLeaf(), dk.getTree());
    }

    /* T.setnodes(τ, (pkj)j∈[N]) → ek */
    public TreeEK setNodes(Map<Integer, byte[]> pkMap)
    {
        Tree treeCopy = this.deepCopy();

        for (Node node : treeCopy.getNodesInternal()) {
            int nodeIndex = node.getNodeIndex();
            byte[] pk = pkMap.get(nodeIndex);
            if (pk != null) {
                node.setPk(pk);
            }
        }
        return new TreeEK(treeCopy, pkMap);
    }

    /* T.setpath(τ, i, (skpl )l∈[Li]) → dki */
    public TreeDk setPath(int leaf, Map<Integer, byte[]> skMap)
    {
        Tree treeCopy = this.deepCopy();

        List<Integer> leafPath = treeCopy.T_path(leaf);

        for (Node node : treeCopy.getNodesInternal()) {
            if (leafPath.contains(node.getNodeIndex())) {
                node.setSk(skMap.get(node.getNodeIndex()));
            }
        }

        return new TreeDk(treeCopy, skMap, leaf);
    }

    private static void treeAddInternal(Tree tree){ 

        List<Integer> nodeLevelList = new ArrayList<>();

        // Loop through all internal nodes and collect the level of each leaf node
        for (int i = 0; i < tree.getNodesInternal().size(); i++) {
            Tree.Node node = tree.getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                nodeLevelList.add(node.getNodeLevel());
            }
        }

        // Sort the levels to find the leaf node(s) with the lowest level
        Collections.sort(nodeLevelList);

        // List to store indexes of all leaf nodes that have the minimum level
        List<Integer> nodeIndexList = new ArrayList<>();
        for (int i = 0; i <  tree.getNodesInternal().size(); i++) {
            Tree.Node node =  tree.getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                if (node.getNodeLevel() == nodeLevelList.get(0)) {
                    nodeIndexList.add(node.getNodeIndex());
                }
            }
        }

        // Sort the node indexes to pick the leftmost (smallest index) leaf node with the lowest level
        Collections.sort(nodeIndexList);
        // System.out.print("leftmost (smallest index) leaf node: ");
        // System.out.println(nodeIndexList);

        // Find the position of that node in the internal node list
        int nodeIndex = -1;
        for (int i = 0; i <  tree.getNodesInternal().size(); i++) {
            Tree.Node node =  tree.getNodesInternal().get(i);

            if (node.isLeaf() == true) {
                if (node.getNodeIndex() == nodeIndexList.get(0)) {
                    nodeIndex = i;
                }
            }
        }

        // System.out.print("position of the node: ");
        // System.out.println(nodeIndex);

        // Get the actual leaf node object
        Tree.Node currentLeafNode =  tree.getNodesInternal().get(nodeIndex);

        // Find the position of that node in the internal node list
        int parentNodeIndex = -1;
        for (int i = 0; i <  tree.getNodesInternal().size(); i++) {
            Tree.Node node =  tree.getNodesInternal().get(i);

            if (node.getNodeIndex() == currentLeafNode.getRootnode()) {
                parentNodeIndex = i;
            }
        }

        Tree.Node currentLeafNodeParent =  tree.getNodesInternal().get(parentNodeIndex);


        // Create a new internal node (node1) to replace the current leaf node
        Tree.Node node1 = new Tree.Node.Builder()
            .setNodeIndex(tree.nodeIndexMax + 1)
            .build();

        if (nodeIndex != -1) {
            // Set properties of the new internal node based on the current leaf node
            node1.setRootnode(currentLeafNode.getRootnode());  // Inherit parent
            node1.setNodeLevel(currentLeafNode.getNodeLevel());  // Same level as leaf
            node1.setChildLeftnode(currentLeafNode.getNodeIndex());  // Left child is current leaf
            node1.setChildRightnode(tree.nodeIndexMax + 2);  // Right child will be the new leaf
        }

        node1.setIsValidNode(true);
        tree.addNode(node1);

        // Create a new leaf node (node2) as the right child of node1
        Tree.Node node2 = new Tree.Node.Builder()
            .setNodeIndex(tree.nodeIndexMax + 2)  // New index
            .build();

        node2.setRootnode(tree.nodeIndexMax + 1);  // Its parent is the newly added node1
        node2.setNodeLevel(currentLeafNode.getNodeLevel() + 1);  // Level is increased
        node2.setIsLeaf(true);  // It's a leaf
        tree.leafIndexMax++;
        node2.setLeafIndex(tree.leafIndexMax);
        tree.leafIndexes.add(tree.leafIndexMax);

        // Update the original leaf node to now be an internal node with updated level and parent
        currentLeafNode.setRootnode(tree.nodeIndexMax + 1);  // New parent
        currentLeafNode.setNodeLevel(node2.getNodeLevel());  // Updated level

        if (currentLeafNodeParent.getChildLeftnode() == currentLeafNode.getNodeIndex()) {
            currentLeafNodeParent.setChildLeftnode(node1.getNodeIndex());
        } else if (currentLeafNodeParent.getChildRightnode() == currentLeafNode.getNodeIndex()) {
             currentLeafNodeParent.setChildRightnode(node1.getNodeIndex());
        }

        node2.setIsValidNode(true);
        // Add the new leaf node to the tree
        tree.addNode(node2);

        tree.treeSize++;
        tree.treeSize++;
        tree.numLeaves++;

        tree.TAddnode1Index = node1.getNodeIndex();
        tree.TAddnode2Index = node2.getNodeIndex();
        tree.TAddnode2Index_leaf = node2.getLeafIndex();
        tree.TAddnode1Index_leaf = node1.getLeafIndex();


        tree.nodeIndexes.add(tree.TAddnode1Index);
        tree.nodeIndexes.add(tree.TAddnode2Index);

        tree.nodeIndexMax = tree.TAddnode2Index;
    }

    public static TreeAddEkReturn T_add_Ek(TreeEK ek)
    {
        treeAddInternal(ek.getTree());

        // dk.getDataSk().put(dk.getTree().TAddnode2Index, new byte[0]);
        // dk.getDataSk().put(dk.getTree().TAddnode1Index, new byte[0]);

        return new TreeAddEkReturn(
            ek.getDataPk(), 
            ek.getTree().T_path(ek.getTree().leafIndexMax),
            ek.getTree().T_co_path(ek.getTree().leafIndexMax),
            ek.getTree().numLeaves,
            ek.getTree());
    }

    public static TreeAddDkReturn T_add_dk(TreeDk dk)
    {

        treeAddInternal(dk.getTree());

        List<Integer> set2 = new ArrayList<>(dk.getDataSk().keySet());

        /////////////////////////////////////////////////////////////////
        // To find the intersection


        // List<Integer> set2 = dk.getTree().T_path(dk.getTree().TAddnode1Index_leaf);

        //////////////////////////////////////////////////////////////////
        // Finding the leaf in input node.
        int leafNodeIndex = findLeafIndexFromSet(set2, dk.getTree());


        List<Integer> set3 = dk.getTree().T_path(leafNodeIndex);


        // check if TAddnode2Index is leaf node or added node
        List<Integer> set1 = dk.getTree().T_path(dk.getTree().TAddnode2Index_leaf);

        int IntersectionNode = findFirstCommonNodeInOrder(set1, set3);

        if (IntersectionNode != -1) {
            // do nothing
        } else {
            System.out.println("Error T_add_dk: No common node found.");
        }


        //////////////////////////////////////////////////////////////////
        // wrong assumption if new nodes to be added

        // dk.getDataSk().put(dk.getTree().TAddnode2Index, new byte[0]);
        // dk.getDataSk().put(dk.getTree().TAddnode1Index, new byte[0]);


        return new TreeAddDkReturn(
            dk.getDataSk(),
            leafNodeIndex,
            IntersectionNode,
            dk.getTree());
    }

    private static void treeRemInternal(Tree tree, int leaf){

        Tree.Node NodeToBeRemoved = null;
        Tree.Node NodeRootToBeRemoved = null;
        Tree.Node NewNodeRoot = null;
        Tree.Node tempNode;

        List<Integer> pathList = tree.T_path(leaf);
        List<Integer> coPathList = tree.T_co_path(leaf);

        // tree.TRem_AffectedLeaf = coPathList.get(0);
        tree.TRem_AffectedLeaf = -1;
        int affectedLeaf_nodeIndex = coPathList.get(0);

        for (Node node : tree.getNodesInternal()) {
            if (node.getNodeIndex() == affectedLeaf_nodeIndex) {
                tree.TRem_AffectedLeaf = node.getLeafIndex();
                break;
            }
        }

        if(pathList.size() > 2)
        {
            // Todo: add an implementation to throw an exception if pathList size is < 4
            int LeafNodeIndex = pathList.get(0);
            int LeafRootNodeIndex = pathList.get(1);
            int newLeafRootNodeIndex = pathList.get(2);

            // System.out.print("LeafNodeIndex: ");
            // System.out.println(LeafNodeIndex);

            for (int i = 0; i < tree.getNodesInternal().size(); i++) {

                tempNode = tree.getNodesInternal().get(i);

                if (tempNode.getNodeIndex() == LeafNodeIndex) {
                    NodeToBeRemoved = tree.getNodesInternal().get(i);
                }

                if (tempNode.getNodeIndex() == LeafRootNodeIndex) {
                    NodeRootToBeRemoved = tree.getNodesInternal().get(i);
                }

                if (tempNode.getNodeIndex() == newLeafRootNodeIndex) {
                    NewNodeRoot = tree.getNodesInternal().get(i);
                }
            }

            if (NewNodeRoot.getChildLeftnode() == NodeToBeRemoved.getRootnode())
            {
                if (NodeRootToBeRemoved.getChildLeftnode() == NodeToBeRemoved.getNodeIndex())
                {
                    NewNodeRoot.setChildLeftnode(NodeRootToBeRemoved.getChildRightnode());

                    for (int i = 0; i < tree.getNodesInternal().size(); i++) 
                    {
                        tempNode = tree.getNodesInternal().get(i);
                        if (tempNode.getNodeIndex() == NodeRootToBeRemoved.getChildRightnode()) 
                        {
                            tempNode.setRootnode(NewNodeRoot.getNodeIndex());
                            tempNode.setNodeLevel(tempNode.getNodeLevel()-1);
                            tree.TRemnode2Index = tempNode.getNodeIndex();
                        }
                    }
                }
                else if (NodeRootToBeRemoved.getChildRightnode() == NodeToBeRemoved.getNodeIndex())
                {
                    NewNodeRoot.setChildLeftnode(NodeRootToBeRemoved.getChildLeftnode());

                    for (int i = 0; i < tree.getNodesInternal().size(); i++) 
                    {
                        tempNode = tree.getNodesInternal().get(i);
                        if (tempNode.getNodeIndex() == NodeRootToBeRemoved.getChildLeftnode()) 
                        {
                            tempNode.setRootnode(NewNodeRoot.getNodeIndex());
                            tempNode.setNodeLevel(tempNode.getNodeLevel()-1);
                            tree.TRemnode2Index = tempNode.getNodeIndex();
                        }
                    }
                }
            } else if (NewNodeRoot.getChildRightnode() == NodeToBeRemoved.getRootnode())
            {
                if (NodeRootToBeRemoved.getChildLeftnode() == NodeToBeRemoved.getNodeIndex())
                {
                    NewNodeRoot.setChildRightnode(NodeRootToBeRemoved.getChildRightnode());

                    for (int i = 0; i < tree.getNodesInternal().size(); i++) 
                    {
                        tempNode = tree.getNodesInternal().get(i);
                        if (tempNode.getNodeIndex() == NodeRootToBeRemoved.getChildRightnode()) 
                        {
                            tempNode.setRootnode(NewNodeRoot.getNodeIndex());
                            tempNode.setNodeLevel(tempNode.getNodeLevel()-1);
                            tree.TRemnode2Index = tempNode.getNodeIndex();
                        }
                    }
                }
                else if (NodeRootToBeRemoved.getChildRightnode() == NodeToBeRemoved.getNodeIndex())
                {
                    NewNodeRoot.setChildRightnode(NodeRootToBeRemoved.getChildLeftnode());

                    for (int i = 0; i < tree.getNodesInternal().size(); i++) 
                    {
                        tempNode = tree.getNodesInternal().get(i);
                        if (tempNode.getNodeIndex() == NodeRootToBeRemoved.getChildLeftnode()) 
                        {
                            tempNode.setRootnode(NewNodeRoot.getNodeIndex());
                            tempNode.setNodeLevel(tempNode.getNodeLevel()-1);
                            tree.TRemnode2Index = tempNode.getNodeIndex();
                        }
                    }
                }

            }

            NodeToBeRemoved.setRootnode(-1);
            NodeToBeRemoved.setNodeLevel(-1);
            NodeToBeRemoved.setLeafIndex(-1);
            NodeToBeRemoved.setIsLeaf(false);
            NodeToBeRemoved.setIsValidNode(false);
            NodeToBeRemoved.setPk(null);
            NodeToBeRemoved.setSk(null);

            NodeRootToBeRemoved.setRootnode(-1);
            NodeRootToBeRemoved.setNodeLevel(-1);
            NodeRootToBeRemoved.setLeafIndex(-1);
            NodeRootToBeRemoved.setChildRightnode(-1);
            NodeRootToBeRemoved.setChildLeftnode(-1);
            NodeRootToBeRemoved.setIsLeaf(false);
            NodeRootToBeRemoved.setIsValidNode(false);
            NodeRootToBeRemoved.setPk(null);
            NodeRootToBeRemoved.setSk(null);

            for (Node node : tree.getNodesInternal()) {
                List<Integer> path = tree.getPathToRoot(node.getNodeIndex());

                // Level is the path length minus 1, or -1 if root
                int level = (path.size() > 0 && path.get(path.size() - 1) == node.getNodeIndex()) ? -1 : path.size() - 1;
                node.setNodeLevel(level);
            }

            tree.numLeaves--;
            tree.treeSize--;
            tree.treeSize--;

            tree.TRemnode1Index = NewNodeRoot.getNodeIndex();
        }

    }

    public List<Integer> getPathToRoot(int nodeIndex) {
        List<Integer> path = new ArrayList<>();
        int current = nodeIndex;

        while (current != -1) {
            Node currentNode = null;
            for (Node node : getNodesInternal()) {
                if (node.getNodeIndex() == current) {
                    currentNode = node;
                    break;
                }
            }
            if (currentNode == null) break;

            path.add(current);
            current = currentNode.getRootnode();
        }

        return path;
    }


    public static TreeAddEkReturn T_rem_Ek(TreeEK ek, int leaf)
    {
        treeRemInternal(ek.getTree(), leaf);

        return new TreeAddEkReturn(
            ek.getDataPk(), 
            ek.getTree().T_path(ek.getTree().TRem_AffectedLeaf),
            ek.getTree().T_co_path(ek.getTree().TRem_AffectedLeaf),
            ek.getTree().numLeaves, // check this: seems something wrong: this is not needed for remove
            ek.getTree() );
    }

    public static TreeAddDkReturn T_rem_Dk(TreeDk dk, int leaf)
    {
//****************************************************
// Bug:
// 
// If Intersection node is root node then this node removing is not working
// Yet to fix this.
// 
//  
// ************************************************************/

        /////////////////////////////////////////////////////////////////
        // To find the intersection

        List<Integer> set1 = dk.getTree().T_path(leaf);
        List<Integer> set2 = new ArrayList<>(dk.getDataSk().keySet());

        // int leafNodeIndex = findLeafIndexFromSet(set2, dk.getTree());
        // List<Integer> set3 = dk.getTree().T_path(leafNodeIndex);

        int IntersectionNode = findFirstCommonNodeInOrder(set1, set2);

        treeRemInternal(dk.getTree(), leaf);

        if (IntersectionNode != -1) {
            // do nothing
        } else {
            System.out.println("Error T_rem_Dk: No common node found.");
        }


        //////////////////////////////////////////////////////////////////
        // Finding the leaf in input node.

        int leafNodeIndex = findLeafIndexFromSet(set2, dk.getTree());

        return new TreeAddDkReturn(
            dk.getDataSk(), 
            leafNodeIndex,
            IntersectionNode,
            dk.getTree());
    }

    public List<Integer> T_path( int Leaf_i) {
        int tempNodeIndex;
        int currentNodeIndex = -1;
        List<Integer> returnlist = new ArrayList<>();

        for (int i = 0; i < getNodesInternal().size(); i++)
        {
            Tree.Node node = getNodesInternal().get(i);
            if (node.isLeaf() == true)
            {
                if(node.getLeafIndex() == Leaf_i)
                {
                    tempNodeIndex = node.getNodeIndex();
                    currentNodeIndex = tempNodeIndex;
                }
            }
        }

        while (currentNodeIndex != -1)
        {
            Node currentNode = null;

            for (Node node : getNodesInternal()) {
                if (node.getNodeIndex() == currentNodeIndex) {
                    currentNode = node;
                    break;
                }
            }

            if (currentNode == null)
            {
                break;
            }
            returnlist.add(currentNodeIndex);
            currentNodeIndex = currentNode.getRootnode();
        }
        return returnlist;
    }


    public List<Integer> T_co_path( int Leaf_i) {
        List<Integer> copath = new ArrayList<>();

        List<Integer> path  = T_path(Leaf_i);

        if (path.size() < 2) return copath; // no co-path if only one node

        // Start from 2nd node in path (index 1), because first is the leaf
        for (int i = 1; i < path.size(); i++) {
            int childIndex = path.get(i - 1);
            int parentIndex = path.get(i);

            Node node = getNodesInternal().get(parentIndex-1);

            int leftChild = node.getChildLeftnode();
            int rightChild = node.getChildRightnode();

            if (leftChild == childIndex && rightChild != -1) {
                copath.add(rightChild);
            } else if (rightChild == childIndex && leftChild != -1) {
                copath.add(leftChild);
            }
        }

        return copath;
    }

    // set 1 should be proper like the path order.
    // set 2 can be just indexes
    public static int findFirstCommonNodeInOrder(List<Integer> set1, List<Integer> set2) {

        Set<Integer> set2Lookup = new HashSet<>(set2);

        // Iterate over set1 (which is in leaf to root order)
        for (int node : set1) {
            if (set2Lookup.contains(node)) {
                return node; // Return the first node in set1 that is also in set2
            }
        }

        // If no common node found
        return -1;
    }

    // public static void main(String[] args) {
    //     List<Integer> set1 = Arrays.asList(13, 7, 2, 3, 4, 1);
    //     List<Integer> set2 = Arrays.asList(3,2,17,10);

    //     Integer result = findFirstCommonNodeInOrder(set1, set2);

    //     if (result != null) {
    //         System.out.println("First common node in set1 order: " + result);
    //     } else {
    //         System.out.println("No common node found.");
    //     }
    // }

    public static int findLeafIndexFromSet(List<Integer> nodeIndexes, Tree tree)
    {
        for (Node node : tree.getNodesInternal()) {
            if (nodeIndexes.contains(node.getNodeIndex()) && node.isLeaf() && node.isValidNode()) {
                return node.getLeafIndex();
            }
        }
        return -1; // not found
    }

}


