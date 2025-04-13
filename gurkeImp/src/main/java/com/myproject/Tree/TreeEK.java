package com.myproject.Tree;

import java.util.ArrayList;
import java.util.List;

// import com.myproject.Tree.Tree;

public class TreeEK {
    private int depth;
    private List<byte[]> dataPk;

    public TreeEK() {
        this.depth = 3;
        // this.dataPk = new ArrayList<>();
        // for (int i = 0; i < (int) Math.pow(2, depth) - 1; i++) {
        //     this.dataPk.add(null);
        // }
    }

    public void setDataPk(List<byte[]> dataPk) {
        this.dataPk = dataPk;
    }

    public List<byte[]> getDataPk() {
        return dataPk;
    }

    public Object get(int index) {
        return dataPk.get(index);
    }

    // public void set(int index, Object value) {
    //     if (index >= this.size()) {
    //         // Expanding the list as needed
    //         int newDepth = (int) Math.log(index + 1) + 1;
    //         for (int i = this.size(); i < (int) Math.pow(2, newDepth) - 1; i++) {
    //             dataPk.add(null);
    //         }
    //         this.depth = newDepth;
    //     }
    //     dataPk.set(index, value);
    // }

    public int size() {
        return dataPk.size();
    }

    public List<Integer> path(int i) {
        return Pathable.Path(i);
    }

    public List<Integer> copath(int i) {
        return Pathable.Copath(i);
    }

    public int getDepth() {
        return depth;
    }

    public int getSize() {
        return (int) Math.pow(2, depth) - 1;
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