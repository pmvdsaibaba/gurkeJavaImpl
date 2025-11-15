package com.gurkePrj.staticUBKem;
import com.gurkePrj.standardKEM.KEM;

import java.util.ArrayList;
import java.util.List;

public class BKGen {

    public static List<byte[]> gen(int n) throws Exception {

        KEM.KeyPair keyPair = KEM.gen(); 
        byte[] ek = keyPair.getEk(); 
        byte[] dk = keyPair.getDk();

        List<byte[]> decapsulationKeys = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            decapsulationKeys.add(dk);  // All dk values are the same
        }

        List<byte[]> result = new ArrayList<>();
        result.add(ek);
        result.addAll(decapsulationKeys);

        return result;
    }
}
