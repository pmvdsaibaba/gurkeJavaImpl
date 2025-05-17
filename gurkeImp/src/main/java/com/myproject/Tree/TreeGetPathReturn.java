package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeGetPathReturn {
    private List<byte[]> dataSk;
    private int LeafIndex;

    public TreeGetPathReturn(List<byte[]> dataSk, int LeafIndex) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
    }

    public List<byte[]> getDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }

}
