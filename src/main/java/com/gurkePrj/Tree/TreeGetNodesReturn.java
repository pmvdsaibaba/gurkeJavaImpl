package com.gurkePrj.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeGetNodesReturn {
    private Map<Integer, byte[]> dataPk;
    private Tree Tree;

    public TreeGetNodesReturn(Map<Integer, byte[]> dataPk, Tree Tree) {
        this.dataPk = dataPk;
        this.Tree = Tree;
    }

    public Map<Integer, byte[]> getDataPk() { return dataPk; }
    public Tree getTree() { return Tree; }

}
