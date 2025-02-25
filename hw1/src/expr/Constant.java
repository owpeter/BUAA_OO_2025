package expr;

import Poly.Mono;
import Poly.Poly;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

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
