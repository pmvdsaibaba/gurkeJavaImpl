package com.myproject.d_SSMR;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.dynamicUBKem.UB_KEM.BKGenResult;

import com.myproject.Tree.TreeEK;
import com.myproject.Tree.TreeDk;
import com.myproject.signatureScheme.SignatureScheme;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class d_SSMR {

    public static class senderState
    {
        public Set<Integer> memR;
        public TreeEK ek;
        public byte[] ssk;
        public byte[] svk;
        public byte[] tr;

        public senderState(Set<Integer> memR, TreeEK ek, byte[] ssk, byte[] svk, byte[] tr) {
            this.memR = new HashSet<>(memR);
            this.ek = ek;
            this.ssk = ssk;
            this.svk = svk;
            this.tr = tr;
        }
    }

    public static class ReceiverState {
        public Set<Integer> memR;
        public TreeDk dk;
        public byte[] svk;
        public byte[] tr;

        public ReceiverState(Set<Integer> memR, TreeDk dk, byte[] svk, byte[] tr) {
            this.memR = new HashSet<>(memR);
            this.dk = dk;
            this.svk = svk;
            this.tr = tr;
        }
    }

    public static class InitResult {
        public senderState senderState;
        public List<ReceiverState> receiverStates;

        public InitResult(senderState senderState, List<ReceiverState> receiverStates) {
            this.senderState = senderState;
            this.receiverStates = receiverStates;
        }
    }


    public static InitResult procInit(int nR) throws Exception
    {
        UB_KEM ubKem = new UB_KEM();

        BKGenResult bkGenResult = ubKem.gen(nR);
        TreeEK ek = bkGenResult.ek;
        List<TreeDk> dkList = bkGenResult.dkList;

        SignatureScheme.KeyPair sigKeyPair = SignatureScheme.gen();
        byte[] svk = sigKeyPair.getVk();
        byte[] ssk = sigKeyPair.getSk();

        // ε (empty byte array)
        byte[] tr = new byte[0];

        // HashSet is a collection that does not allow duplicate elements and does not guarantee order
        Set<Integer> memR = new HashSet<>();
        for (int j = 1; j <= nR; j++) {
            memR.add(j);
        }

        senderState senderState = new senderState(memR, ek, ssk, svk, tr);

        List<ReceiverState> receiverStates = new ArrayList<>();
        for (int j = 0; j < nR; j++) {
            ReceiverState receiverState = new ReceiverState(memR, dkList.get(j), svk, tr);
            receiverStates.add(receiverState);
        }

        return new InitResult(senderState, receiverStates);
    }

}