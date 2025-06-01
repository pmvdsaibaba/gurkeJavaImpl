package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeAddEkReturn {
    private List<byte[]> dataPk;
    private List<Integer> pathList;
    private List<Integer> coPathList;
    private int leafsCount;
    private Tree Tree;

    public TreeAddEkReturn(List<byte[]> dataPk, List<Integer> pathList, List<Integer> coPathList, int leafsCount, Tree Tree) {
        this.dataPk = dataPk;
        this.pathList = pathList;
        this.coPathList = coPathList;
        this.leafsCount = leafsCount;
        this.Tree = Tree;
    }

    public List<byte[]> getDataPk() { return dataPk; }
    public List<Integer> getPathList() { return pathList; }
    public List<Integer> getCoPathList() { return coPathList; }
    public int getLeafsCount() { return leafsCount; }
    public Tree getTree() { return Tree; }
}
