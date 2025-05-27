package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

import com.myproject.Tree.Tree;

public class TreeGetPathReturn {
    private List<byte[]> dataSk;
    private int LeafIndex;
    private Tree Tree;

    public TreeGetPathReturn(List<byte[]> dataSk, int LeafIndex, Tree Tree) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
        this.Tree = Tree;
    }

    public List<byte[]> getDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }
    public Tree getTree() { return Tree; }

}
