package processString;

public class preProcess {
    private static String input;

    public static String process(String preInput) {
        StringBuilder sb = new StringBuilder();
        StringBuilder preSb = new StringBuilder();
        int n = preInput.length();

        for (int i = 0; i < n; i++) {
            if (preInput.charAt(i) != ' ' && preInput.charAt(i) != '\t') {
                preSb.append(preInput.charAt(i));
            }
        }

        String input = preSb.toString();
        n = input.length();

        if (input.charAt(0) == '+' || input.charAt(0) == '-') {
            // 若开头为符号，加前导0
            sb.append('0');
        }

        for (int i = 0; i < n; i++) {
            if (input.charAt(i) == '+' || input.charAt(i) == '-') {
                // 合并符号
                int sign = input.charAt(i) == '+' ? 1 : input.charAt(i) == '-' ? -1 : 1;
                while (i + 1 < n && (input.charAt(i + 1) == '+' || input.charAt(i + 1) == '-')) {
                    if (input.charAt(i + 1) == '-') {
                        sign *= -1;
                    }
                    i++;
                }
                if (sign == 1) {
                    sb.append("+");
                } else {
                    sb.append("-");
                }
            } else if (input.charAt(i) == '^') {
                // 省略 ^ 后 +
                sb.append(input.charAt(i));
                if (i + 1 < n && input.charAt(i + 1) == '+') {
                    i++;
                }
            } else if (input.charAt(i) == '(' &&
                    (i + 1 < n && (input.charAt(i + 1) == '+' || input.charAt(i + 1) == '-'))) {
                // 检查是否是三角函数内的负数常量
                boolean isTrigConstant = false;
                if (i >= 3) {
                    String prefix = input.substring(i - 3, i);
                    if (prefix.equals("sin") || prefix.equals("cos")) {
                        // 检查后面是否跟着数字，如果是，则是三角函数内的常量
                        if (i + 2 < n && Character.isDigit(input.charAt(i + 2))) {
                            isTrigConstant = true;
                        }
                    }
                }

                sb.append(input.charAt(i));

                // 只有在不是三角函数内的常量时，才添加前导0
                if (!isTrigConstant) {
                    sb.append('0');
                }
            }
            else {
                sb.append(input.charAt(i));
            }
        }
        return sb.toString();
    }
}
