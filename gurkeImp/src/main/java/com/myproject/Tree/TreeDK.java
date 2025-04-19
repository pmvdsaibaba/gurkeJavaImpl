package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

// import com.myproject.Tree.Tree;

public class TreeDK {
    // A UBKEM decapsulation key
    private List<byte[]> dataSk;
    private Integer leaf;

    public TreeDK() {
        // if (dkList != null) {
        //     this.dataSk = dkList;
        // } else {
        //     this.dataSk = new ArrayList<>();
        //     for (int i = 0; i < initialDepth; i++) {
        //         this.dataSk.add(null);
        //     }
        // }
        // this.leaf = leaf;
    }

    public void setDataSk(List<byte[]> dataSk) {
        this.dataSk = dataSk;
    }

    public void setLeaf(int leaf) {
        this.leaf = leaf;
    }

    // Helper class to hold the result
    public static class DkData {
        public List<byte[]> dataSk;
        public Integer leaf;

        public DkData(List<byte[]> dataSk, Integer leaf) {
            this.dataSk = dataSk;
            this.leaf = leaf;
        }

        public List<byte[]> getDataSk() {
            return dataSk;
        }
        public Integer getDataLeaf() {
            return leaf;
        }
    }

    public DkData getDkPath() {
        return new DkData(this.dataSk, this.leaf);
    }


    // public Object pop(int index) {
    //     return dataSk.remove(index);
    // }

    // public Object get(int index) {
    //     return dataSk.get(index);
    // }

    // public void set(int index, Object value) {
    //     dataSk.set(index, value);
    // }

    public int size() {
        return dataSk.size();
    }

    // public void append(Object item) {
    //     dataSk.add(item);
    // }

    public List<Integer> path() {
        return Pathable.path(this.leaf);
    }
}

// class Pathable {
//     public static List<Integer> path(int leaf) {
//         return Path(leaf);
//     }

//     public static List<Integer> copath(int leaf) {
//         return Copath(leaf);
//     }

//     // Static methods for path and copath logic
//     public static List<Integer> Path(int leaf) {
//         List<Integer> path = new ArrayList<>();
//         int a = leaf + 1;
//         while (a != 1) {
//             path.add(a - 1);
//             a /= 2;
//         }
//         path.add(0);
//         path.sort((x, y) -> y - x); // Reverse the list
//         return path;
//     }

//     public static List<Integer> Copath(int leaf) {
//         List<Integer> path = Path(leaf);
//         List<Integer> copath = new ArrayList<>();
//         for (int i = 1; i < path.size(); i++) {
//             int p = path.get(i);
//             int cop = (p % 2 != 0) ? p + 1 : p - 1;
//             copath.add(cop);
//         }
//         return copath;
//     }
// }