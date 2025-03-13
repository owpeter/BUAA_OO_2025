package expr;

import poly.Poly;
import poly.Mono;
import poly.Multiply;

import java.math.BigInteger;
import java.util.ArrayList;

// 因子 | 项 * 因子

public class Term implements Factor{
    private ArrayList<Factor> factors;
    private final boolean sign;

    public Term(boolean sign) {
        this.sign = sign;
        this.factors = new ArrayList<Factor>();
    }

    public Term(boolean sign, Factor factor) {
        this.sign = sign;
        this.factors = new ArrayList<Factor>();
        this.factors.add(factor);
    }

    public Term(boolean sign, ArrayList<Factor> factors) {
        this.sign = sign;
        this.factors = factors;
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public ArrayList<Factor> getFactors() {
        return this.factors;
    }

    public boolean getSign() {
        return this.sign;
    }

    public Poly toPoly() {
        Poly poly = new Poly();
        if (this.sign) {
            poly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        } else {
            poly.addMono(new Mono(BigInteger.ONE.negate(), BigInteger.ZERO));
        }

        for (Factor factor : factors) {
            if (factor instanceof Variable
                || factor instanceof Constant
                || factor instanceof TrigonometricFunction) {
                // 变量因子、常数因子、三角函数因子都是单项式
                Multiply.polyMultiplyMono(poly, factor.toMono());
            } else {
                // 表达式因子
                Multiply.polyMultiply(poly, factor.toPoly());
            }
        }
        return poly;
    }

    @Override
    public Mono toMono() {
        return null;
    }
}
