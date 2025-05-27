package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

import com.myproject.Tree.Tree;

public class TreeDk {

    private List<byte[]> dataSk;
    private Integer leaf;
    private Tree Tree;

    public TreeDk(Tree tree, List<byte[]> dataSk, int leaf) {
        this.dataSk = dataSk;
        this.leaf = leaf;
        this.Tree = tree;
    }

    public List<byte[]> getDataSk() { return dataSk; }
    public int getLeaf() { return leaf; }
    public Tree getTree() { return Tree; }

}
