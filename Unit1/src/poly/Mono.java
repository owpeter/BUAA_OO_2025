package poly;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Mono {
    private BigInteger coe;
    private BigInteger exp;
    private HashMap<Poly, BigInteger> sinMap;
    private HashMap<Poly, BigInteger> cosMap;

    public Mono(BigInteger coe, BigInteger exp) {
        this.coe = coe;
        this.exp = exp;
        this.sinMap = new HashMap<>();
        this.cosMap = new HashMap<>();
    }

    public BigInteger getExp() {
        return this.exp;
    }

    public BigInteger getCoe() {
        return this.coe;
    }

    public Map<Poly, BigInteger> getSinMap() {
        return this.sinMap;
    }

    public Map<Poly, BigInteger> getCosMap() {
        return this.cosMap;
    }

    public void setCoe(BigInteger coe) {
        this.coe = coe;
    }

    public void setExp(BigInteger exp) {
        this.exp = exp;
    }

    public void addSinTrig(Poly poly, BigInteger exp) {
        this.sinMap.merge(poly, exp, BigInteger::add);
    }

    public void addCosTrig(Poly poly, BigInteger exp) {
        this.cosMap.merge(poly, exp, BigInteger::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mono mono = (Mono) o;
        return this.exp.equals(mono.exp) &&
               this.sinMap.equals(mono.sinMap) &&
               this.cosMap.equals(mono.cosMap);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + exp.hashCode();
        hash = 31 * hash + sinMap.hashCode();
        hash = 31 * hash + cosMap.hashCode();
        return hash;
    }

    public Mono copy() {
        return copy(true);
    }

    public Mono copy(boolean withTrig) {
        Mono newMono = new Mono(this.coe, this.exp);
        if (withTrig) {
            for (Map.Entry<Poly, BigInteger> entry : this.sinMap.entrySet()) {
                newMono.sinMap.put(entry.getKey().copy(), entry.getValue());
            }
            for (Map.Entry<Poly, BigInteger> entry : this.cosMap.entrySet()) {
                newMono.cosMap.put(entry.getKey().copy(), entry.getValue());
            }
        }
        return newMono;
    }

    public boolean isZero() {
        return this.coe.equals(BigInteger.ZERO);
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
}
