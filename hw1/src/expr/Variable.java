package expr;

import Poly.Mono;
import Poly.Poly;

import java.math.BigInteger;

public class  Variable implements Factor {
    private final String exp;

    public Variable(String exp) {
        this.exp = exp;
    }

    @Override
    public Mono toMono() {
        return new Mono(BigInteger.ONE, new BigInteger(exp));
    }

    @Override
    public Poly toPoly() {
        return null;
    }


}
