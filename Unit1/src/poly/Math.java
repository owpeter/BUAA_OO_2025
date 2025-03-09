package poly;

import java.math.BigInteger;

public class Math {
    public static boolean isPowerOfTwoNMultipliedByMTwo(BigInteger num, BigInteger n) {
        // 如果num为0，直接返回false
        if (num.compareTo(BigInteger.ZERO) == 0) {
            return false;
        }
        // 创建一个新的变量来存储num的绝对值
        BigInteger absNum = num.compareTo(BigInteger.ZERO) < 0 ? num.negate() : num;
        // 如果n为负数，直接返回false
        if (n.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        // 计算2^n
        BigInteger twoN = BigInteger.valueOf(2).pow(n.intValue());

        return absNum.mod(twoN).equals(BigInteger.ZERO);
    }
}