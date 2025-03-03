package expr;

import poly.Poly;
import poly.Mono;

import java.math.BigInteger;
import java.util.ArrayList;

// 因子 | 项 * 因子

public class Term {
    private ArrayList<Factor> factors;
    private final boolean sign;

    public Term(boolean sign) {
        this.sign = sign;
        this.factors = new ArrayList<Factor>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        if (this.sign) {
            poly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        } else {
            poly.addMono(new Mono(BigInteger.ONE.negate(), BigInteger.ZERO));
        }

        for (Factor factor : factors) {
            if (factor instanceof Variable) {
                // * 幂函数因子
                poly.multiMono(factor.toMono());
            } else if (factor instanceof Constant) {
                // * 常数因子
                poly.multiMono(factor.toMono());
            } else {
                // * 表达式因子
                poly.multiPoly(factor.toPoly());
            }
        }
        return poly;
    }
}
