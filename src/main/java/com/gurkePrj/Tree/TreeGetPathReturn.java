package com.gurkePrj.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gurkePrj.Tree.Tree;

public class TreeGetPathReturn {
    private Map<Integer, byte[]> dataSk;
    private int LeafIndex;
    private Tree Tree;

    public TreeGetPathReturn(Map<Integer, byte[]> dataSk, int LeafIndex, Tree Tree) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
        this.Tree = Tree;
    }

    public Map<Integer, byte[]> getDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }
    public Tree getTree() { return Tree; }

}
