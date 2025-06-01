package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.myproject.Tree.Tree;

public class TreeDk {

    private Map<Integer, byte[]> dataSk;
    private Integer leaf;
    private Tree Tree;

    public TreeDk(Tree tree, Map<Integer, byte[]> dataSk, int leaf) {
        this.dataSk = dataSk;
        this.leaf = leaf;
        this.Tree = tree;
    }

    public Map<Integer, byte[]> getDataSk() { return dataSk; }
    public int getLeaf() { return leaf; }
    public Tree getTree() { return Tree; }

}
