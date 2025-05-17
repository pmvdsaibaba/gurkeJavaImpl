package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeAddEkReturn {
    private List<byte[]> dataPk;
    private List<Integer> pathList;
    private List<Integer> coPathList;
    private int leafsCount;

    public TreeAddEkReturn(List<byte[]> dataPk, List<Integer> pathList, List<Integer> coPathList, int leafsCount) {
        this.dataPk = dataPk;
        this.pathList = pathList;
        this.coPathList = coPathList;
        this.leafsCount = leafsCount;
    }

    public List<byte[]> getDataPk() { return dataPk; }
    public List<Integer> getPathList() { return pathList; }
    public List<Integer> getCoPathList() { return coPathList; }
    public int getLeafsCount() { return leafsCount; }

}
