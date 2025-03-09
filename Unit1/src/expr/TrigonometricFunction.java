package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;

public class TrigonometricFunction implements Factor {
    private final String type;  // "sin" or "cos"
    private final Factor factor;
    private final BigInteger exponent;

    public TrigonometricFunction(String type, Factor factor, BigInteger exponent) {
        this.type = type;
        this.factor = factor;
        this.exponent = exponent;
    }

    @Override
    public Poly toPoly() {
        // 三角函数不应该直接转换为多项式
        return null;
    }

    @Override
    public Mono toMono() {
        // 如果指数为0，则返回常数1（系数为1，指数为0的单项式）
        if (this.exponent.equals(BigInteger.ZERO)) {
            return new Mono(BigInteger.ONE, BigInteger.ZERO);
        }
        // 1. 获取内部因子的形式（单项式或多项式）
        Poly innerPoly;
        Mono innerMono = factor.toMono();
        if (innerMono != null) {
            // 如果能转换为单项式，创建新的多项式存储它
            innerPoly = new Poly();
            innerPoly.addMono(innerMono);
        } else {
            // 如果不能转换为单项式，尝试直接获取多项式形式
            innerPoly = factor.toPoly();
            if (innerPoly == null) {
                return null;  // 如果都无法转换，返回null
            }
        }
        if (innerPoly.allZero()) {
            if (type.equals("sin")) {
                return new Mono(BigInteger.ZERO, BigInteger.ZERO);
            } else if (type.equals("cos")) {
                return new Mono(BigInteger.ONE, BigInteger.ZERO);
            } else {
                return null;
            }
        }
        // 2. 创建新的单项式，系数为1，指数为0
        Mono result = new Mono(BigInteger.ONE, BigInteger.ZERO);

        // 指数是奇数且sin且取反更短，取反, 系数取反
        Poly negPoly = innerPoly.negative();

        if (type.equals("sin") && exponent.testBit(0)) {
            if (negPoly.toString().length() < innerPoly.toString().length()) {
                innerPoly = negPoly;
                result.setCoe(BigInteger.ONE.negate());
            }
        } else if (type.equals("cos") || (type.equals("sin") && !exponent.testBit(0))) {
            // 指数是偶数且sin 或 cos，取反更短，取反，系数不变
            if (negPoly.toString().length() < innerPoly.toString().length()) {
                innerPoly = negPoly;
            }
        }



        // 3. 根据函数类型，添加对应的三角函数信息
        if (type.equals("sin")) {
            result.addSinTrig(innerPoly, exponent);
        } else {  // cos
            result.addCosTrig(innerPoly, exponent);
        }

        return result;
    }
} 