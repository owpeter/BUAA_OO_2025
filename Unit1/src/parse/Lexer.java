package parse;

import procstring.PreProcess;

public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = this.preProcess(input);
        // System.out.println(this.input);
        this.next();
    }

    private String preProcess(String preInput) {
        String input = PreProcess.prePreProcess(preInput);
        return PreProcess.process(input.length(), input);
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }

    public void next() {
        // 先判断当前字符，curToken也是当前字符，pos已经指向下一个字符。
        if (pos >= input.length()) {
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            // 数字
            curToken = getNumber();
        } else if (c == '+' || c == '-' || c == '*' || c == '^' || c == '(' || c == ')') {
            // 运算符
            pos++;
            curToken = String.valueOf(c);
        } else if (c == 'x') {
            // x
            pos++;
            curToken = String.valueOf(c);
        } else if (c == 'd') {
            pos += 2;
            curToken = "dx";
        } else if (c == 's') {
            // sin
            pos += 3;
            curToken = "sin";
        } else if (c == 'c') {
            // cos
            pos += 3;
            curToken = "cos";
        } else {
            // f g h
            pos++;
            curToken = String.valueOf(c);
        }
    }

    public String peek() {
        return this.curToken;
    }
}
