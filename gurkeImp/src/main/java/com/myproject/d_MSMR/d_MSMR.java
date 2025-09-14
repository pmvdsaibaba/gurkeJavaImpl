package com.myproject.d_MSMR;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.dynamicUBKem.UB_KEM.BKGenResult;
import com.myproject.dynamicUBKem.UB_KEM.c_BKAdd;
import com.myproject.dynamicUBKem.UB_KEM.c_BKRemove;
import com.myproject.dynamicUBKem.UB_KEM.c_BKFork;

import com.myproject.dynamicUBKem.UB_KEM.BKEncResult;
import com.myproject.dynamicUBKem.UB_KEM.EncOutput;
import com.myproject.dynamicUBKem.UB_KEM.FinResult;

import com.myproject.standardKEM.KEM.*;
import com.myproject.standardKEM.KEM;

import com.myproject.RandomOracle.RandomOracle;

import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeDk;
import com.myproject.signatureScheme.SignatureScheme;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

import java.util.Map;
import java.util.HashMap;

import java.util.Queue;
import java.util.LinkedList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;

public class d_MSMR {

    // Operation types
    public enum OpType {
        ADD, REMOVE
    }

    // Target types
    public enum TargetType {
        SENDER, RECEIVER
    }

    public static class QueuedOperation {
        public OpType op;
        public TargetType t;
        public int uid;
        public Object diff;
        // public TreeEK ekuidR;
        public byte[] ekuidR;

        public QueuedOperation(OpType op, TargetType t, int uid, Object diff, byte[] ekuidR) {
            this.op = op;
            this.t = t;
            this.uid = uid;
            this.diff = diff;
            this.ekuidR = ekuidR;
        }
    }


    public static class SenderState {
        public int i;
        public Set<Integer> memS;
        public Set<Integer> memR;
        public TreeEK ek;
        public byte[] ssk;
        public byte[] svk;
        public byte[] tr;
        public Queue<QueuedOperation> ops;

        public SenderState(int i, Set<Integer> memS, Set<Integer> memR, TreeEK ek, 
                          byte[] ssk, byte[] svk, byte[] tr, Queue<QueuedOperation> ops) {
            this.i = i;
            this.memS = new HashSet<>(memS);
            this.memR = new HashSet<>(memR);
            this.ek = ek;
            this.ssk = ssk;
            this.svk = svk;
            this.tr = tr;
            this.ops = new LinkedList<>(ops);
        }
    }

    public static class SenderStateInReceiver {
        public Set<Integer> memS;
        public Set<Integer> memR;
        public Map<Integer, TreeDk> dkMap; // St[i] for each sender i
        public Map<Integer, byte[]> svkMap; // svk for each sender i
        public Map<Integer, byte[]> trMap; // tr for each sender i

        public SenderStateInReceiver(Set<Integer> memS, Set<Integer> memR, Map<Integer, TreeDk> dkMap,
                           Map<Integer, byte[]> svkMap, Map<Integer, byte[]> trMap) {
            this.memS = new HashSet<>(memS);
            this.memR = new HashSet<>(memR);
            this.dkMap = new HashMap<>(dkMap);
            this.svkMap = new HashMap<>(svkMap);
            this.trMap = new HashMap<>(trMap);
        }
    }

    public static class InitResult {
        public Map<Integer, SenderState> senderStates;
        public Map<Integer, Map<Integer, SenderStateInReceiver>> receiverStatesMap;

