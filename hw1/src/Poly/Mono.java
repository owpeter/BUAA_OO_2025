package Poly;

import java.math.BigInteger;

public class Mono {
    private BigInteger coe;
    private BigInteger exp;
    // 符号问题？？

    public Mono(BigInteger coe, BigInteger exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public void MultiMono(Mono mono2) {
        this.coe = this.coe.multiply(mono2.coe);
        this.exp = this.exp.add(mono2.exp);
    }

    public Mono MultiMono(Mono mono1, Mono mono2) {
        BigInteger newCoe = mono1.coe.multiply(mono2.coe);
        BigInteger newExp = mono1.exp.add(mono2.exp);
        return new Mono(newCoe, newExp);
    }
}
