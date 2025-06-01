package com.myproject.dynamicUBKem;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import com.myproject.Nike.Nike;
import com.myproject.RandomOracle.RandomOracle;
import com.myproject.Tree.TreeGetPathReturn;
import com.myproject.Tree.TreeAddEkReturn;
import com.myproject.Tree.TreeGetNodesReturn;

import java.security.SecureRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


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

        Tree tree = Tree.init(n);

        List<Integer> nodeIndexes = tree.nodes();

        Map<Integer, byte[]> pkMap = new HashMap<>();
        Map<Integer, byte[]> skMap = new HashMap<>();

        for (int j = 0; j < nodeIndexes.size(); j++)
        {
            Nike.KeyPair kp = Nike.gen();

            pkMap.put(j + 1, kp.getEk());
            skMap.put(j + 1, kp.getDk());
        }

        TreeEK ek = tree.setNodes(pkMap);

        List<TreeDk> dkList = new ArrayList<>();

        for (int i = 1; i <= n; i++)
        {
            List<Integer> path = tree.T_path(i);

            Map<Integer, byte[]> pathSkMap = new HashMap<>();

            for (Integer pathNodeIndex : path) {
                pathSkMap.put(pathNodeIndex, skMap.get(pathNodeIndex));
            }

            TreeDk dk = tree.setPath(i, pathSkMap);
            dkList.add(dk);
        }

        this.tree = tree;

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

        TreeGetNodesReturn getnodesreturn = Tree.getNodes(ek); 
        Map<Integer, byte[]> pkMap = getnodesreturn.getDataPk();

        Map<Integer, byte[]> newPkMap = new HashMap<>();

        byte[] kFinal = null;

        for (Map.Entry<Integer, byte[]> entry : pkMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] pkj = entry.getValue();

            byte[] kPrime = Nike.key(sk, pkj);

            RandomOracle.RandomOracleResult hashOutput = RandomOracle.H(c, kPrime, ad);
            byte[] s = hashOutput.getS();
            byte[] kj = hashOutput.getK();

            Nike.KeyPair newKp = Nike.gen(s);
            byte[] newPk = newKp.getEk();

            newPkMap.put(nodeId, newPk);

            if ((kFinal == null) && ( nodeId == 1 )) 
            {
                kFinal = kj;

                // System.out.println("BK.fin SK:");
                // printByteArray(sk);
                // System.out.println("BK.fin PK:");
                // printByteArray(pkj);
                // System.out.println("BK.fin  k prime ");
                // printByteArray(kPrime);
            }
        }

        TreeEK newEk = this.tree.setNodes(newPkMap);

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

        TreeGetPathReturn pathResult = Tree.getPath(dk);
        int i = pathResult.getLeafIndex();
        Map<Integer, byte[]> skMap = pathResult.getDataSk();

        byte[] pk = new byte[c.length - 1];
        System.arraycopy(c, 1, pk, 0, pk.length);

        Map<Integer, byte[]> updatedSkMap = new HashMap<>();
        byte[] kFinal = null;

        for (Map.Entry<Integer, byte[]> entry : skMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] skl = entry.getValue();

            byte[] kPrime = Nike.key(skl, pk);

            RandomOracle.RandomOracleResult hashOutput = RandomOracle.H(c, kPrime, ad);
            byte[] s = hashOutput.getS();
            byte[] kl = hashOutput.getK();

            Nike.KeyPair newKp = Nike.gen(s);
            byte[] newSk = newKp.getDk();

            updatedSkMap.put(nodeId, newSk);

            if ((kFinal == null) && ( nodeId == 1 ))
            {
                kFinal = kl;

                // System.out.println("BK.dec SK:");
                // printByteArray(skl);
                // System.out.println("BK.dec PK:");
                // printByteArray(pk);
                // System.out.println("BK.dec  k prime ");
                // printByteArray(kPrime);
            }

            // kFinal = kl;
        }

        TreeDk newDk = pathResult.getTree().setPath(i, updatedSkMap);

        return new DecResult(newDk, kFinal); // Step 43: Return (dk, k)
    }

    public class BKAddResult {
        public TreeEK ek;
        public TreeDk dk;
        public c_BKAdd c;

        public BKAddResult(TreeEK ek, TreeDk dk, c_BKAdd c) {
            this.ek = ek;
            this.dk = dk;
            this.c = c;
        }
    }

    public class c_BKAdd {
        public byte t;
        public Map<Integer, byte[]> pkstarMap;
        public Map<Integer, byte[]> pk_lMap;

        public c_BKAdd(byte t, Map<Integer, byte[]> pkstarMap, Map<Integer, byte[]> pk_lMap) {
            this.t = t;
            this.pkstarMap = pkstarMap;
            this.pk_lMap = pk_lMap;
        }
    }

    public BKAddResult add(TreeEK ek) throws Exception {

        TreeAddEkReturn addReturn = Tree.T_add_Ek(ek);
        Map<Integer, byte[]> pkMap = addReturn.getDataPk();
        List<Integer> path = addReturn.getPathList(); 
        List<Integer> coPath = addReturn.getCoPathList(); 
        int n = addReturn.getLeafsCount();
        Tree tempTree = addReturn.getTree();

        // pk*
        Map<Integer, byte[]> pkStarMap =  new HashMap<>();

        for (Integer pathNodeIndex : coPath) {
            pkStarMap.put(pathNodeIndex, pkMap.get(pathNodeIndex));
        }

        Nike.KeyPair leafKeyPair = Nike.gen();
        byte[] sk = leafKeyPair.getDk();
        byte[] pk = leafKeyPair.getEk();

        Map<Integer, byte[]> pkNewMap = new HashMap<>();
        Map<Integer, byte[]> skNewMap = new HashMap<>();

        skNewMap.put(path.get(0), sk);
        pkMap.put(path.get(0), pk);

        SecureRandom secureRandom = new SecureRandom();
        byte[] sPrime = new byte[32];
        secureRandom.nextBytes(sPrime);

        for (int l = 0; l < (path.size() - 1);  l++) {
            Nike.KeyPair kp = Nike.gen(sPrime);
            byte[] pk_l = kp.getEk();
            byte[] sk_l = kp.getDk();

            pkNewMap.put(path.get(l), pk_l);

            byte[] k = Nike.key(sk_l, pkMap.get(coPath.get(l)));

            RandomOracle.RandomOracleResult roRes = RandomOracle.H(k, pk_l);
            byte[] s = roRes.getS();
            sPrime = roRes.getK(); // updated s′

            Nike.KeyPair kpPrev = Nike.gen(s);
            byte[] pkPrev = kpPrev.getEk();
            byte[] skPrev = kpPrev.getDk();

            pkMap.put(path.get(l+1), pkPrev);
            skNewMap.put(path.get(l+1), skPrev);
        }

        TreeEK newEk = tempTree.setNodes(pkMap);

        // here n is not the num of leaves but the added latest leaf index
        TreeDk newDk = tempTree.setPath(n, skNewMap);

        byte A = (byte) 'A';
        c_BKAdd c = new c_BKAdd(A, pkStarMap, pkNewMap);

        return new BKAddResult(newEk, newDk, c);
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
