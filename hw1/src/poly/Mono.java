package poly;

import java.math.BigInteger;

public class Mono {
    private BigInteger coe;
    private BigInteger exp;

    public Mono(BigInteger coe, BigInteger exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public BigInteger getExp() {
        return this.exp;
    }

    public BigInteger getCoe() {
        return this.coe;
    }

    public void addMono(Mono mono) {
        this.coe = this.coe.add(mono.coe);
    }

    public void multiMono(Mono mono2) {
        this.coe = this.coe.multiply(mono2.coe);
        this.exp = this.exp.add(mono2.exp);
    }

    public Mono multiMono(Mono mono1, Mono mono2) {
        BigInteger newCoe = mono1.coe.multiply(mono2.coe);
        BigInteger newExp = mono1.exp.add(mono2.exp);
        return new Mono(newCoe, newExp);
    }

    public Mono copy() {
        return new Mono(this.coe, this.exp);
    }

    public String toString(boolean isHead) {
        StringBuilder sb = new StringBuilder();

        if (this.coe.equals(BigInteger.ZERO)) {
            // 0*x^n
            return "";
        }

        // coe != 0
        if (!isHead) {
            if (this.coe.compareTo(BigInteger.ZERO) > 0) {
                sb.append("+");
            }
            // 负数是否需要符号- ？
        }

        if (this.exp.equals(BigInteger.ZERO)) {
            // a
            sb.append(this.coe);
        } else if (this.exp.equals(BigInteger.ONE)) {
            // a*x
            sb = coeBuilder(sb);
        }
        else {
            sb = coeBuilder(sb);
            sb.append("^");
            sb.append(this.exp);
        }

        return sb.toString();
    }

    private StringBuilder coeBuilder(StringBuilder sb) {
        if (this.coe.equals(BigInteger.ONE)) {
            // x^n
            sb.append("x");
        } else if (this.coe.equals(BigInteger.ONE.negate())) {
            // -x^n
            sb.append("-x");
        } else {
            sb.append(this.coe);
            sb.append("*x");
        }

        return sb;
    }

    public boolean isZero() {
        return this.coe.equals(BigInteger.ZERO);
    }
}