        public InitResult(Map<Integer, SenderState> senderStates, Map<Integer, Map<Integer, SenderStateInReceiver>> receiverStatesMap) {
            this.senderStates = senderStates;
            this.receiverStatesMap = receiverStatesMap;
        }
    }



////////////////////////////////////////////////////
    // Proc init(nS, nR)
////////////////////////////////////////////////////
    public static InitResult procInit(int nS, int nR) throws Exception
    {
        Map<Integer, SenderState> senderStates = new HashMap<>();

        Map<Integer, Map<Integer, SenderStateInReceiver>> receiverStateMap = new HashMap<>();

        // For each sender
        for (int i = 1; i <= nS; i++)
        {
            // Generate BK keys
            BKGenResult bkGenResult = UB_KEM.gen(nR);
            TreeEK ek = bkGenResult.ek;
            List<TreeDk> dkList = bkGenResult.dkList;

            // Generate signature keys
            SignatureScheme.KeyPair sigKeyPair = SignatureScheme.gen();
            byte[] svk = sigKeyPair.getVk();
            byte[] ssk = sigKeyPair.getSk();

            byte[] tr = new byte[0]; // ε
            Queue<QueuedOperation> ops = new LinkedList<>();

            Set<Integer> memS = new HashSet<>();
            for (int s = 1; s <= nS; s++)
            {
                memS.add(s);
            }

            Set<Integer> memR = new HashSet<>();
            for (int r = 1; r <= nR; r++)
            {
                memR.add(r);
            }

            SenderState senderState = new SenderState(i, memS, memR, ek, ssk, svk, tr, ops);
            senderStates.put(i, senderState);

            // Create receiver states for this sender
            for (int j = 1; j <= nR; j++)
            {
                Map<Integer, TreeDk> dkMap = new HashMap<>();
                Map<Integer, byte[]> svkMap = new HashMap<>();
                Map<Integer, byte[]> trMap = new HashMap<>();

                dkMap.put(i, dkList.get(j - 1));
                svkMap.put(i, svk);
                trMap.put(i, tr);

                SenderStateInReceiver senderStateInReceiver = new SenderStateInReceiver(memS, memR, dkMap, svkMap, trMap);

                // Group by receiver ID (j)
                receiverStateMap
                    .computeIfAbsent(j, k -> new HashMap<>())  // initialize if absent
                    .put(i, senderStateInReceiver);            // add state for this sender
            }
        }

        return new InitResult(senderStates, receiverStateMap);
    }

    public static class ToOperation {
        public OpType op;
        public TargetType target;
        public int uid;

        public ToOperation(OpType op, TargetType target, int uid) {
            this.op = op;
            this.target = target;
            this.uid = uid;
        }

        public static ToOperation empty() {
            return new ToOperation(null, null, -1);
        }

        public boolean isEmpty() {
            return op == null && target == null && uid == -1;
        }
    }

    public static class Kid {
        public byte[] id;
        public int i;
        public Set<Integer> memS;
        public Set<Integer> memR;

        public Kid(byte[] id, int i, Set<Integer> memS, Set<Integer> memR) {
            this.id = id;
            this.i = i;
            this.memS = new HashSet<>(memS);
            this.memR = new HashSet<>(memR);
        }
    }

