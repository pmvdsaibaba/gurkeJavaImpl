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


import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.digests.SHA512Digest;

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
        boolean isNewAddRcvr;

        public ReceiverState(Set<Integer> memR, TreeDk dk, byte[] svk, byte[] tr, boolean isNewAddRcvr) {
        // public ReceiverState(Set<Integer> memR, TreeDk dk, byte[] svk, byte[] tr) {
            this.memR = new HashSet<>(memR);
            this.dk = dk;
            this.svk = svk;
            this.tr = tr;
            this.isNewAddRcvr = isNewAddRcvr;
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
        boolean isNewAddRcvr = false;

        // HashSet is a collection that does not allow duplicate elements and does not guarantee order
        Set<Integer> memR = new HashSet<>();
        for (int j = 1; j <= nR; j++) {
            memR.add(j);
        }

        senderState senderState = new senderState(memR, ek, ssk, svk, tr);

        List<ReceiverState> receiverStates = new ArrayList<>();
        for (int j = 0; j < nR; j++) {
            ReceiverState receiverState = new ReceiverState(memR, dkList.get(j), svk, tr, isNewAddRcvr);
            // ReceiverState receiverState = new ReceiverState(memR, dkList.get(j), svk, tr);
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


        // May be bug. Discuss with Paul.
        // BKEncResult encResult = UB_KEM.enc(ekPrime);
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

        byte[] kdf_k = "kdf_k".getBytes();
        byte[] kdf_id = "kdf_id".getBytes();
        byte[] kdf_tr = "kdf_tr".getBytes();

        // byte[] k = finResult.k;

        byte[] k = deriveKey(finResult.k, kdf_k);
        byte[] id = deriveKey(finResult.k, kdf_id);
        tr = deriveKey(finResult.k, kdf_tr);

        Kid kid = new Kid(id, memR); // Using k as id

        // senderState newState = new senderState(memR, newEk, sskPrime, svkPrime, tr);
        // senderState newState = new senderState(memR, newEk, sskPrime, svkPrime, k);
        st.memR = memR;
        st.ek = newEk;
        st.ssk = sskPrime;
        st.svk = svkPrime;
        st.tr = tr;

        return new EncapsResult(st, cR, k, kid);
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


    public static class ReceiveFailure {
        public ReceiverState state;

        public ReceiveFailure(ReceiverState state) {
            this.state = state;
        }
    }

////////////////////////////////////////////77
// rcv
    public static Object procRcv(ReceiverState st, byte[] ad, Ciphertext c) throws Exception
    {

        byte[] cPrime = c.cPrime;
        Object cM = c.cM;
        byte[] svkPrime = c.svkPrime;
        byte[] signature = c.signature;

        // System.out.println("Type of cM: " + cM.getClass().getName());

        Set<Integer> memR = st.memR;
        TreeDk dk = st.dk;
        byte[] svk = st.svk;
        byte[] tr = st.tr;
        boolean isNewAddRcvr = st.isNewAddRcvr;

        byte[] messageToVerify = concatAll(tr, ad, cPrime, serializeObject(cM), svkPrime);
        if (!SignatureScheme.vfy(svk, messageToVerify, signature)) {
            return new ReceiveFailure(st);
        }

        // if ((cM != null && (!(cM instanceof byte[]) || ((byte[]) cM).length != 0)) || (isNewAddRcvr == true))
        if (cM != null && (!(cM instanceof byte[]) || ((byte[]) cM).length != 0))
        {
            if (isNewAddRcvr == true)
            {
                isNewAddRcvr = false;
            }
            else
            {
                UB_KEM.BKProcResult procResult = UB_KEM.proc(dk, cM);
                dk = procResult.dk1;
            }
        }

        byte[] fullMessage = concatAll(tr, ad, serializeCiphertext(c));
        UB_KEM.DecResult decResult = UB_KEM.dec(dk, fullMessage, cPrime);
        TreeDk newDk = decResult.dk;

        // byte[] k = decResult.k;

        byte[] kdf_k = "kdf_k".getBytes();
        byte[] kdf_id = "kdf_id".getBytes();
        byte[] kdf_tr = "kdf_tr".getBytes();

        byte[] k = deriveKey(decResult.k, kdf_k);
        byte[] id = deriveKey(decResult.k, kdf_id);
        tr = deriveKey(decResult.k, kdf_tr);

        Kid kid = new Kid(id, memR);

        // ReceiverState newState = new ReceiverState(memR, newDk, svkPrime, tr);
        st.memR = memR;
        st.dk = newDk;
        st.svk = svkPrime;
        st.tr = tr;
        st.isNewAddRcvr = isNewAddRcvr;

        return new ReceiveResult(st, k, kid);
        // return new ReceiveResult(newState, k, kid);
    }

    public static class ReceiveResult
    {
        public ReceiverState updatedState;
        public byte[] key;
        public Kid kid;

        public ReceiveResult(ReceiverState updatedState, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.key = key;
            this.kid = kid;
        }
    }



    public static class AddResult {
        public senderState updatedsenderState;
        public ReceiverState newReceiverState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public AddResult(senderState updatedsenderState, ReceiverState newReceiverState, 
                        Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedsenderState = updatedsenderState;
            this.newReceiverState = newReceiverState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

////////////////////////////////////////////77
// add
    public static AddResult procAdd(senderState st, byte[] ad, int uid) throws Exception
    {
        Set<Integer> memR = new HashSet<>(st.memR);
        TreeEK ek = st.ek;
        byte[] ssk = st.ssk;
        byte[] svk = st.svk;
        byte[] tr = st.tr;

        //todo: what if uid is already present
        memR.add(uid);

        UB_KEM.BKAddResult addResult = UB_KEM.add(ek);

        TreeEK newEk = addResult.ek;
        TreeDk newDk = addResult.dk;
        Object cM = addResult.c;

        /////// in paper this is not done here. But Clarified with Paul
        st.memR = memR;
        // st.ek = ek;
        st.ek = newEk;
        st.ssk = ssk;
        st.svk = svk;
        st.tr = tr;

        // byte[] tr1 = new byte[0];
        // st.tr = tr1;

        boolean isNewAddRcvr = true;

        ReceiverState newReceiverState = new ReceiverState(memR, newDk, svk, tr, isNewAddRcvr);
        // ReceiverState newReceiverState = new ReceiverState(memR, newDk, svk, tr);

        EncapsResult encapsResult = encaps(st, newEk, ad, cM);

        return new AddResult(encapsResult.updatedState, newReceiverState, 
                           encapsResult.ciphertext, encapsResult.key, encapsResult.kid);
    }



    public static class RemoveResult
    {
        public senderState updatedState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public RemoveResult(senderState updatedState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedState = updatedState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

////////////////////////////////////////////77
// rmv
    public static RemoveResult procRmv(senderState st, byte[] ad, int uid) throws Exception
    {
        Set<Integer> memR = new HashSet<>(st.memR);
        TreeEK ek = st.ek;
        byte[] ssk = st.ssk;
        byte[] svk = st.svk;
        byte[] tr = st.tr;

        memR.remove(uid);

        UB_KEM.BKRemoveResult removeResult = UB_KEM.rmv(ek, uid);

        TreeEK newEk = removeResult.ek;
        Object cM = removeResult.c;

        /////// in paper this is not done here. But Clarified with Paul
        st.memR = memR;
        st.ek = newEk;
        // st.ek = ek;
        st.ssk = ssk;
        st.svk = svk;
        st.tr = tr;

        EncapsResult encapsResult = encaps(st, newEk, ad, cM);

        return new RemoveResult(encapsResult.updatedState, encapsResult.ciphertext, 
                               encapsResult.key, encapsResult.kid);
    }

    private static byte[] serializeCiphertext(Ciphertext c) {
        return concatAll(c.cPrime, serializeObject(c.cM), c.svkPrime, c.signature);
    }

    private static byte[] serializeObject(Object obj)
    {
        if (obj == null) {
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

    private static byte[] deriveKey(byte[] masterKey, byte[] info)
    {
        byte[] salt = new byte[] {0x01, 0x02, 0x03, 0x04};
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA512Digest());
        HKDFParameters params = new HKDFParameters(masterKey, salt, info);
        hkdf.init(params);

        byte[] derivedKey = new byte[64]; // 64 bytes = 512-bit key
        hkdf.generateBytes(derivedKey, 0, derivedKey.length);
        return derivedKey;
    }

}