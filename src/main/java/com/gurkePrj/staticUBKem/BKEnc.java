package com.gurkePrj.staticUBKem;
import com.gurkePrj.standardKEM.KEM;

public class BKEnc {

    public static EncapsulationReturn enc(byte[] ek) throws Exception
    {
        KEM.EncapsulationResult result = KEM.enc(ek);

        byte[] k = result.getK();
        byte[] c = result.getC();

        EncapsulationResult u = new EncapsulationResult(k, c);

        return new EncapsulationReturn(u, c);
    }

    public static class EncapsulationResult{
        byte[] k;
        byte[] c;

        public EncapsulationResult(byte[] k, byte[] c) {
            this.k = k;
            this.c = c;
        }

        public byte[] getK() {
            return k;
        }

        public byte[] getC() {
            return c;
        }
    }

    public static class EncapsulationReturn {
        EncapsulationResult u;
        byte[] c;

        public EncapsulationReturn(EncapsulationResult u, byte[] c) {
            this.u = u;
            this.c = c;
        }

        public EncapsulationResult getU() {
            return u;
        }

        public byte[] getC() {
            return c;
        }
    }
}
