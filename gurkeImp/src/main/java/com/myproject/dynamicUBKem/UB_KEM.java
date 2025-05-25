package com.myproject.dynamicUBKem;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;
import com.myproject.RandomOracle.RandomOracle;
import com.myproject.Tree.TreeGetPathReturn;

import java.util.ArrayList;
import java.util.List;

public class UB_KEM {

    Tree tree;

    public class BKGenResult {
        public TreeEK ek;
        public List<TreeDk> dkList;

        public BKGenResult(TreeEK ek, List<TreeDk> dkList) {
            this.ek = ek;
            this.dkList = dkList;
        }
    }

    public BKGenResult gen(int n) throws Exception {
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

        this.tree = tree;

        // Step 08: Return result
        return new BKGenResult(ek, dkList);
    }


    public class BKEncResult {
        public EncOutput u;
        public byte[] c;

        public BKEncResult(EncOutput u, byte[] c) {
            this.u = u;
            this.c = c;
        }
    }

    public class EncOutput {
        public TreeEK ek;
        public byte[] sk;
        public byte[] c;

        public EncOutput(TreeEK ek, byte[] sk, byte[] c) {
            this.ek = ek;
            this.sk = sk;
            this.c = c;
        }
    }

    public BKEncResult enc(TreeEK ek) throws Exception {

        byte E = (byte) 'E';  // 69

        Nike.KeyPair kp = Nike.gen();

        byte[] pk = kp.getEk();
        byte[] sk = kp.getDk();

        // c ← (E, pk)
        byte[] c = prependByte(E, pk);

        EncOutput u = new EncOutput(ek, sk, c);

        return new BKEncResult(u, c);
    }

    public byte[] prependByte(byte prefix, byte[] original) {
        byte[] result = new byte[original.length + 1];
        result[0] = prefix; // Set F at the beginning
        System.arraycopy(original, 0, result, 1, original.length);
        return result;
    }

    public class FinResult {
        public TreeEK ek;
        public byte[] k; // Final derived key

        public FinResult(TreeEK ek, byte[] k) {
            this.ek = ek;
            this.k = k;
        }
    }

    public FinResult fin(EncOutput u, byte[] ad) throws Exception {
        TreeEK ek = u.ek;
        byte[] sk = u.sk;
        byte[] c = u.c;

        List<byte[]> pkList = this.tree.T_getNodes(ek); 

        List<byte[]> newPkList = new ArrayList<>();

        byte[] kFinal = null;

        for (byte[] pkj : pkList) {

            byte[] kPrime = Nike.key(sk, pkj);



            RandomOracle.RandomOracleResult hashOutput = RandomOracle.H(c, kPrime, ad);
            byte[] s = hashOutput.getS();
            byte[] kj = hashOutput.getK();

            Nike.KeyPair newKp = Nike.gen(s);
            byte[] newPk = newKp.getEk();
            newPkList.add(newPk);

            if (kFinal == null) {
                kFinal = kj; // Use k1 as the final key 

                // System.out.println("BK.fin SK:");
                // printByteArray(sk);
                // System.out.println("BK.fin PK:");
                // printByteArray(pkj);
                // System.out.println("BK.fin  k prime ");
                // printByteArray(kPrime);
            }
        }

        TreeEK newEk = this.tree.setNodes(newPkList);

        return new FinResult(newEk, kFinal); // Step 21
    }

    public class DecResult {
        public TreeDk dk;
        public byte[] k;

        public DecResult(TreeDk dk, byte[] k) {
            this.dk = dk;
            this.k = k;
        }
    }

    public DecResult dec(TreeDk dk, byte[] ad, byte[] c) throws Exception {

        TreeGetPathReturn pathResult = this.tree.getPath(dk);
        int i = pathResult.getLeafIndex();
        List<byte[]> skList = pathResult.getDataSk();

        byte[] pk = new byte[c.length - 1];
        System.arraycopy(c, 1, pk, 0, pk.length);

        List<byte[]> updatedSkList = new ArrayList<>();
        byte[] kFinal = null;

        for (byte[] skl : skList) {
            byte[] kPrime = Nike.key(skl, pk);




            RandomOracle.RandomOracleResult hashOutput = RandomOracle.H(c, kPrime, ad);
            byte[] s = hashOutput.getS();
            byte[] kl = hashOutput.getK();

            Nike.KeyPair newKp = Nike.gen(s);
            byte[] newSk = newKp.getDk();
            updatedSkList.add(newSk);

            if (kFinal == null) {
                kFinal = kl; // Use k1 as the final key

                // System.out.println("BK.dec SK:");
                // printByteArray(skl);
                // System.out.println("BK.dec PK:");
                // printByteArray(pk);
                // System.out.println("BK.dec  k prime ");
                // printByteArray(kPrime);
            }

            kFinal = kl;
        }

        TreeDk newDk = this.tree.setPath(i, updatedSkList);

        return new DecResult(newDk, kFinal); // Step 43: Return (dk, k)
    }
    // Utility to print byte arrays
    private void printByteArray(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }
}
