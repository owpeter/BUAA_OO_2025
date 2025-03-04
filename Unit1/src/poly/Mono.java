package poly;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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
        // 首先尝试直接查找是否存在相同参数的 sin 函数
        if (this.sinMap.containsKey(poly)) {
            this.sinMap.merge(poly, exp, BigInteger::add);
            return;
        }
        
        // 如果没有找到相同参数的 sin 函数，尝试查找参数为负形式的 sin 函数
        Poly negPoly = poly.negative();
        if (this.sinMap.containsKey(negPoly)) {
            // 如果找到参数为负形式的 sin 函数
            // 判断即将加入的三角函数幂次的奇偶性
            if (exp.testBit(0)) {  // 奇数次幂
                // 对于奇数次幂，需要改变系数符号
                this.setCoe(this.getCoe().negate());
            }
            // 合并幂次
            this.sinMap.merge(negPoly, exp, BigInteger::add);
        } else {
            // 如果没有找到可以合并的项，直接添加
            this.sinMap.merge(poly, exp, BigInteger::add);
        }
    }

    public void addCosTrig(Poly poly, BigInteger exp) {
        // 首先尝试直接查找是否存在相同参数的 cos 函数
        if (this.cosMap.containsKey(poly)) {
            this.cosMap.merge(poly, exp, BigInteger::add);
            return;
        }
        
        // 如果没有找到相同参数的 cos 函数，尝试查找参数为负形式的 cos 函数
        Poly negPoly = poly.negative();
        if (this.cosMap.containsKey(negPoly)) {
            // 由于 cos 是偶函数，无论幂次如何，都不需要改变系数符号
            // 直接合并幂次
            this.cosMap.merge(negPoly, exp, BigInteger::add);
        } else {
            // 如果没有找到可以合并的项，直接添加
            this.cosMap.merge(poly, exp, BigInteger::add);
        }
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
        return ToString.monoToString(this, isHead);
    }
}
