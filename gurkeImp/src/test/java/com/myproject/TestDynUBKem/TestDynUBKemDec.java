package com.myproject.TestDynUBKem;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;


public class TestDynUBKemDec {

    @Test
    public void testUBKemDec() throws Exception {

        int n = 5;  // Number of users / leaves

        UB_KEM ubKem = new UB_KEM();

        // Generate keys using UB_KEM
        UB_KEM.BKGenResult result = ubKem.gen(n);

        TreeEK ek = result.ek;
        List<byte[]> pkList = ek.getDataPk();

        assertNotNull(ek, "Encryption key (EK) should not be null");
        // System.out.println("Encryption Key (EK) List:");


        // for (byte[] pk : pkList) {
        //     printByteArray(pk);
        // }

        // Validate DKs
        List<TreeDk> dkList = result.dkList;
        assertNotNull(dkList, "Decryption key list should not be null");
        assertEquals(n, dkList.size(), "Should have exactly n decryption keys");

        List<byte[]> skList;

        for (int i = 0; i < dkList.size(); i++) {
            TreeDk dk = dkList.get(i);
            skList = dk.getDataSk();

            assertNotNull(dk, "Decryption key should not be null for user " + (i + 1));
            // System.out.println("Decapsulation Key (DK" + (i + 1) + ") List:");
            // for (byte[] sk : skList) {
            //     printByteArray(sk);
            // }
        }


        TreeDk dk = result.dkList.get(0);

        UB_KEM.BKEncResult encResult = ubKem.enc(ek);



        byte[] ad = new byte[32];
        Arrays.fill(ad, (byte) 0xEF);

        UB_KEM.FinResult finResult = ubKem.fin(encResult.u, ad);


        UB_KEM.DecResult decResult = ubKem.dec(dk, ad, encResult.c);

        // assertArrayEquals(decResult.k, finResult.k, "Shared keys should match");


        System.out.println("ubKem.dec and ubKem.fin succeeded. Shared key (k):");
        printByteArray(decResult.k);
        printByteArray(finResult.k);


        dk = decResult.dk;
        encResult = ubKem.enc(finResult.ek);
        finResult = ubKem.fin(encResult.u, ad);
        decResult = ubKem.dec(dk, ad, encResult.c);

        System.out.println("ubKem.dec and ubKem.fin succeeded. Shared key (k):");
        printByteArray(decResult.k);
        printByteArray(finResult.k);

        dk = decResult.dk;
        encResult = ubKem.enc(finResult.ek);
        finResult = ubKem.fin(encResult.u, ad);
        decResult = ubKem.dec(dk, ad, encResult.c);

        System.out.println("ubKem.dec and ubKem.fin succeeded. Shared key (k):");
        printByteArray(decResult.k);
        printByteArray(finResult.k);

        dk = decResult.dk;
        encResult = ubKem.enc(finResult.ek);
        finResult = ubKem.fin(encResult.u, ad);
        decResult = ubKem.dec(dk, ad, encResult.c);

        System.out.println("ubKem.dec and ubKem.fin succeeded. Shared key (k):");
        printByteArray(decResult.k);
        printByteArray(finResult.k);
    }

    // Utility method to print byte arrays in a readable format
    private void printByteArray(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));  // Convert to hexadecimal representation
        }
        System.out.println(sb.toString());
    }
}
