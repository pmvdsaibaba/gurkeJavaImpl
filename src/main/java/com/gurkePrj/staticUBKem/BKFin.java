package com.myproject.staticUBKem;
import com.myproject.standardKEM.KEM;
import com.myproject.RandomOracle.RandomOracle;

import com.myproject.staticUBKem.BKEnc.EncapsulationResult;

import java.util.ArrayList;
import java.util.List;

public class BKFin {

    public static FinResult fin(EncapsulationResult u, byte[] ad) throws Exception {
        byte[] kPrime = u.getK();
        byte[] cPrime = u.getC();

        RandomOracle.RandomOracleResult oracleResult = RandomOracle.H(cPrime, kPrime, ad);
        byte[] s = oracleResult.getS(); // Seed (s)
        byte[] k = oracleResult.getK(); // Key (k)

        KEM.KeyPair keyPair = KEM.gen(s);

        byte[] new_ek = keyPair.getEk();
        byte[] new_dk = keyPair.getDk();

        return new FinResult(new_ek, k); // Return encapsulation key (ek) and key (k)
    }


    public static class FinResult {
        byte[] ek;
        byte[] k;

        public FinResult(byte[] ek, byte[] k) {
            this.ek = ek;
            this.k = k;
        }

        public byte[] getEk() {
            return ek;
        }

        public byte[] getK() {
            return k;
        }
    }
}
