package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

public class TreeEK {

    private List<byte[]> dataPk;

    public TreeEK(List<byte[]> dataPk) {
        this.dataPk = dataPk;
    }

    public List<byte[]> getDataPk() { return dataPk; }
}
