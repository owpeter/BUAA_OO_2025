package poly;

import java.math.BigInteger;

public class Power {
    public static void polyPower(Poly poly, BigInteger exp) {
        Poly oldPoly = poly.copy();
        while (exp.compareTo(BigInteger.ONE) > 0) {
            Multiply.polyMultiply(poly, oldPoly);
            exp = exp.subtract(BigInteger.ONE);
        }
    }
} 