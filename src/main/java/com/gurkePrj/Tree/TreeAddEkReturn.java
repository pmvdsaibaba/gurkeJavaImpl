package com.gurkePrj.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TreeAddEkReturn {
    private Map<Integer, byte[]> dataPk;
    private List<Integer> pathList;
    private List<Integer> coPathList;
    private int leafsCount;
    private Tree Tree;

    public TreeAddEkReturn(Map<Integer, byte[]> dataPk, List<Integer> pathList, List<Integer> coPathList, int leafsCount, Tree Tree) {
        this.dataPk = dataPk;
        this.pathList = pathList;
        this.coPathList = coPathList;
        this.leafsCount = leafsCount;
        this.Tree = Tree;
    }

    public Map<Integer, byte[]> getDataPk() { return dataPk; }
    public List<Integer> getPathList() { return pathList; }
    public List<Integer> getCoPathList() { return coPathList; }
    public int getLeafsCount() { return leafsCount; }
    public Tree getTree() { return Tree; }
}
