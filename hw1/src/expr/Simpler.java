package expr;

import Poly.*;

import java.math.BigInteger;
import java.util.HashMap;

public class Simpler {
    private Poly poly;

    public Simpler(Poly poly) {
        this.poly = poly;
    }

    public Poly simplerPoly() {
        HashMap<BigInteger, Mono> monoMap = new HashMap<>();
        for (Mono mono: this.poly.getMonos()) {
            BigInteger exp = mono.getExp();
            if (monoMap.containsKey(exp)) {
                // 相同幂次的单项式相加
                monoMap.get(exp).addMono(mono);
            } else {
                monoMap.put(exp, mono);
            }
        }

        Poly poly = new Poly();
        for (Mono mono: monoMap.values()) {
            poly.addMono(mono);
        }
        return poly;
    }

}
