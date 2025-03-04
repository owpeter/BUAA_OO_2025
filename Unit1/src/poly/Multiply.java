package poly;

import java.math.BigInteger;
import java.util.Map;

public class Multiply {
    public static void monoMultiply(Mono mono1, Mono mono2) {
        // 1. 系数相乘
        mono1.setCoe(mono1.getCoe().multiply(mono2.getCoe()));
        // 2. 指数相加
        mono1.setExp(mono1.getExp().add(mono2.getExp()));
        // 3. 合并三角函数
        for (Map.Entry<Poly, BigInteger> entry : mono2.getSinMap().entrySet()) {
            mono1.addSinTrig(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Poly, BigInteger> entry : mono2.getCosMap().entrySet()) {
            mono1.addCosTrig(entry.getKey(), entry.getValue());
        }
    }

    public static Mono monoMultiplyNew(Mono mono1, Mono mono2) {
        // 创建不带三角函数的新单项式
        Mono result = mono1.copy(true);
        // 使用monoMultiply进行乘法运算
        monoMultiply(result, mono2);
        return result;
    }

    public static void polyMultiplyMono(Poly poly, Mono mono) {
        for (Mono thisMono : poly.getMonos()) {
            monoMultiply(thisMono, mono);
        }
    }

    public static void polyMultiply(Poly poly1, Poly poly2) {
        Poly result = new Poly();
        for (Mono mono1 : poly1.getMonos()) {
            for (Mono mono2 : poly2.getMonos()) {
                Mono newMono = monoMultiplyNew(mono1, mono2);
                result.addMono(newMono);
            }
        }
        poly1.setMonos(result.getMonos());
    }
} 