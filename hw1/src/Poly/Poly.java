package Poly;

import java.util.ArrayList;

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

    public void MultiMono(Mono mono) {
        // (monos) * mono = mono1*mono + mono2 * mono ...
        for (Mono thisMono: this.monos) {
            thisMono.MultiMono(mono);
        }
    }

    public void MultiPoly(Poly newPoly) {
        Poly poly = new Poly();
        for (Mono mono1 : this.monos) {
            for (Mono mono2 : newPoly.getMonos()) {
                Mono newMono = mono1.MultiMono(mono1, mono2);
                poly.addMono(newMono);
            }
        }
        this.monos = poly.getMonos();
    }
}
