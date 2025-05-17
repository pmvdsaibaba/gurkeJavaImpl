package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeDk {

    private List<byte[]> dataSk;
    private Integer leaf;

    public TreeDk(List<byte[]> dataSk, int leaf) {
        this.dataSk = dataSk;
        this.leaf = leaf;
    }

    public List<byte[]> getDataSk() { return dataSk; }
    public int getLeaf() { return leaf; }

}
