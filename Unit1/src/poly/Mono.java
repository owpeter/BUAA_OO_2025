package poly;

import processString.ToString;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
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

    public boolean toOne(Mono mono) { // key 三角函数内因子 -> value 三角函数幂次
        // 如果两个Mono除2次幂均相同，且sin的二次幂的poly和cos二次幂的poly相同或cos二次幂的poly和sin的二次幂的poly相同，返回1
        if (!this.coe.equals(mono.coe) || !this.exp.equals(mono.exp)) {
            return false;
        }

        // 获得二次幂的Poly
        HashSet<Poly> thisSinPoly = new HashSet<>();
        HashSet<Poly> thatSinPoly = new HashSet<>();
        HashSet<Poly> thisCosPoly = new HashSet<>();
        HashSet<Poly> thatCosPoly = new HashSet<>();

        HashMap<Poly, BigInteger> newThisSinMap = new HashMap<>();
        // 删除 sinMap 和 cosMap 中值为2的键值对
        for (Map.Entry<Poly, BigInteger> entry : this.sinMap.entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThisSinMap.put(entry.getKey(), entry.getValue());
            } else {
//                thisSinPoly = entry.getKey();
                thisSinPoly.add(entry.getKey());
            }
        }
        HashMap<Poly, BigInteger> newThatSinMap = new HashMap<>();
        for (Map.Entry<Poly, BigInteger> entry : mono.sinMap.entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThatSinMap.put(entry.getKey(), entry.getValue());
            } else {
//                thatSinPoly = entry.getKey();
                thatSinPoly.add(entry.getKey());
            }
        }
        if (!newThisSinMap.equals(newThatSinMap)) {
            return false;
        }


        HashMap<Poly, BigInteger> newThisCosMap = new HashMap<>();
        for (Map.Entry<Poly, BigInteger> entry : this.cosMap.entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThisCosMap.put(entry.getKey(), entry.getValue());
            } else {
//                thisCosPoly = entry.getKey();
                thisCosPoly.add(entry.getKey());
            }
        }
        HashMap<Poly, BigInteger> newThatCosMap = new HashMap<>();
        for (Map.Entry<Poly, BigInteger> entry : mono.cosMap.entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThatCosMap.put(entry.getKey(), entry.getValue());
            } else {
//                thatCosPoly = entry.getKey();
                thatCosPoly.add(entry.getKey());
            }
        }
        if (!newThisSinMap.equals(newThatSinMap) || !newThisCosMap.equals(newThatCosMap)) {
            return false;
        }

//        if (thisSinPoly != null && thatCosPoly != null && thisCosPoly == null && thatSinPoly == null) {
//            return thisSinPoly.equals(thatCosPoly);
//        }
        if (thisSinPoly.size() == 1 && thatCosPoly.size() == 1
                && thisCosPoly.isEmpty() && thatSinPoly.isEmpty()) {
            return thisSinPoly.equals(thatCosPoly);
        }

//        if (thisCosPoly != null && thatSinPoly != null && thisSinPoly == null && thatCosPoly == null) {
//            return thisCosPoly.equals(thatSinPoly);
//        }

        if (thisCosPoly.size() == 1 && thatSinPoly.size() == 1
                && thisSinPoly.isEmpty() && thatCosPoly.isEmpty()) {
            return thisCosPoly.equals(thatSinPoly);
        }

        return false;

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

    public void twiceMono() {
        this.coe = this.coe.multiply(BigInteger.valueOf(2));
    }

    public String toString(boolean isHead) {
        return ToString.monoToString(this, isHead);
    }
}
