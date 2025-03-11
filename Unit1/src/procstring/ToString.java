package procstring;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 用于处理多项式和单项式的字符串表示
 */
public class ToString {
    
    /**
     * 将多项式转换为字符串
     * @param poly 需要转换的多项式
     * @return 多项式的字符串表示
     */
    public static String polyToString(Poly poly) {
        if (allZero(poly)) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        boolean isHead = true;
        List<Mono> sortedMonos = poly.getMonos();
        for (Mono mono : sortedMonos) {
            if (isHead) {
                if (!mono.isZero()) {
                    // 不是前导0
                    sb.append(monoToString(mono, isHead));
                    isHead = false;
                }
            } else {
                sb.append(monoToString(mono, isHead));
            }
        }

        return sb.toString();
    }
    
    /**
     * 判断多项式是否全为0
     * @param poly 需要判断的多项式
     * @return 是否全为0
     */
    private static boolean allZero(Poly poly) {
        if (poly.getMonos().isEmpty()) {
            return true;
        }
        for (Mono mono : poly.getMonos()) {
            if (!mono.isZero()) {
                return false;
            }
        }
        return true;
    }

    private static boolean onlyTrigWithCoeOneOrMinusOne(Mono mono) {
        return (mono.getCoe().equals(BigInteger.ONE) ||
                mono.getCoe().equals(BigInteger.ONE.negate())) &&
                mono.getExp().equals(BigInteger.ZERO) &&
                (!mono.getSinMap().isEmpty() || !mono.getCosMap().isEmpty());
    }

    /**
     * 将单项式转换为字符串
     * @param mono 需要转换的单项式
     * @param isHead 是否是多项式的首项
     * @return 单项式的字符串表示
     */
    public static String monoToString(Mono mono, boolean isHead) {
        if (mono.getCoe().equals(BigInteger.ZERO)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!isHead && mono.getCoe().compareTo(BigInteger.ZERO) > 0) {
            sb.append("+");
        }
        if (!onlyTrigWithCoeOneOrMinusOne(mono)) {
            if (mono.getExp().equals(BigInteger.ZERO)) {
                sb.append(mono.getCoe());
            }
            else if (mono.getExp().equals(BigInteger.ONE)) {
                appendCoeWithX(sb, mono);
            }
            else {
                appendCoeWithX(sb, mono);
                sb.append("^").append(mono.getExp());
            }
        } else if (mono.getCoe().equals(BigInteger.ONE.negate())) {
            sb.append("-");
        }
        StringBuilder newSb = new StringBuilder();
        newSb = trigToString(sb, mono);
        return newSb.toString();
    }

    private static StringBuilder trigToString(StringBuilder sb, Mono mono) {
        boolean isFirstFactor = onlyTrigWithCoeOneOrMinusOne(mono);
        for (Map.Entry<Poly, BigInteger> entry : mono.getSinMap().entrySet()) {


            if (!isFirstFactor) {
                sb.append("*");
            }
            isFirstFactor = false;
            sb.append("sin(");
            Poly factor = entry.getKey();
            if (needsExtraParentheses(factor)) {
                sb.append("(").append(polyToString(factor)).append(")");
            } else {
                sb.append(polyToString(factor));
            }
            sb.append(")");
            BigInteger power = entry.getValue();
            if (power.compareTo(BigInteger.ONE) > 0) {
                sb.append("^").append(power);
            }
        }
        for (Map.Entry<Poly, BigInteger> entry : mono.getCosMap().entrySet()) {


            if (!isFirstFactor) {
                sb.append("*");
            }
            isFirstFactor = false;
            sb.append("cos(");
            Poly factor = entry.getKey();
            BigInteger power = entry.getValue();
            if (needsExtraParentheses(factor)) {
                sb.append("(").append(polyToString(factor)).append(")");
            } else {
                sb.append(polyToString(factor));
            }
            sb.append(")");
            if (power.compareTo(BigInteger.ONE) > 0) {
                sb.append("^").append(power);
            }
        }
        return sb;
    }
    
    /**
     * 辅助方法：添加系数和变量x
     * @param sb 字符串构建器
     * @param mono 单项式
     */
    private static void appendCoeWithX(StringBuilder sb, Mono mono) {
        if (mono.getCoe().equals(BigInteger.ONE)) {
            // 系数为1，只添加x
            sb.append("x");
        } else if (mono.getCoe().equals(BigInteger.ONE.negate())) {
            // 系数为-1，添加-x
            sb.append("-x");
        } else {
            // 其他系数，添加系数*x
            sb.append(mono.getCoe()).append("*x");
        }
    }
    
    /**
     * 判断多项式是否需要额外的括号
     * 如果多项式只有一个单项式，且该单项式是常数或单一变量，则不需要额外的括号
     * 否则需要额外的括号
     * @param poly 需要判断的多项式
     * @return 是否需要额外的括号
     */
    private static boolean needsExtraParentheses(Poly poly) {
        List<Mono> monos = poly.getMonos();
        
        // 如果多项式只有一个单项式
        if (monos.size() == 1) {
            Mono mono1 = monos.get(0);
            if (mono1.getCoe().equals(BigInteger.ZERO)) {
                return false;
            }
            int cnt = coeCnt(mono1) + varCnt(mono1) + mono1.getSinMap().size() + mono1.getCosMap().size();
            return cnt > 1;
        }
        
        // 其他情况需要额外的括号
        return true;
    }

    private static int coeCnt(Mono mono) {
        return mono.getCoe().equals(BigInteger.ONE) ? 0 : 1;
    }

    private static int varCnt(Mono mono) {
        return mono.getExp().equals(BigInteger.ZERO) ? 0 : 1;
    }
} 