    public static class EncapsResult {
        public SenderState updatedState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public EncapsResult(SenderState updatedState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

    public static class Ciphertext {
        public int i;
        public byte[] cPrime;
        public Object cM;
        public Queue<QueuedCiphertext> cq;
        public byte[] svkStar;
        public byte[] svkPrime;
        public ToOperation to;
        public byte[] signature;

        public Ciphertext(int i, byte[] cPrime, Object cM, Queue<QueuedCiphertext> cq,
                         byte[] svkStar, byte[] svkPrime, ToOperation to, byte[] signature) {
            this.i = i;
            this.cPrime = cPrime;
            this.cM = cM;
            this.cq = new LinkedList<>(cq);
            this.svkStar = svkStar;
            this.svkPrime = svkPrime;
            this.to = to;
            this.signature = signature;
        }
    }

    public static class QueuedCiphertext {
        public OpType op;
        public TargetType t;
        public int uid;
        public Object cM;
        public Object cP;

        public QueuedCiphertext(OpType op, TargetType t, int uid, Object cM, Object cP) {
            this.op = op;
            this.t = t;
            this.uid = uid;
            this.cM = cM;
            this.cP = cP;
        }
    }
//////////////////////////////////////////////////////////
    // Helper Proc encaps(st, ek, ad, cM, cq, svk*, to)
//////////////////////////////////////////////////////////
    private static EncapsResult encaps(SenderState st, TreeEK ek, byte[] ad, Object cM, 
                                      Queue<QueuedCiphertext> cq, byte[] svkStar, ToOperation to) throws Exception
    {
        BKEncResult encResult = UB_KEM.enc(ek);
        EncOutput u = encResult.u;
        byte[] cPrime = encResult.c;

        SignatureScheme.KeyPair newSigKeys = SignatureScheme.gen();
        byte[] svkPrime = newSigKeys.getVk();
        byte[] sskPrime = newSigKeys.getSk();

        // Create message to sign
        byte[] messageToSign = concatAll(st.tr, ad, intToByteArray(st.i), cPrime, 
                                        serializeObject(cM), serializeQueue(cq), 
                                        svkStar, svkPrime, serializeToOperation(to));
        
        byte[] sigma = SignatureScheme.sgn(st.ssk, messageToSign);

        Ciphertext cR = new Ciphertext(st.i, cPrime, cM, cq, svkStar, svkPrime, to, sigma);

        byte[] finInput = concatAll(st.tr, ad, serializeCiphertext(cR));
        FinResult finResult = UB_KEM.fin(u, finInput);
        TreeEK newEk = finResult.ek;

        // Derive keys
        byte[] kdf_k = "kdf_k".getBytes();
        byte[] kdf_id = "kdf_id".getBytes();
        byte[] kdf_tr = "kdf_tr".getBytes();

        byte[] k = deriveKey(finResult.k, kdf_k);
        byte[] id = deriveKey(finResult.k, kdf_id);
        byte[] tr = deriveKey(finResult.k, kdf_tr);

        Kid kid = new Kid(id, st.i, st.memS, st.memR);

        // sender removal
        Set<Integer> memS = new HashSet<>(st.memS);
        if (to.target == TargetType.SENDER && to.op == OpType.REMOVE) {
            memS.remove(to.uid);
        }

        SenderState updatedState = new SenderState(st.i, memS, st.memR, newEk, sskPrime, svkPrime, tr, st.ops);
        
        return new EncapsResult(updatedState, cR, k, kid);
    }

    public static class EnqOpsResult {
        public SenderState updatedState;
        public Queue<QueuedCiphertext> cq;

        public EnqOpsResult(SenderState updatedState, Queue<QueuedCiphertext> cq) {
            this.updatedState = updatedState;
            this.cq = cq;
        }
    }

//////////////////////////////////////////////////////////
    // Helper Proc enq-ops(st, mem'S, mem'R)
////////////////////////////////////////////////////////////
    private static EnqOpsResult enqOps(SenderState st, Set<Integer> memSPrime, Set<Integer> memRPrime) throws Exception {
        Queue<QueuedCiphertext> cq = new LinkedList<>();
        
        while (!st.ops.isEmpty())
        {
            /*  Retrieves the next operation to process from the st.ops queue.
                The poll() method is used to retrieve and remove the head (first element) of the queue. If the queue is empty, it returns null. This is a non-blocking operation. */
            QueuedOperation queuedOp = st.ops.poll();
            Object cM = null;
            Object cP = null;

            if (queuedOp.t == TargetType.RECEIVER && queuedOp.op == OpType.ADD) {
                // Receiver add
                UB_KEM.BKAddResult addResult = UB_KEM.add(st.ek);

                // todo: if new state is needed or check if updating old state is ok.
                st.ek = addResult.ek;
                TreeDk dk = addResult.dk;
                cM = addResult.c;

                // Encrypt BK dk to receiver
                EncapsulationResult kemEncResult = KEM.enc(queuedOp.ekuidR);
                byte[] c1 = kemEncResult.c;
                byte[] k = kemEncResult.k;

                // // c2 = H(k, c1) ⊕ (dk, tr, diff)
                // Todo: check if the xor is truncating the hash if sizes are different
                // Todo: check if the serializeObject should be also for diff
                byte[] hash = RandomOracle.Hash2(k, c1);
                byte[] dkTrDiff = concatAll(serializeTreeDk(dk), st.tr, serializeObject_Diff(queuedOp.diff));
                byte[] c2 = xor(hash, dkTrDiff);
                
                cP = new Object[]{c1, c2}; // (c1, c2)
            } else if (queuedOp.t == TargetType.RECEIVER && queuedOp.op == OpType.REMOVE) {
                // Receiver remove
                UB_KEM.BKRemoveResult removeResult = UB_KEM.rmv(st.ek, queuedOp.uid);
                st.ek = removeResult.ek;
                cM = removeResult.c;
                cP = null;
            }

            QueuedCiphertext qc = new QueuedCiphertext(queuedOp.op, queuedOp.t, queuedOp.uid, cM, cP);

            /* Adds the queued ciphertext to the queue cq. */
            cq.offer(qc);
        }

        SenderState updatedState = new SenderState(st.i, memSPrime, memRPrime, st.ek, st.ssk, st.svk, st.tr, st.ops);
        return new EnqOpsResult(updatedState, cq);
    }




    public static class DeqOpsResult {
        public Set<Integer> memS;
        public Set<Integer> memR;
        public TreeDk dk;
        public byte[] svk;
        public byte[] tr;

        public DeqOpsResult(Set<Integer> memS, Set<Integer> memR, TreeDk dk, byte[] svk, byte[] tr) {
            this.memS = new HashSet<>(memS);
            this.memR = new HashSet<>(memR);
            this.dk = dk;
            this.svk = svk;
            this.tr = tr;
        }
    }

//////////////////////////////////////////////////////////
    // Helper Proc deq-ops(st, cq, i)
////////////////////////////////////////////////////////////
    private static DeqOpsResult deqOps(Set<Integer> memS, Set<Integer> memR, TreeDk dk, 
                                    byte[] svk, byte[] tr, Queue<QueuedCiphertext> cq, int i) throws Exception
    {
        Set<Integer> currentMemS = new HashSet<>(memS);
        Set<Integer> currentMemR = new HashSet<>(memR);
        // Dk could be of type bytes or MapTreeDk
        TreeDk currentDk = dk;
        byte[] currentSvk = svk;
        byte[] currentTr = tr;

        while (!cq.isEmpty())
        {
            QueuedCiphertext qc = cq.poll();
            OpType op = qc.op;
            TargetType t = qc.t;
            int uid = qc.uid;
            Object cM = qc.cM;
            Object cP = qc.cP;

            // If tr is empty (uninitialized)
            if (currentTr.length == 0)
            {
                if (t == TargetType.RECEIVER && op == OpType.ADD)
                {
/////////////////////////////////////////////////////////////
// This code need to be fixed.
// there should be a new parameter dk different from TreeDk
/////////////////////////////////////////////////////////////
                    // // Init message from sender i
                    // if (cP instanceof Object[])
                    // {
                    //     Object[] cPArray = (Object[]) cP;
                    //     byte[] c1 = (byte[]) cPArray[0];
                    //     byte[] c2 = (byte[]) cPArray[1];

                    //     // Decrypt
                    //     // Todo: Verify for the case when Receiver is added Dk is of type, Bytes
                    //     DecapsulationResult kemDecResult = KEM.dec(currentDk, c1);
                    //     byte[] k = kemDecResult.k;

                    //     // Derive plaintext
                    //     byte[] hashInput = concatAll(k, c1);
                    //     byte[] hash = computeHash(hashInput);
                    //     byte[] plaintext = xor(c2, hash);

                    //     // Parse plaintext (dk, tr, diff)
                    //     // This is a simplified parsing - in practice you'd need proper deserialization
                    //     currentDk = deserializeTreeDk(plaintext); // Extract dk
                    //     currentTr = extractTr(plaintext); // Extract tr
                    //     Diff diffResult = extractDiff(plaintext); // Extract diff

                    //     // Merge diff into membership sets
                    //     MergeDiffResult mergeResult = mergeDiff(currentMemS, currentMemR, 
                    //                                         diffResult.senderAdd, diffResult.senderRemove,
                    //                                         diffResult.receiverAdd, diffResult.receiverRemove);
                    //     currentMemS = mergeResult.newMemS;
                    //     currentMemR = mergeResult.newMemR;
                    // }
                } else {
                    // Ignore until init from sender i
                    continue;
                }
            }

            // Update membership based on operation
            if (op == OpType.ADD) {
                if (t == TargetType.SENDER) {
                    currentMemS.add(uid);
                } else if (t == TargetType.RECEIVER) {
                    currentMemR.add(uid);
                }
            } else if (op == OpType.REMOVE) {
                if (t == TargetType.SENDER) {
                    currentMemS.remove(uid);
                } else if (t == TargetType.RECEIVER) {
                    currentMemR.remove(uid);
                }
            }

            // Process BK operations for receivers
            if (t == TargetType.RECEIVER && cM != null)
            {
                UB_KEM.BKProcResult procResult = UB_KEM.proc(currentDk, cM);
                // BKRemoveResult procResult = UB_KEM.proc(currentDk, cM);
                // UB_KEM.proc retrun type is different from what is expected here
                currentDk = procResult.dk1;
                // Handle forked keys if needed
            }
        }

        return new DeqOpsResult(currentMemS, currentMemR, currentDk, currentSvk, currentTr);
    }


////////////////////////////////////////////////////
// Proc rcv(st, ad, c)
////////////////////////////////////////////////////
    public static ReceiveResult procRcv(Map<Integer, SenderStateInReceiver> st, byte[] ad, Ciphertext c) throws Exception
    {
        if (c == null) {
            return new ReceiveResult(st, null, null, false);
        }

        // Parse ciphertext
        int i = c.i;
        byte[] cPrime = c.cPrime;
        Object cM = c.cM;
        Queue<QueuedCiphertext> cq = c.cq;
        byte[] svkStar = c.svkStar;
        byte[] svkPrime = c.svkPrime;
        ToOperation to = c.to;
        byte[] sigma = c.signature;

        SenderStateInReceiver ST = st.get(i);
        if (ST == null || !ST.dkMap.containsKey(i) || !ST.svkMap.containsKey(i) || !ST.trMap.containsKey(i)) {
            return new ReceiveResult(st, null, null, false);
        }

        OpType op = to.op;
        TargetType t = to.target;
        int uid = to.uid;

        TreeDk dk = ST.dkMap.get(i);
        byte[] svk = ST.svkMap.get(i);
        byte[] tr = ST.trMap.get(i);
        Set<Integer> memS = new HashSet<>(ST.memS);
        Set<Integer> memR = new HashSet<>(ST.memR);

        // Dequeue operations
        DeqOpsResult deqResult = deqOps(memS, memR, dk, svk, tr, cq, i);
        memS = deqResult.memS;
        memR = deqResult.memR;
        dk = deqResult.dk;
        svk = deqResult.svk;
        tr = deqResult.tr;

        // Verify signature
        byte[] messageToVerify = concatAll(tr, ad, intToByteArray(c.i), cPrime, serializeObject(cM), 
                                        serializeQueue(cq), svkStar, svkPrime, 
                                        serializeToOperation(to));
        
        if (!SignatureScheme.vfy(svk, messageToVerify, sigma)) {
            return new ReceiveResult(st, null, null, false);
        }

        // Process cM if not empty
        if (cM != null && !isEmptyObject(cM)) {
            UB_KEM.BKProcResult procResult = UB_KEM.proc(dk, cM);
            dk = procResult.dk1;
            TreeDk dkStar = procResult.dk2;
            // int uid = procResult.uid;
            // Todo: There is issue with algorithm.
            // int uid = 10001;

            // If forked
            if (dkStar != null) {
                // Add new sender state
                // ST.dkMap.put(uid, dkStar);
                // ST.svkMap.put(uid, svkStar);
                // ST.trMap.put(uid, new byte[0]); // ε

                Map<Integer, TreeDk> dkMap = new HashMap<>();
                Map<Integer, byte[]> svkMap = new HashMap<>();
                Map<Integer, byte[]> trMap = new HashMap<>();

                dkMap.put(uid, dkStar);
                svkMap.put(uid, svkStar);
                trMap.put(uid, new byte[0]); // ε

                SenderStateInReceiver newSenderState = new SenderStateInReceiver(new HashSet<>(memS), new HashSet<>(memR), dkMap, svkMap, trMap);
                st.put(uid, newSenderState);
            }

            // Update membership based on operation
            if (to.op == OpType.ADD) {
                if (to.target == TargetType.SENDER) {
                    memS.add(uid);
                } else if (to.target == TargetType.RECEIVER) {
                    memR.add(uid);
                }
            } else if (to.op == OpType.REMOVE) {
                if (to.target == TargetType.RECEIVER) {
                    memR.remove(uid);
                }
            }
        }

        // Decrypt
        byte[] finInput = concatAll(tr, ad, serializeCiphertext(c));
        UB_KEM.DecResult decResult = UB_KEM.dec(dk, finInput, cPrime);
        dk = decResult.dk;

        byte[] kdf_k = "kdf_k".getBytes();
        byte[] kdf_id = "kdf_id".getBytes();
        byte[] kdf_tr = "kdf_tr".getBytes();

        byte[] k = deriveKey(decResult.k, kdf_k);
        byte[] id = deriveKey(decResult.k, kdf_id);
        byte[] newTr = deriveKey(decResult.k, kdf_tr);

        Kid kid = new Kid(id, i, memS, memR);

        // Handle sender removal
        if (to.op == OpType.REMOVE && to.target == TargetType.SENDER) {
            memS.remove(to.uid);
            if (to.uid == i) {
                // Current sender removed, remove from state
                ST.dkMap.remove(i);
                ST.svkMap.remove(i);
                ST.trMap.remove(i);
            }
        }

        // Update state for sender i
        ST.dkMap.put(i, dk);
        ST.svkMap.put(i, svkPrime);
        ST.trMap.put(i, newTr);

        SenderStateInReceiver updatedState = new SenderStateInReceiver(memS, memR, ST.dkMap, ST.svkMap, ST.trMap);
        st.put(i, updatedState);

        return new ReceiveResult(st, k, kid, true);
    }

    public static class ReceiveResult {
        public Map<Integer, SenderStateInReceiver> updatedState;
        public byte[] key;
        public Kid kid;
        public boolean success;

        public ReceiveResult(Map<Integer, SenderStateInReceiver> updatedState, byte[] key, Kid kid, boolean success) {
            this.updatedState = updatedState;
            this.key = key;
            this.kid = kid;
            this.success = success;
        }
    }
    private static boolean isEmptyObject(Object obj) {
        // Check if object represents empty/null value
        return obj == null || (obj instanceof byte[] && ((byte[]) obj).length == 0);
    }

    public static class Diff {
        public Set<Integer> senderAdd;
        public Set<Integer> senderRemove;
        public Set<Integer> receiverAdd;
        public Set<Integer> receiverRemove;

        public Diff(Set<Integer> senderAdd, Set<Integer> senderRemove, 
                    Set<Integer> receiverAdd, Set<Integer> receiverRemove) {
            this.senderAdd = new HashSet<>(senderAdd);
            this.senderRemove = new HashSet<>(senderRemove);
            this.receiverAdd = new HashSet<>(receiverAdd);
            this.receiverRemove = new HashSet<>(receiverRemove);
        }
    }

    public static class MergeDiffResult {
        public Set<Integer> newMemS;
        public Set<Integer> newMemR;

        public MergeDiffResult(Set<Integer> newMemS, Set<Integer> newMemR) {
            this.newMemS = new HashSet<>(newMemS);
            this.newMemR = new HashSet<>(newMemR);
        }
    }

    // Helper Proc diff(A, A', B, B')
    private static Diff diff(Set<Integer> A, Set<Integer> APrime, Set<Integer> B, Set<Integer> BPrime) {
        Set<Integer> senderAdd = new HashSet<>(APrime);
        senderAdd.removeAll(A);
        
        Set<Integer> senderRemove = new HashSet<>(A);
        senderRemove.removeAll(APrime);
        
        Set<Integer> receiverAdd = new HashSet<>(BPrime);
        receiverAdd.removeAll(B);
        
        Set<Integer> receiverRemove = new HashSet<>(B);
        receiverRemove.removeAll(BPrime);
        
        return new Diff(senderAdd, senderRemove, receiverAdd, receiverRemove);
    }

    // Helper Proc merge-diff(A, B, A+, A-, B+, B-)
    private static MergeDiffResult mergeDiff(Set<Integer> A, Set<Integer> B, 
                                            Set<Integer> APlus, Set<Integer> AMinus,
                                            Set<Integer> BPlus, Set<Integer> BMinus) {
        Set<Integer> newMemS = new HashSet<>(A);
        newMemS.addAll(APlus);
        newMemS.removeAll(AMinus);
        
        Set<Integer> newMemR = new HashSet<>(B);
        newMemR.addAll(BPlus);
        newMemR.removeAll(BMinus);
        
        return new MergeDiffResult(newMemS, newMemR);
    }

////////////////////////////////////////////////
    // Proc snd(st, ad)
///////////////////////////////////////////////
    public static SendResult procSnd(SenderState st, byte[] ad) throws Exception {
        // Run queued ops first
        EnqOpsResult enqResult = enqOps(st, st.memS, st.memR);
        st = enqResult.updatedState;
        Queue<QueuedCiphertext> cq = enqResult.cq;

        ToOperation to = ToOperation.empty();
        EncapsResult encapsResult = encaps(st, st.ek, ad, null, cq, new byte[0], to);
        
        return new SendResult(encapsResult.updatedState, encapsResult.ciphertext, 
                             encapsResult.key, encapsResult.kid);
    }

    public static class SendResult {
        public SenderState updatedState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public SendResult(SenderState updatedState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

///////////////////////////////////////////////////

///////////////////////////////////////////////////
    public static byte[] concatenateByteArrays(Map<Integer, byte[]> byteMap)
    {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            for (byte[] byteArray : byteMap.values()) {
                outputStream.write(byteArray);
            }
            return outputStream.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error concatenating byte arrays", e);
        }
    }


    private static byte[] serializeObject(Object obj)
    {
        /* May be there are more object types to cover */
        if (obj == null)
        {
            return new byte[0];
        }
        else if (obj instanceof c_BKAdd)
        {
            // System.out.println("Serializing object of type: " + obj.getClass().getName());
            // System.out.println("Processing c_BKAdd");
            c_BKAdd c = (c_BKAdd) obj;

            return concatAll(
                new byte[]{c.t},
                concatenateByteArrays(c.pkstarMap),
                concatenateByteArrays(c.pk_lMap)
            );
        }
        else if (obj instanceof c_BKRemove)
        {
            // System.out.println("Serializing object of type: " + obj.getClass().getName());
            // System.out.println("Processing c_BKRemove");
            c_BKRemove c = (c_BKRemove) obj;

            return concatAll(
                new byte[]{c.t},
                intToByteArray(c.i),
                concatenateByteArrays(c.pkStarMap),
                c.pkCircle,
                concatenateByteArrays(c.pkPrimeMap)
            );
        }
        else if (obj instanceof c_BKFork)
        {
            c_BKFork c = (c_BKFork) obj;

            return concatAll(
                new byte[]{c.t},
                c.pk
            );
        }
        else if (obj instanceof byte[])
        {
            return new byte[0]; 
        }
        // not sure what to return
        // return obj.toString().getBytes();
        System.out.println("WARNING: Falling back to toString() for type: " + obj.getClass().getName());
        // return new byte[0];
        throw new IllegalArgumentException("Unsupported ciphertext type: " + obj.getClass().getName());
    }
    
    private static byte[] serializeObject_Diff(Object obj)
    {
        /* May be there are more object types to cover */
        if (obj == null)
        {
            return new byte[0];
        }
        else if (obj instanceof Diff)
        {
            // System.out.println("Serializing object of type: " + obj.getClass().getName());
            Diff d = (Diff) obj;

            return concatAll(
                concatenateIntSet(d.senderAdd),
                concatenateIntSet(d.senderRemove),
                concatenateIntSet(d.receiverAdd),
                concatenateIntSet(d.receiverRemove)
            );
        }
        else if (obj instanceof byte[])
        {
            // should not occur here, as this is a diff serialization
            System.out.println("Serializing byte[] object, returning empty array");
            return new byte[0]; 
        }

        System.out.println("WARNING: Falling back to toString() for type: " + obj.getClass().getName());
        throw new IllegalArgumentException("Unsupported ciphertext type: " + obj.getClass().getName());
    }

    private static byte[] concatenateIntSet(Set<Integer> set)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Integer value : set) {
            byte[] bytes = intToByteArray(value);
            outputStream.write(bytes, 0, bytes.length);
        }
        return outputStream.toByteArray();
    }

