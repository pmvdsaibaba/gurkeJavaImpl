package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

import com.myproject.Tree.Tree;

public class TreeEK {

    private List<byte[]> dataPk;
    private Tree Tree;

    public TreeEK(Tree tree, List<byte[]> dataPk) {
        this.dataPk = dataPk;
        this.Tree = tree;
    }

    public List<byte[]> getDataPk() { return dataPk; }
    public Tree getTree() { return Tree; }
}
