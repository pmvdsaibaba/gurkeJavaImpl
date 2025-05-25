package com.myproject.TestDynUBKem;

import com.myproject.dynamicUBKem.UB_KEM;
import com.myproject.Tree.TreeDk;
import com.myproject.Tree.TreeEK;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDynUBKemGen {

    @Test
    public void testUBKemGen() throws Exception {
        int n = 5;  // Number of users / leaves

        UB_KEM ubKem = new UB_KEM();

        // Generate keys using UB_KEM
        UB_KEM.BKGenResult result = ubKem.gen(n);

        // Validate EK
        TreeEK ek = result.ek;
        List<byte[]> pkList = ek.getDataPk();

        assertNotNull(ek, "Encryption key (EK) should not be null");
        System.out.println("Encryption Key (EK) List:");
        // printByteArray(ek.getSerialized()); // Assuming you have a serialization method for TreeEK

        for (byte[] pk : pkList) {
            printByteArray(pk);
        }

        // Validate DKs
        List<TreeDk> dkList = result.dkList;
        assertNotNull(dkList, "Decryption key list should not be null");
        assertEquals(n, dkList.size(), "Should have exactly n decryption keys");

        List<byte[]> skList;

        for (int i = 0; i < dkList.size(); i++) {
            TreeDk dk = dkList.get(i);
            skList = dk.getDataSk();
            assertNotNull(dk, "Decryption key should not be null for user " + (i + 1));
            System.out.println("Decapsulation Key (DK" + (i + 1) + ") List:");
            // printByteArray(dk.getSerialized()); // Assuming TreeDk has a getSerialized() method
            for (byte[] sk : skList) {
                printByteArray(sk);
            }
        }
    }

    // Utility to print byte arrays in hex format
    public static void printByteArray(byte[] byteArray) {
        if (byteArray == null) {
            System.out.println("null");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X", b));
        }
        System.out.println(sb.toString());
    }
}
