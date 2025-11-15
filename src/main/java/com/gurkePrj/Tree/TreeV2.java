package com.gurkePrj.Tree;

import com.gurkePrj.Tree.TreeEK;
import java.util.ArrayList;
import java.util.List;

public class TreeV2 {
    private int numLeaves;
    private int size;
    private TreeEK treeEk;

    // Hold the single instance here
    private static TreeV2 instance;

    // Private constructor so no one else can create instances
    private TreeV2() {}

    // Public static method to initialize and access the instance
    public static void init(int n) {
        if (instance == null) {
            instance = new TreeV2();
            instance.numLeaves = n;
            instance.size = 2 * n - 1;
        }
    }

    public static TreeV2 getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Tree is not initialized. Call Tree.init(n) first.");
        }
        return instance;
    }

    public int getNumLeaves() {
        return numLeaves;
    }
}
