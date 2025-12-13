package com.gurkePrj.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TreeAddDkReturn {
    private Map<Integer, byte[]> dataSk;
    private int LeafIndex;

    private int LeafIntersectionIndex;
    private Tree Tree;

    public TreeAddDkReturn(Map<Integer, byte[]> dataSk, int LeafIndex, int LeafIntersectionIndex, Tree Tree) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
        this.LeafIntersectionIndex = LeafIntersectionIndex;
        this.Tree = Tree;
    }

    public Map<Integer, byte[]> getDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }
    public int getLeafIntersectionIndex() { return LeafIntersectionIndex; }

    public Tree getTree() { return Tree; }
}
