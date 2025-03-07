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
        // TODO: bugs
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
        if (!Math.isPowerOfTwoNMultipliedByTwoM(coe, exp)) {
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
}
