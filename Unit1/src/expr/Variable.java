package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;

public class  Variable implements Factor {
    private final BigInteger exp;

    public Variable(BigInteger exp) {
        this.exp = exp;
    }

    public BigInteger getExp() {
        return exp;
    }

    @Override
    public Mono toMono() {
        return new Mono(BigInteger.ONE, this.exp);
    }

    @Override
    public Poly toPoly() {
        return null;
    }
}
