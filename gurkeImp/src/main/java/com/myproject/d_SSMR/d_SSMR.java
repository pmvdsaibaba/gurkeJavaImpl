package com.myproject.d_SSMR;

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

import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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


////////////////////////////////////////////77
// init
    public static InitResult procInit(int nR) throws Exception
    {

        BKGenResult bkGenResult = UB_KEM.gen(nR);
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


    public static class Ciphertext {
        public byte[] cPrime;
        public Object cM;
        public byte[] svkPrime;
        public byte[] signature;

        public Ciphertext(byte[] cPrime, Object cM, byte[] svkPrime, byte[] signature) {
            this.cPrime = cPrime;
            this.cM = cM;
            this.svkPrime = svkPrime;
            this.signature = signature;
        }
    }

    public static class Kid {
        public byte[] id;
        public Set<Integer> memR;

        public Kid(byte[] id, Set<Integer> memR) {
            this.id = id;
            this.memR = new HashSet<>(memR);
        }
    }

    public static class EncapsResult {
        public senderState updatedState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public EncapsResult(senderState updatedState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }


////////////////////////////////////////////77
// encaps
    private static EncapsResult encaps(senderState st, TreeEK ek, byte[] ad, Object cM) throws Exception
    {

        Set<Integer> memR = st.memR;
        TreeEK ekPrime = st.ek;
        byte[] ssk = st.ssk;
        byte[] svk = st.svk;
        byte[] tr = st.tr;


        BKEncResult encResult = UB_KEM.enc(ek);
        EncOutput u = encResult.u;
        byte[] cPrime = encResult.c;

        SignatureScheme.KeyPair newSigKeys = SignatureScheme.gen();
        byte[] svkPrime = newSigKeys.getVk();
        byte[] sskPrime = newSigKeys.getSk();

        byte[] messageToSign = concatAll(tr, ad, cPrime, serializeObject(cM), svkPrime);
        byte[] sigma = SignatureScheme.sgn(ssk, messageToSign);

        Ciphertext cR = new Ciphertext(cPrime, cM, svkPrime, sigma);

        byte[] finInput = concatAll(tr, ad, serializeCiphertext(cR));
        FinResult finResult = UB_KEM.fin(u, finInput);
        TreeEK newEk = finResult.ek;
        byte[] k = finResult.k;

        Kid kid = new Kid(k, memR); // Using k as id

        // senderState newState = new senderState(memR, newEk, sskPrime, svkPrime, tr);
        senderState newState = new senderState(memR, newEk, sskPrime, svkPrime, k);

        return new EncapsResult(newState, cR, k, kid);
    }


    public static class SendResult {
        public senderState updatedState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public SendResult(senderState updatedState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

////////////////////////////////////////////77
// snd
    public static SendResult procSnd(senderState st, byte[] ad) throws Exception {

        EncapsResult encapsResult = encaps(st, st.ek, ad, null);
        
        return new SendResult(encapsResult.updatedState, encapsResult.ciphertext, 
                             encapsResult.key, encapsResult.kid);
    }


    private static byte[] serializeCiphertext(Ciphertext c) {
        return concatAll(c.cPrime, serializeObject(c.cM), c.svkPrime, c.signature);
    }

    private static byte[] serializeObject(Object obj) {
        if (obj == null) {
            return new byte[0];
        }
        else if (obj instanceof c_BKAdd)
        {
            c_BKAdd c = (c_BKAdd) obj;

            return concatAll(
                new byte[]{c.t},
                concatenateByteArrays(c.pkstarMap),
                concatenateByteArrays(c.pk_lMap)
            );
        }
        else if (obj instanceof c_BKRemove)
        {
            c_BKRemove c = (c_BKRemove) obj;

            return concatAll(
                new byte[]{c.t},
                intToByteArray(c.i),
                concatenateByteArrays(c.pkStarMap),
                c.pkCircle,
                concatenateByteArrays(c.pkPrimeMap)
            );
        }
        // not sure what to return
        return obj.toString().getBytes();
    }

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

    private static byte[] concatAll(byte[]... arrays)
    {
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

    public static byte[] intToByteArray(int value)
    {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)(value)
        };
    }


}