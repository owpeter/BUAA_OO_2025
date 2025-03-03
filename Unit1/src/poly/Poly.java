package poly;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Poly {
    private HashSet<Mono> monos;

    public List<Mono> getMonos() {
        ArrayList<Mono> sortedMonos = new ArrayList<>(this.monos);
        // 把正数项放在前面
        for (int i = 0; i < sortedMonos.size(); i++) {
            if (sortedMonos.get(i).getCoe().compareTo(BigInteger.ZERO) > 0) {
                Mono temp = sortedMonos.get(i);
                sortedMonos.set(i, sortedMonos.get(0));
                sortedMonos.set(0, temp);
            }
        }
        return sortedMonos;
    }

    public void setMonos(List<Mono> monos) {
        this.monos = new HashSet<>(monos);
    }

    public Poly() {
        this.monos = new HashSet<>();
    }

    public void addMono(Mono mono) {
        if (mono.isZero()) {
            return;
        }
        
        Mono existingMono = null;
        for (Mono m : this.monos) {
            if (m.equals(mono)) {
                existingMono = m;
                break;
            }
        }
        
        if (existingMono != null) {
            Add.monoAdd(existingMono, mono);
            if (existingMono.isZero()) {
                this.monos.remove(existingMono);
            }
        } else {
            this.monos.add(mono.copy());
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
        List<Mono> sortedMonos = getMonos();
        for (Mono mono : sortedMonos) {
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
        if (monos.isEmpty()) {
            return true;
        }
        for (Mono mono : monos) {
            if (!mono.isZero()) {
                return false;
            }
        }
        return true;
    }
}
