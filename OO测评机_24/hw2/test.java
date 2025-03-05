import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapComparator {
    public static boolean compareMapsIgnoringTwo(
        HashMap<Poly, BigInteger> map1,
        HashMap<Poly, BigInteger> map2
    ) {
        // 获取所有键的并集
        Set<Poly> allKeys = new HashSet<>();
        allKeys.addAll(map1.keySet());
        allKeys.addAll(map2.keySet());

        for (Poly key : allKeys) {
            BigInteger val1 = map1.get(key);
            BigInteger val2 = map2.get(key);

            // 检查任一值为2则跳过
            boolean isVal1Two = val1 != null && val1.equals(BigInteger.valueOf(2));
            boolean isVal2Two = val2 != null && val2.equals(BigInteger.valueOf(2));
            if (isVal1Two || isVal2Two) {
                continue;
            }

            // 检查键的存在性和值是否一致
            boolean exists1 = map1.containsKey(key);
            boolean exists2 = map2.containsKey(key);
            if (exists1 != exists2) {
                return false; // 一个存在，另一个不存在
            }
            if (exists1 && !val1.equals(val2)) {
                return false; // 值不相等
            }
        }
        return true;
    }
}