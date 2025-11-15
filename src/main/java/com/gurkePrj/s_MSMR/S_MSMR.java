package com.myproject.s_MSMR;

import com.myproject.staticUBKem.BKGen;
import com.myproject.signatureScheme.SignatureScheme;

import com.myproject.staticUBKem.BKEnc;
import com.myproject.staticUBKem.BKEnc.EncapsulationReturn;
import com.myproject.staticUBKem.BKEnc.EncapsulationResult;
import com.myproject.staticUBKem.BKFin;
import com.myproject.staticUBKem.BKFin.FinResult;
import com.myproject.staticUBKem.BKDec;
import com.myproject.staticUBKem.BKDec.DecResult;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

public class S_MSMR {

    public static class SenderState {
        public int id;
        public int nS;
        public int nR;
        public byte[] ek;
        public byte[] ssk;

        public SenderState(int id, int nS, int nR, byte[] ek, byte[] ssk) {
            this.id = id;
            this.nS = nS;
            this.nR = nR;
            this.ek = ek;
            this.ssk = ssk;
        }
    }

    public static class ReceiverState {
        public int nS;
        public int nR;
        public List<ReceiverInfo> senderInfoList;

        public ReceiverState(int nS, int nR, List<ReceiverInfo> senderInfoList) {
            this.nS = nS;
            this.nR = nR;
            this.senderInfoList = senderInfoList;
        }

        public static class ReceiverInfo {
            public byte[] dk;
            public byte[] svk;

            public ReceiverInfo(byte[] dk, byte[] svk) {
                this.dk = dk;
                this.svk = svk;
            }
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

    public static InitResult procInit(int nS, int nR) throws Exception {
        List<SenderState> senderStates = new ArrayList<>();
        List<List<ReceiverState.ReceiverInfo>> Sj = new ArrayList<>();

        for (int j = 0; j < nR; j++) {
            Sj.add(new ArrayList<>());
        }

        for (int i = 0; i < nS; i++) {

            List<byte[]> bkResult = BKGen.gen(nR);
            byte[] ek = bkResult.get(0);
            List<byte[]> dks = bkResult.subList(1, bkResult.size());

            SignatureScheme.KeyPair sigKeyPair = SignatureScheme.gen();
            byte[] svk = sigKeyPair.getVk();
            byte[] ssk = sigKeyPair.getSk();

            SenderState senderState = new SenderState(i, nS, nR, ek, ssk);
            senderStates.add(senderState);

            for (int j = 0; j < nR; j++) {
                ReceiverState.ReceiverInfo info = new ReceiverState.ReceiverInfo(dks.get(j), svk);
                Sj.get(j).add(info);
            }
        }

        List<ReceiverState> receiverStates = new ArrayList<>();
        for (int j = 0; j < nR; j++) {
            ReceiverState receiverState = new ReceiverState(nS, nR, Sj.get(j));
            receiverStates.add(receiverState);
        }

        return new InitResult(senderStates, receiverStates);
    }


    // Implements: procSnd
    public static class Ciphertext {
        public int senderId;
        public byte[] cPrime;
        public byte[] svkPrime;
        public byte[] signature;

        public Ciphertext(int senderId, byte[] cPrime, byte[] svkPrime, byte[] signature) {
            this.senderId = senderId;
            this.cPrime = cPrime;
            this.svkPrime = svkPrime;
            this.signature = signature;
        }
    }

    public static class ProcSndResult {
        public SenderState updatedSenderState;
        public Ciphertext ciphertext;
        public byte[] key;
        public Kid kid;

        public ProcSndResult(SenderState updatedSenderState, Ciphertext ciphertext, byte[] key, Kid kid) {
            this.updatedSenderState = updatedSenderState;
            this.ciphertext = ciphertext;
            this.key = key;
            this.kid = kid;
        }
    }

    public static class Kid {
        public byte[] id;
        public int senderId;
        public int nS;
        public int nR;

        public Kid(byte[] id, int senderId, int nS, int nR) {
            this.id = id;
            this.senderId = senderId;
            this.nS = nS;
            this.nR = nR;
        }
    }

