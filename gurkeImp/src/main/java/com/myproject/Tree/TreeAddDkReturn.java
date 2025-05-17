package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeAddDkReturn {
    private List<byte[]> dataSk;
    private int LeafIndex;
    private int LeafIntersectionIndex;

    public TreeAddDkReturn(List<byte[]> dataSk, int LeafIndex, int LeafIntersectionIndex) {
        this.dataSk = dataSk;
        this.LeafIndex = LeafIndex;
        this.LeafIntersectionIndex = LeafIntersectionIndex;
    }

    public List<byte[]> getAddDataSk() { return dataSk; }
    public int getLeafIndex() { return LeafIndex; }
    public int getLeafIntersectionIndex() { return LeafIntersectionIndex; }

}
