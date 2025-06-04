package com.myproject.dynamicUBKem;

import com.myproject.Tree.Tree;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeAddDkReturn;
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


        // In tree add and rmv. these pk and sk map should be updated.
        // to do: may be get the tree with valid nodes.
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

    public class BKRemoveResult {
        public TreeEK ek;
        public c_BKRemove c;

        public BKRemoveResult(TreeEK ek, c_BKRemove c) {
            this.ek = ek;
            this.c = c;
        }
    }

    public class c_BKRemove {
        public byte t;
        public int i;
        public Map<Integer, byte[]> pkStarMap;
        public byte[] pkCircle;
        public Map<Integer, byte[]> pkPrimeMap;

        public c_BKRemove(byte t, int i, Map<Integer, byte[]> pkStarMap, byte[] pkCircle, Map<Integer, byte[]> pkPrimeMap) {
            this.t = t;
            this.i = i;
            this.pkStarMap = pkStarMap;
            this.pkCircle = pkCircle;
            this.pkPrimeMap = pkPrimeMap;
        }
    }

    public BKRemoveResult rmv(TreeEK ek, int i) throws Exception
    {
        TreeAddEkReturn remReturn = Tree.T_rem_Ek(ek, i);

        Map<Integer, byte[]> pkMap = remReturn.getDataPk();
        List<Integer> path = remReturn.getPathList();
        List<Integer> coPath = remReturn.getCoPathList();     // (cpl)
        Tree tempTree = remReturn.getTree();

        Map<Integer, byte[]> pkStarMap = new HashMap<>();

        for (Integer coPathNode : coPath) {
            pkStarMap.put(coPathNode, pkMap.get(coPathNode));
        }

        Nike.KeyPair kpCircle = Nike.gen();
        byte[] pkCircle = kpCircle.getEk();
        byte[] skCircle = kpCircle.getDk();

        byte[] pkpL = pkMap.get(path.get(0));
        byte[] k = Nike.key(skCircle, pkpL);

        RandomOracle.RandomOracleResult ro1 = RandomOracle.H(k, pkCircle);
        byte[] s = ro1.getS();
        byte[] sPrime = ro1.getK();

        Nike.KeyPair kpPL = Nike.gen(s);
        byte[] newPkpL = kpPL.getEk();
        pkMap.put(path.get(0), newPkpL);

        Map<Integer, byte[]> pkPrimeMap = new HashMap<>();

        for (int l = 0; l < (path.size() - 1);  l++) 
        {
            Nike.KeyPair kpPrimeL = Nike.gen(sPrime);
            byte[] pkPrimeL = kpPrimeL.getEk();
            byte[] skPrimeL = kpPrimeL.getDk();
            pkPrimeMap.put(path.get(l), pkPrimeL);

            byte[] pkCoPath = pkMap.get(coPath.get(l));
            k = Nike.key(skPrimeL, pkCoPath);

            RandomOracle.RandomOracleResult ro2 = RandomOracle.H(k, pkPrimeL);
            s = ro2.getS();
            sPrime = ro2.getK();

            Nike.KeyPair kpParent = Nike.gen(s);
            byte[] pkParent = kpParent.getEk();
            pkMap.put(path.get(l + 1), pkParent);
        }

        TreeEK newEk = tempTree.setNodes(pkMap);

        byte R = (byte) 'R';
        c_BKRemove c = new c_BKRemove(R, i, pkStarMap, pkCircle, pkPrimeMap);

        return new BKRemoveResult(newEk, c);
    }

    public class BKForkResult {
        public TreeEK ek1;
        public TreeEK ek2;
        public c_BKFork c;

        public BKForkResult(TreeEK ek1, TreeEK ek2, c_BKFork c) {
            this.ek1 = ek1;
            this.ek2 = ek2;
            this.c = c;
        }
    }

    public class c_BKFork {
        public byte t;
        public byte[] pk;

        public c_BKFork(byte t, byte[] pk) {
            this.t = t;
            this.pk = pk;
        }
    }

    public BKForkResult fork(TreeEK ek) throws Exception
    {
        Nike.KeyPair forkKp = Nike.gen();
        byte[] pkFork = forkKp.getEk();
        byte[] skFork = forkKp.getDk();

        byte F = (byte) 'F';
        byte[] c = prependByte(F, pkFork);

        TreeGetNodesReturn getnodesreturn = Tree.getNodes(ek);
        Map<Integer, byte[]> pkMap = getnodesreturn.getDataPk();
        Tree Tree1 = getnodesreturn.getTree();


        Map<Integer, byte[]> pk1Map = new HashMap<>();
        Map<Integer, byte[]> pk2Map = new HashMap<>();

        // In tree add and rmv. these pk and sk map should be updated.
        // to do: may be get the tree with valid nodes. 
        // update: this seems ok for now
        for (Map.Entry<Integer, byte[]> entry : pkMap.entrySet()) {
            int nodeId = entry.getKey();
            byte[] pkj = entry.getValue();

            byte[] k = Nike.key(skFork, pkj);

            // H(k, c, 1)
            RandomOracle.RandomOracleResult ro1 = RandomOracle.H(c, k, new byte[]{1});
            byte[] s1j = ro1.getS();

            // H(k, c, 2)
            RandomOracle.RandomOracleResult ro2 = RandomOracle.H(c, k, new byte[]{2});
            byte[] s2j = ro2.getS();

            Nike.KeyPair kp1j = Nike.gen(s1j);
            Nike.KeyPair kp2j = Nike.gen(s2j);

            pk1Map.put(nodeId, kp1j.getEk());
            pk2Map.put(nodeId, kp2j.getEk());
        }

        TreeEK ek1 = Tree1.setNodes(pk1Map);
        TreeEK ek2 = Tree1.setNodes(pk2Map);

        return new BKForkResult(ek1, ek2, new c_BKFork(F, pkFork));
    }

    public class BKProcResult {
        public TreeDk dk1;
        public TreeDk dk2;

        public BKProcResult(TreeDk dk1, TreeDk dk2) {
            this.dk1 = dk1;
            this.dk2 = dk2;
        }
    }

    public BKProcResult proc(TreeDk dk, Object c) throws Exception
    {
        byte t;
        Object cPrime;

        // List<Integer> set2 = new ArrayList<>(dk.getDataSk().keySet());
        // int leafNodeIndex = Tree.findLeafIndexFromSet(set2, dk.getTree());
        // List<Integer> dkPath = dk.getTree().T_path(leafNodeIndex);

        if (c instanceof c_BKFork)
        {
            c_BKFork fork = (c_BKFork) c;
            t = fork.t;
            cPrime = fork.pk;

            TreeGetPathReturn pathResult = Tree.getPath(dk);
            int i = pathResult.getLeafIndex();
            Map<Integer, byte[]> skMap = pathResult.getDataSk();

            Map<Integer, byte[]> sk1Map = new HashMap<>();
            Map<Integer, byte[]> sk2Map = new HashMap<>();

            for (Map.Entry<Integer, byte[]> entry : skMap.entrySet())
            {
                int nodeId = entry.getKey();
                byte[] skl = entry.getValue();

                byte[] k = Nike.key(skl, (byte[]) cPrime);

                byte[] cRaw = prependByte(t, (byte[]) cPrime);
                byte[] tag1 = new byte[]{1};
                byte[] tag2 = new byte[]{2};

                byte[] s1l = RandomOracle.H(cRaw, k, tag1).getS();
                byte[] s2l = RandomOracle.H(cRaw, k, tag2).getS();

                byte[] sk1l = Nike.gen(s1l).getDk();
                byte[] sk2l = Nike.gen(s2l).getDk();

                sk1Map.put(nodeId, sk1l);
                sk2Map.put(nodeId, sk2l);
            }

            TreeDk dk1 = pathResult.getTree().setPath(i, sk1Map);
            TreeDk dk2 = pathResult.getTree().setPath(i, sk2Map);
            return new BKProcResult(dk1, dk2);
        } 
        else if (c instanceof c_BKAdd || c instanceof c_BKRemove)
        {
            TreeDk newDk;
            Tree tree;
            int i;
            Map<Integer, byte[]> skMap;
            int lStar;

            if (c instanceof c_BKAdd)
            {
                c_BKAdd add = (c_BKAdd) c;
                t = add.t;
                cPrime = new Object[]{add.pk_lMap, add.pkstarMap};

                TreeAddDkReturn addDk = Tree.T_add_dk(dk);
                tree = addDk.getTree();
                i = addDk.getLeafIndex();
                skMap = addDk.getDataSk();
                lStar = addDk.getLeafIntersectionIndex();
            }
            else
            {
                c_BKRemove rem = (c_BKRemove) c;
                t = rem.t;
                cPrime = new Object[]{rem.i, rem.pkStarMap, rem.pkCircle, rem.pkPrimeMap};

                TreeAddDkReturn remDk = Tree.T_rem_Dk(dk, rem.i);
                tree = remDk.getTree();
                i = remDk.getLeafIndex();
                skMap = remDk.getDataSk();
                lStar = remDk.getLeafIntersectionIndex();
            }

            // Get path length
            List<Integer> path = tree.T_path(i);
            int position = path.indexOf(lStar);

            byte[] pkR, skL;
            if (t == 'R')
            {
                c_BKRemove rem = (c_BKRemove) c;
                if (lStar == path.get(0)) 
                {
                    pkR = rem.pkCircle;
                    skL = skMap.get(path.get(0));
                }
                else
                {
                    pkR = rem.pkPrimeMap.get(path.get(position - 1));
                    skL = skMap.get(path.get(position - 1));
                }
            }
            else
            {

                c_BKAdd add = (c_BKAdd) c;
                pkR = add.pk_lMap.get(path.get(position - 1));
                skL = skMap.get(path.get(position - 1));
            }

            byte[] k = Nike.key(skL, pkR);
            RandomOracle.RandomOracleResult ro = RandomOracle.H(k, pkR);
            byte[] s = ro.getS();
            byte[] sPrime = ro.getK();

            skMap.put(path.get(lStar), Nike.gen(s).getDk());


            for (int jj = position; jj < ((path.size())-1) ; jj++) {
                Nike.KeyPair kp = Nike.gen(sPrime);
                byte[] sk_l = kp.getDk();
                pkR = (t == 'R') ? ((c_BKRemove) c).pkStarMap.get(path.get(jj)) : ((c_BKAdd) c).pkstarMap.get(path.get(jj));
                k = Nike.key(sk_l, pkR);
                ro = RandomOracle.H(k, kp.getEk());
                s = ro.getS();
                sPrime = ro.getK();

                skMap.put(path.get(jj - 1), Nike.gen(s).getDk());
            }

            newDk = tree.setPath(i, skMap);
            return new BKProcResult(newDk, null);
        }

        throw new IllegalArgumentException("Unsupported ciphertext type");
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
