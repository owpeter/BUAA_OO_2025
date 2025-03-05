package poly;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TrigSimpify {

//    public static void simpify(Mono mono, Poly poly, BigInteger exp) {
//
//    }

    public static void simplifyAddSinMap(Mono mono, Poly poly, BigInteger exp) {

        if (mono.getSinMap().containsKey(poly)) {
            mono.getSinMap().merge(poly, exp, BigInteger::add);
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

//    public static void simplifySign()

//    public static void simplifySinCosSquare(Poly poly, Mono mono) {
//        for (Mono thisMono : poly.getMonos()) {
//
//        }
//    }

//    public static void simplifyContrastTrig (Mono mono1, Mono mono2) {
//        Map<Poly, BigInteger> sinMap1 = mono1.getSinMap();
//        Map<Poly, BigInteger> sinMap2 = mono2.getSinMap();
//        for (Map.Entry<Poly, BigInteger> entry : sinMap1.entrySet()) {
//            // 如果 sinMap2 中有相同幂次的三角函数 && 幂次为奇数
//            if (sinMap2.containsKey(entry.getKey()) && entry.getValue().testBit(0)) {
//                // 如果
//            }
//        }
//            if (sinMap2.containsValue()) {
//        }
//    }
}
