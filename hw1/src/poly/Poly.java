package poly;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class Poly {
    private ArrayList<Mono> monos;

    public ArrayList<Mono> getMonos() {
        return this.monos;
    }

    public Poly() {
        this.monos = new ArrayList<Mono>();
    }

    public void addMono(Mono mono) {
        this.monos.add(mono);

        // simpler
        HashMap<BigInteger, Mono> monoMap = new HashMap<>();
        for (Mono thisMono : this.monos) {
            BigInteger exp = thisMono.getExp();
            if (monoMap.containsKey(exp)) {
                monoMap.get(exp).addMono(thisMono);
            } else {
                monoMap.put(exp, thisMono);
            }
        }

        this.monos.clear();
        this.monos.addAll(monoMap.values());

        int n = this.monos.size();
        for (int i = 0; i < n; i++) {
            if (this.monos.get(i).getCoe().compareTo(BigInteger.ZERO) > 0) {
                Mono temp = monos.get(i);
                monos.set(i, monos.get(0));
                monos.set(0, temp);
            }
        }
    }

    public void addPoly(Poly newPoly) {
        for (Mono mono : newPoly.getMonos()) {
            this.addMono(mono);
        }
    }

    public void multiMono(Mono mono) {
        // (monos) * mono = mono1*mono + mono2 * mono ...
        for (Mono thisMono : this.monos) {
            thisMono.multiMono(mono);
        }
    }

    public void multiPoly(Poly newPoly) {
        Poly poly = new Poly();
        for (Mono mono1 : this.monos) {
            for (Mono mono2 : newPoly.getMonos()) {
                Mono newMono = mono1.multiMono(mono1, mono2);
                poly.addMono(newMono);
            }
        }
        this.monos = poly.getMonos();
    }

    public void powPoly(String stringExp) {
        BigInteger exp = new BigInteger(stringExp);

        Poly oldPoly = this.copy();
        while (exp.compareTo(BigInteger.ONE) > 0) {
            this.multiPoly(oldPoly);
            exp = exp.subtract(BigInteger.ONE);
        }

    }

    public Poly copy() {
        Poly poly = new Poly();
        for (Mono mono : monos) {
            poly.addMono(mono.copy());
        }
        return poly;
    }

    public String toString() {
        if (this.allZero()) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        boolean isHead = true;
        for (Mono mono : this.monos) {
            if (isHead) {
                if (!mono.isZero()) {
                    // 不是前导0
                    sb.append(mono.toString(isHead));
                    isHead = false;
                }
            } else {
                sb.append(mono.toString(isHead));
            }
        }

        return sb.toString();
    }

    private boolean allZero() {
        for (Mono mono : this.monos) {
            if (!mono.isZero()) {
                return false;
            }
        }
        return true;
    }
}
