package poly;

// import java.math.BigInteger;

public class Add {
    public static void monoAdd(Mono mono1, Mono mono2) {
        mono1.setCoe(mono1.getCoe().add(mono2.getCoe()));

    }

    public static void polyAdd(Poly poly1, Poly poly2) {
        for (Mono mono : poly2.getMonos()) {
            poly1.addMono(mono);
        }
    }
} 