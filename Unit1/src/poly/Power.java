package poly;

import java.math.BigInteger;

public class Power {
    public static void polyPower(Poly poly, BigInteger exp) {
        Poly oldPoly = poly.copy();
        BigInteger i = exp;
        while (i.compareTo(BigInteger.ONE) > 0) {
            Multiply.polyMultiply(poly, oldPoly);
            i = i.subtract(BigInteger.ONE);
        }
    }
} 