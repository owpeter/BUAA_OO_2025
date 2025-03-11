package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;

public class Constant implements Factor {
    private final BigInteger constant;

    public Constant(BigInteger constant) {
        this.constant = constant;
    }

    @Override
    public Mono toMono() {
        return new Mono(this.constant, BigInteger.ZERO);
    }

    @Override
    public Poly toPoly() {
        return null; // ?
    }
}
