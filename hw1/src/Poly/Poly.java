package Poly;

import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import Poly.*;

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
    }

    public void addPoly(Poly newPoly) {
        for(Mono mono: newPoly.getMonos()) {
            this.addMono(mono);
        }
    }

    public void multiMono(Mono mono) {
        // (monos) * mono = mono1*mono + mono2 * mono ...
        for (Mono thisMono: this.monos) {
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