    private static byte[] serializeTreeDk(TreeDk dk)
    {
        Map<Integer, byte[]> skMap = dk.getDataSk();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] value : skMap.values()) {
            if (value != null) {
                outputStream.write(value, 0, value.length);
            }
        }
        byte[] concatenated = outputStream.toByteArray();
        return concatenated;
    }

    private static byte[] serializeQueue(Queue<QueuedCiphertext> cq) {
        if (cq == null || cq.isEmpty()) {
            return new byte[0];
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (QueuedCiphertext qc : cq) {
            byte[] opBytes = new byte[]{(byte) qc.op.ordinal()};
            byte[] tBytes = new byte[]{(byte) qc.t.ordinal()};
            byte[] uidBytes = intToByteArray(qc.uid);
            byte[] cMBytes = serializeObject(qc.cM);
            byte[] cPBytes;
            if (qc.cP instanceof byte[]) {
                cPBytes = (byte[]) qc.cP;
            } else if (qc.cP != null) {
                // If cP is an Object[] (e.g., {c1, c2}), concatenate its byte[] elements
                if (qc.cP instanceof Object[]) {
                    Object[] arr = (Object[]) qc.cP;
                    ByteArrayOutputStream cPStream = new ByteArrayOutputStream();
                    for (Object o : arr) {
                        if (o instanceof byte[]) {
                            byte[] b = (byte[]) o;
                            cPStream.write(b, 0, b.length);
                        }
                    }
                    cPBytes = cPStream.toByteArray();
                } else {
                    cPBytes = serializeObject(qc.cP);
                }
            } else {
                cPBytes = new byte[0];
            }
            byte[] serialized = concatAll(opBytes, tBytes, uidBytes, cMBytes, cPBytes);
            try {
                outputStream.write(serialized);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing QueuedCiphertext", e);
            }
        }
        return outputStream.toByteArray();
    }

    private static byte[] serializeToOperation(ToOperation to) {
        if (to.isEmpty()) {
            return new byte[0];
        }
        return concatAll(new byte[]{(byte)(to.op.ordinal())}, 
                        new byte[]{(byte)(to.target.ordinal())}, 
                        intToByteArray(to.uid));
    }

    private static byte[] serializeCiphertext(Ciphertext c) {
        return concatAll(intToByteArray(c.i), c.cPrime, serializeObject(c.cM), 
                        serializeQueue(c.cq), c.svkStar, c.svkPrime, 
                        serializeToOperation(c.to), c.signature);
    }


    private static byte[] concatAll(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, result, currentIndex, array.length);
                currentIndex += array.length;
            }
        }
        return result;
    }

    private static byte[] xor(byte[] a, byte[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays must have same length");
        }
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte)(a[i] ^ b[i]);
        }
        return result;
    }

    private static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)(value)
        };
    }

    private static byte[] deriveKey(byte[] masterKey, byte[] info) {
        byte[] salt = new byte[] {0x01, 0x02, 0x03, 0x04};
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        HKDFParameters params = new HKDFParameters(masterKey, salt, info);
        hkdf.init(params);

        byte[] derivedKey = new byte[64]; // 64 bytes = 512-bit key
        hkdf.generateBytes(derivedKey, 0, derivedKey.length);
        return derivedKey;
    }

}

