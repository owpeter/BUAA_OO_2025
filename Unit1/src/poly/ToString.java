package poly;

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
    
    /**
     * 将单项式转换为字符串
     * @param mono 需要转换的单项式
     * @param isHead 是否是多项式的首项
     * @return 单项式的字符串表示
     */
    public static String monoToString(Mono mono, boolean isHead) {
        // 1. 如果系数为0，直接返回空字符串
        if (mono.getCoe().equals(BigInteger.ZERO)) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // 2. 处理符号
        // 如果不是首项且系数为正数，添加加号
        if (!isHead && mono.getCoe().compareTo(BigInteger.ZERO) > 0) {
            sb.append("+");
        }
        // 负数的符号会在后续处理中自动添加
        
        // 3. 判断是否是特殊情况：系数为1或-1，指数为0，且有三角函数因子
        boolean onlyTrigWithCoeOneOrMinusOne = (mono.getCoe().equals(BigInteger.ONE) || 
                                              mono.getCoe().equals(BigInteger.ONE.negate())) && 
                                              mono.getExp().equals(BigInteger.ZERO) && 
                                              (!mono.getSinMap().isEmpty() || !mono.getCosMap().isEmpty());
        
        // 4. 处理系数和变量部分
        if (!onlyTrigWithCoeOneOrMinusOne) {
            // 4.1 处理常数项（指数为0）
            if (mono.getExp().equals(BigInteger.ZERO)) {
                sb.append(mono.getCoe());
            } 
            // 4.2 处理一次项（指数为1）
            else if (mono.getExp().equals(BigInteger.ONE)) {
                appendCoeWithX(sb, mono);
            }
            // 4.3 处理高次项
            else {
                appendCoeWithX(sb, mono);
                sb.append("^").append(mono.getExp());
            }
        } else if (mono.getCoe().equals(BigInteger.ONE.negate())) {
            // 如果系数为-1，需要添加负号
            sb.append("-");
        }
        
        // 5. 处理三角函数因子
        boolean isFirstFactor = onlyTrigWithCoeOneOrMinusOne;
        
        // 5.1 处理 sin 因子
        for (Map.Entry<Poly, BigInteger> entry : mono.getSinMap().entrySet()) {
            Poly factor = entry.getKey();
            BigInteger power = entry.getValue();
            
            // 添加乘号（如果不是第一个因子）
            if (!isFirstFactor) {
                sb.append("*");
            }
            isFirstFactor = false;
            
            // 添加 sin 函数
            sb.append("sin(");
            
            // 判断是否需要额外的括号
            if (needsExtraParentheses(factor)) {
                sb.append("(").append(polyToString(factor)).append(")");
            } else {
                sb.append(polyToString(factor));
            }
            
            sb.append(")");
            
            // 如果幂次大于1，添加幂次
            if (power.compareTo(BigInteger.ONE) > 0) {
                sb.append("^").append(power);
            }
        }
        
        // 5.2 处理 cos 因子
        for (Map.Entry<Poly, BigInteger> entry : mono.getCosMap().entrySet()) {
            Poly factor = entry.getKey();
            BigInteger power = entry.getValue();
            
            // 添加乘号（如果不是第一个因子）
            if (!isFirstFactor) {
                sb.append("*");
            }
            isFirstFactor = false;
            
            // 添加 cos 函数
            sb.append("cos(");
            
            // 判断是否需要额外的括号
            if (needsExtraParentheses(factor)) {
                sb.append("(").append(polyToString(factor)).append(")");
            } else {
                sb.append(polyToString(factor));
            }
            
            sb.append(")");
            
            // 如果幂次大于1，添加幂次
            if (power.compareTo(BigInteger.ONE) > 0) {
                sb.append("^").append(power);
            }
        }
        
        return sb.toString();
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
            Mono mono = monos.get(0);
            
            // 如果是常数项
            if (mono.getExp().equals(BigInteger.ZERO) && 
                mono.getSinMap().isEmpty() && 
                mono.getCosMap().isEmpty()) {
                return false;
            }
            
            // 如果是单一变量项（如x, x^2等）
            if (mono.getSinMap().isEmpty() && 
                mono.getCosMap().isEmpty()) {
                return false;
            }
        }
        
        // 其他情况需要额外的括号
        return true;
    }
} 