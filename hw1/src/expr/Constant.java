package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;

public class Constant implements Factor {
    private final String constant;

    public Constant(String constant) {
        this.constant = constant;
    }

    @Override
    public Mono toMono() {
        return new Mono(new BigInteger(this.constant), BigInteger.ZERO);
    }

    @Override
    public Poly toPoly() {
        return null; // ?
    }
}
