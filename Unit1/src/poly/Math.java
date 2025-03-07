package poly;

import java.math.BigInteger;

public class Math {
    

    public static boolean isPowerOfTwoNMultipliedByTwoM(BigInteger num, BigInteger n) {
        // 如果num为0，直接返回false
        if (num.compareTo(BigInteger.ZERO) == 0) {
            return false;
        } else if (num.compareTo(BigInteger.ZERO) < 0){
            num = num.negate();
        }

        // 如果n为负数，直接返回false
        if (n.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }

        // 计算2^n
        BigInteger twoN = BigInteger.valueOf(2).pow(n.intValue());

        // 判断num是否是2^n的倍数
        if (!num.mod(twoN).equals(BigInteger.ZERO)) {
            return false;
        }

        // 计算num / 2^n
        BigInteger quotient = num.divide(twoN);

        // 判断商是否是2的幂
        return quotient.equals(BigInteger.ONE) || isPowerOfTwo(quotient);
    }

    // 判断是否是2的幂
    public static boolean isPowerOfTwo(BigInteger num) {
        if (num.compareTo(BigInteger.ZERO) <= 0) {
            return false;
        }
        return num.and(num.subtract(BigInteger.ONE)).equals(BigInteger.ZERO);
    }
}