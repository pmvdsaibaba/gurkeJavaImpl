package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TreeAddDkReturn {
    private Map<Integer, byte[]> dataSk;
    private int LeafIndex;

    // this is unclear for now
    private int LeafIntersectionIndex;

    public TreeAddDkReturn(Map<Integer, byte[]> dataSk, int LeafIndex, int LeafIntersectionIndex) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
        this.LeafIntersectionIndex = LeafIntersectionIndex;
    }

    public Map<Integer, byte[]> getAddDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }
    public int getLeafIntersectionIndex() { return LeafIntersectionIndex; }

}
