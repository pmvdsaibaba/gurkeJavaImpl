package com.myproject.d_MSMR;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.dynamicUBKem.UB_KEM.BKGenResult;
import com.myproject.dynamicUBKem.UB_KEM.c_BKAdd;
import com.myproject.dynamicUBKem.UB_KEM.c_BKRemove;

import com.myproject.dynamicUBKem.UB_KEM.BKEncResult;
import com.myproject.dynamicUBKem.UB_KEM.EncOutput;
import com.myproject.dynamicUBKem.UB_KEM.FinResult;


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
        public TreeEK ekuidR;

        public QueuedOperation(OpType op, TargetType t, int uid, Object diff, TreeEK ekuidR) {
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

    public static class ReceiverState {
        public Set<Integer> memS;
        public Set<Integer> memR;
        public Map<Integer, TreeDk> dkMap; // St[i] for each sender i
        public Map<Integer, byte[]> svkMap; // svk for each sender i
        public Map<Integer, byte[]> trMap; // tr for each sender i

        public ReceiverState(Set<Integer> memS, Set<Integer> memR, Map<Integer, TreeDk> dkMap,
                           Map<Integer, byte[]> svkMap, Map<Integer, byte[]> trMap) {
            this.memS = new HashSet<>(memS);
            this.memR = new HashSet<>(memR);
            this.dkMap = new HashMap<>(dkMap);
            this.svkMap = new HashMap<>(svkMap);
            this.trMap = new HashMap<>(trMap);
        }
    }

    public static class InitResult {
        public List<SenderState> senderStates;
        public List<ReceiverState> receiverStates;

        public InitResult(List<SenderState> senderStates, List<ReceiverState> receiverStates) {
            this.senderStates = senderStates;
            this.receiverStates = receiverStates;
        }
    }

    // Proc init(nS, nR)
    public static InitResult procInit(int nS, int nR) throws Exception {
        List<SenderState> senderStates = new ArrayList<>();
        List<ReceiverState> receiverStates = new ArrayList<>();

        // For each sender
        for (int i = 1; i <= nS; i++) {
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
            for (int s = 1; s <= nS; s++) {
                memS.add(s);
            }

            Set<Integer> memR = new HashSet<>();
            for (int r = 1; r <= nR; r++) {
                memR.add(r);
            }

            SenderState senderState = new SenderState(i, memS, memR, ek, ssk, svk, tr, ops);
            senderStates.add(senderState);

            // Create receiver states for this sender
            for (int j = 0; j < nR; j++)
            {
                // if (receiverStates.size() <= j) {
                    // Initialize new receiver state
                    Map<Integer, TreeDk> dkMap = new HashMap<>();
                    Map<Integer, byte[]> svkMap = new HashMap<>();
                    Map<Integer, byte[]> trMap = new HashMap<>();
                    
                    dkMap.put(i, dkList.get(j));
                    svkMap.put(i, svk);
                    trMap.put(i, tr);
                    
                    ReceiverState receiverState = new ReceiverState(memS, memR, dkMap, svkMap, trMap);
                    receiverStates.add(receiverState);
                // } else {
                //     // Add to existing receiver state
                //     ReceiverState receiverState = receiverStates.get(j);
                //     receiverState.dkMap.put(i, dkList.get(j));
                //     receiverState.svkMap.put(i, svk);
                //     receiverState.trMap.put(i, tr);
                // }
            }
        }

        return new InitResult(senderStates, receiverStates);
    }


}

