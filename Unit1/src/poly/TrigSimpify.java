package poly;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TrigSimpify {

    public static void simplifyAddSinMap(Mono mono, Poly poly, BigInteger exp) {

        if (mono.getSinMap().containsKey(poly)) {
            mono.getSinMap().merge(poly, exp, BigInteger::add);
            return;
        } else if (twiceTrig("sin", mono, poly, exp)) {
            // 二倍角
            mono.setCoe(mono.getCoe().divide(BigInteger.valueOf(2).pow(exp.intValue())));
            Poly twicePoly = poly.twicePoly();
            mono.getSinMap().merge(twicePoly, exp, BigInteger::add);
            mono.getCosMap().remove(poly);
            return;
        }

        // 如果没有找到相同参数的 sin 函数，尝试查找参数为负形式的 sin 函数
        Poly negPoly = poly.negative();
        if (mono.getSinMap().containsKey(negPoly)) {
            // 如果找到参数为负形式的 sin 函数
            // 判断即将加入的三角函数幂次的奇偶性
            if (exp.testBit(0)) {  // 奇数次幂
                // 对于奇数次幂，需要改变系数符号
                mono.setCoe(mono.getCoe().negate());
            }
            // 合并幂次
            mono.getSinMap().merge(negPoly, exp, BigInteger::add);
        } else {
            // 如果没有找到可以合并的项，直接添加
            mono.getSinMap().merge(poly, exp, BigInteger::add);
        }
    }

    public static void simplifyAddCosMap(Mono mono, Poly poly, BigInteger exp) {

        if (mono.getCosMap().containsKey(poly)) {
            mono.getCosMap().merge(poly, exp, BigInteger::add);
            return;
        }
        else if (twiceTrig("cos", mono, poly, exp)) {
            mono.setCoe(mono.getCoe().divide(BigInteger.valueOf(2).pow(exp.intValue())));
            Poly twicePoly = poly.twicePoly();
            mono.getSinMap().merge(twicePoly, exp, BigInteger::add);
            mono.getSinMap().remove(poly);
            return;
        }

        // 如果没有找到相同参数的 cos 函数，尝试查找参数为负形式的 cos 函数
        Poly negPoly = poly.negative();
        if (mono.getCosMap().containsKey(negPoly)) {
            // 由于 cos 是偶函数，无论幂次如何，都不需要改变系数符号
            // 直接合并幂次
            mono.getCosMap().merge(negPoly, exp, BigInteger::add);
        } else {
            // 如果没有找到可以合并的项，直接添加
            mono.getCosMap().merge(poly, exp, BigInteger::add);
        }
    }

    private static boolean twiceTrig(String type, Mono mono, Poly poly, BigInteger exp) {
        BigInteger coe = mono.getCoe();
        if (!Math.isPowerOfTwoNMultipliedByMTwo(coe, exp)) {
            return false;
        }
        else {
            if (type.equals("sin")) {
                return mono.getCosMap().containsKey(poly)
                        && exp.equals(mono.getCosMap().get(poly));
            } else {
                return mono.getSinMap().containsKey(poly)
                        && exp.equals(mono.getSinMap().get(poly));
            }
        }
    }

    public static boolean toOne(Mono mono1, Mono mono2) {
        if (!mono1.getCoe().equals(mono2.getCoe()) || !mono1.getExp().equals(mono2.getExp())) {
            return false;
        }
        HashMap<Poly, BigInteger> newThisSinMap = new HashMap<>();
        HashSet<Poly> thisSinPoly = new HashSet<>();
        for (Map.Entry<Poly, BigInteger> entry : mono1.getSinMap().entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThisSinMap.put(entry.getKey(), entry.getValue());
            } else {
                thisSinPoly.add(entry.getKey());
            }
        }
        HashMap<Poly, BigInteger> newThatSinMap = new HashMap<>();
        HashSet<Poly> thatSinPoly = new HashSet<>();
        for (Map.Entry<Poly, BigInteger> entry : mono2.getSinMap().entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThatSinMap.put(entry.getKey(), entry.getValue());
            } else {
                thatSinPoly.add(entry.getKey());
            }
        }

        HashMap<Poly, BigInteger> newThisCosMap = new HashMap<>();
        HashSet<Poly> thisCosPoly = new HashSet<>();
        for (Map.Entry<Poly, BigInteger> entry : mono1.getCosMap().entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThisCosMap.put(entry.getKey(), entry.getValue());
            } else {
                thisCosPoly.add(entry.getKey());
            }
        }
        HashMap<Poly, BigInteger> newThatCosMap = new HashMap<>();
        HashSet<Poly> thatCosPoly = new HashSet<>();
        for (Map.Entry<Poly, BigInteger> entry : mono2.getCosMap().entrySet()) {
            if (!entry.getValue().equals(BigInteger.valueOf(2))) {
                newThatCosMap.put(entry.getKey(), entry.getValue());
            } else {
                thatCosPoly.add(entry.getKey());
            }
        }

        boolean areMapsEqual = newThisSinMap.equals(newThatSinMap)
            && newThisCosMap.equals(newThatCosMap);
        if (!areMapsEqual) {
            return false;
        }

        boolean isSinCosSwap
            = isSingleElementSwap(thisSinPoly, thatCosPoly, thisCosPoly, thatSinPoly);
        boolean isCosSinSwap
            = isSingleElementSwap(thisCosPoly, thatSinPoly, thisSinPoly, thatCosPoly);
        if (isSinCosSwap && thisSinPoly.equals(thatCosPoly)
            || isCosSinSwap && thisCosPoly.equals(thatSinPoly)) {
            mono1.setSinMap(newThisSinMap);
            mono1.setCosMap(newThisCosMap);
            return true;
        }
        return false;
    }

    private static boolean isSingleElementSwap(
        Set<Poly> thisSet, Set<Poly> thatSet,
        Set<Poly> thisOtherSet, Set<Poly> thatOtherSet) {
        return thisSet.size() == 1 && thatSet.size() == 1
                && thisOtherSet.isEmpty() && thatOtherSet.isEmpty();
    }
}