    public static ProcSndResult procSnd(SenderState st, byte[] ad) throws Exception {
        int i = st.id;
        int nS = st.nS;
        int nR = st.nR;
        byte[] ek = st.ek;
        byte[] ssk = st.ssk;

        EncapsulationReturn encRet = BKEnc.enc(ek);
        EncapsulationResult u = encRet.getU();
        byte[] cPrime = encRet.getC();

        SignatureScheme.KeyPair newSigKeys = SignatureScheme.gen();
        byte[] svkPrime = newSigKeys.getVk();
        byte[] sskPrime = newSigKeys.getSk();


        byte[] toSign = concatAll(ad, intToBytes(i), cPrime, svkPrime);
        byte[] sigma = SignatureScheme.sgn(ssk, toSign);


        Ciphertext ciphertext = new Ciphertext(i, cPrime, svkPrime, sigma);

        byte[] cConcat = concatAll(ad, intToBytes(i), cPrime, svkPrime, sigma);
        FinResult finResult = BKFin.fin(u, cConcat);
        byte[] newEk = finResult.getEk();
        byte[] k = finResult.getK();

        // Kid kid = new Kid(finResult.getEk(), i, nS, nR); // Assuming ek = id
        Kid kid = new Kid(finResult.getK(), i, nS, nR); // Assuming k = id

        SenderState updatedSt = new SenderState(i, nS, nR, newEk, sskPrime);

        return new ProcSndResult(updatedSt, ciphertext, k, kid);
    }

    // Utility to concatenate multiple byte arrays
    private static byte[] concatAll(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    // Utility to convert int to byte[]
    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value >>> 24),
            (byte) (value >>> 16),
            (byte) (value >>> 8),
            (byte) value
        };
    }

    public static class ProcRcvResult {
        public ReceiverState updatedState;
        public byte[] k;
        public Kid kid;

        public ProcRcvResult(ReceiverState updatedState, byte[] k, Kid kid) {
            this.updatedState = updatedState;
            this.k = k;
            this.kid = kid;
        }
    }

    public static class ProcRcvFailure {
        public ReceiverState updatedState;

        public ProcRcvFailure(ReceiverState updatedState) {
            this.updatedState = updatedState;
        }
    }

    // Implements: procRcv
    public static Object procRcv(ReceiverState st, byte[] ad, Ciphertext c) throws Exception {
        int nS = st.nS;
        int nR = st.nR;
        List<ReceiverState.ReceiverInfo> senderList = new ArrayList<>(st.senderInfoList);

        int i = c.senderId;
        byte[] cPrime = c.cPrime;
        byte[] svkPrime = c.svkPrime;
        byte[] sigma = c.signature;

        // Get (dk, svk)
        ReceiverState.ReceiverInfo info = senderList.get(i);
        byte[] dk = info.dk;
        byte[] svk = info.svk;

        // Verify signature
        byte[] signedMessage = concatAll(ad, intToBytes(i), cPrime, svkPrime);
        if (!SignatureScheme.vfy(svk, signedMessage, sigma)) {
            return new ProcRcvFailure(st);  // Return (st, ⊥)
        }

        // (dk, (k, id)) ← BK.dec(dk, (ad, c), c′)
        byte[] fullC = concatAll(ad, intToBytes(i), cPrime, svkPrime, sigma);
        DecResult decResult = BKDec.dec(dk, fullC, cPrime);

        byte[] newDk = decResult.getDk();
        byte[] k = decResult.getK();

        Kid kid = new Kid(k, i, nS, nR);

        senderList.set(i, new ReceiverState.ReceiverInfo(newDk, svkPrime));

        ReceiverState newState = new ReceiverState(nS, nR, senderList);

        return new ProcRcvResult(newState, k, kid);
    }

    public static int procIni(Kid kid) {
        return kid.senderId;
    }

    public static int procMemS(Kid kid) {
        return kid.nS;
    }

    public static int procMemR(Kid kid) {
        return kid.nR;
    }
}
