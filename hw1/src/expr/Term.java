package expr;

import Poly.*;

import java.math.BigInteger;
import java.util.ArrayList;

// 因子 | 项 * 因子

public class Term {
    private ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<Factor>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        poly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        for (Factor factor : factors) {
            if (factor instanceof Variable) {
                // * 幂函数因子
                poly.MultiMono(factor.toMono());
            } else if (factor instanceof Constant) {
                // * 常数因子
                poly.MultiMono(factor.toMono());
            } else {
                // * 表达式因子
                poly.MultiPoly(factor.toPoly());
            }
        }
        return poly;
    }
}
