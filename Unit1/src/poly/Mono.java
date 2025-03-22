package poly;

import procstring.ToString;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Mono {
    private BigInteger coe;
    private BigInteger exp;
    private HashMap<Poly, BigInteger> sinMap;   // 三角函数内因子 -> 三角函数幂次
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

    public void setSinMap(HashMap<Poly, BigInteger> sinMap) {
        this.sinMap = sinMap;
    }

    public void setCosMap(HashMap<Poly, BigInteger> cosMap) {
        this.cosMap = cosMap;
    }

    public void addSinTrig(Poly poly, BigInteger exp) {
        // poly是三角函数内表达式
        TrigSimpify.simplifyAddSinMap(this, poly, exp);
    }

    public void addCosTrig(Poly poly, BigInteger exp) {
        TrigSimpify.simplifyAddCosMap(this, poly, exp);
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

    public boolean noTrig() {
        return this.sinMap.isEmpty() && this.cosMap.isEmpty();
    }

    public void twiceMono() {
        this.coe = this.coe.multiply(BigInteger.valueOf(2));
    }

    public String toString(boolean isHead) {
        return ToString.monoToString(this, isHead);
    }
}