package com.myproject.dynamicUBKem;

import java.util.ArrayList;
import java.util.List;

import com.myproject.Tree.Tree;
import com.myproject.Nike.Nike;

public class DynBKGen {

    public static void gen(int n) throws Exception {

        Tree Tree1 = Tree.init(n);

        List<Integer> nodes = Tree1.nodes(); 

        List<byte[]> PkList = new ArrayList<>();
        List<byte[]> skList = new ArrayList<>();

        for (int i = 1; i <= nodes.size(); i++) {
            Nike.KeyPair nikeGenKeyPair = Nike.gen();
            PkList.add(nikeGenKeyPair.getEk());
            skList.add(nikeGenKeyPair.getDk());
        }


    }
}
