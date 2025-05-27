package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeGetNodesReturn {
    private List<byte[]> dataPk;
    private Tree Tree;

    public TreeGetNodesReturn(List<byte[]> dataPk, Tree Tree) {
        this.dataPk = dataPk;
        this.Tree = Tree;
    }

    public List<byte[]> getDataPk() { return dataPk; }
    public Tree getTree() { return Tree; }

}
