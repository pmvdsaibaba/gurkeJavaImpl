package com.gurkePrj.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gurkePrj.Tree.Tree;

public class TreeEK {

    private Map<Integer, byte[]> dataPk;
    private Tree Tree;

    public TreeEK(Tree tree, Map<Integer, byte[]> dataPk) {
        this.dataPk = dataPk;
        this.Tree = tree;
    }

    public Map<Integer, byte[]> getDataPk() { return dataPk; }
    public Tree getTree() { return Tree; }
}
