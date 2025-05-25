package com.myproject.dynamicUBKem;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;

import java.util.ArrayList;
import java.util.List;

public class UB_KEM {

    public static class BKGenResult {
        public TreeEK ek;
        public List<TreeDk> dkList;

        public BKGenResult(TreeEK ek, List<TreeDk> dkList) {
            this.ek = ek;
            this.dkList = dkList;
        }
    }

    public static BKGenResult gen(int n) throws Exception {
        // Step 00
        Tree tree = Tree.init(n);

        // Step 01
        List<Integer> nodeIndexes = tree.nodes();

        // Step 02–03: Generate (pkj, skj) for all nodes
        List<byte[]> pkList = new ArrayList<>();
        List<byte[]> skList = new ArrayList<>();
        for (int j = 0; j < nodeIndexes.size(); j++) {
            Nike.KeyPair kp = Nike.gen();
            pkList.add(kp.getEk());
            skList.add(kp.getDk());
        }

        // Step 04: Set the pk's in the tree
        TreeEK ek = tree.setNodes(pkList);

        // Step 05–07: For each leaf i ∈ [1..n], get its path and set corresponding sks
        List<TreeDk> dkList = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            List<Integer> path = tree.T_path(i); // This returns node indexes in the path

            // Collect secret keys for path nodes
            List<byte[]> pathSkList = new ArrayList<>();
            for (Integer pathNodeIndex : path) {
                int index = nodeIndexes.indexOf(pathNodeIndex);
                pathSkList.add(skList.get(index));
            }

            // Set SKs in tree and collect the Dk object
            TreeDk dk = tree.setPath(i, pathSkList);
            dkList.add(dk);
        }

        // Step 08: Return result
        return new BKGenResult(ek, dkList);
    }
}
