import expr.*;

import java.math.BigInteger;
import java.util.ArrayList;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parseExpression() {
        Expression expression = new Expression();
        expression.addTerm(parseTerm(true));
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            // 将符号传递给Term
            if (lexer.peek().equals("+")) {
                lexer.next();
                expression.addTerm(parseTerm(true));
            } else {
                lexer.next();
                expression.addTerm(parseTerm(false));
            }
        }
        expression.setExponential("1");
        return expression;
    }

    public Term parseTerm(boolean sign) {
        // 符号存储在Term级
        Term term = new Term(sign);
        term.addFactor(parseFactor());

        while (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        if (this.lexer.peek().equals("(")) {
            // 表达式因子
            lexer.next();
            Expression expr = parseExpression();
            lexer.next();
            if (this.lexer.peek().equals("^")) {
                lexer.next();
                String exp = lexer.peek();
                expr.setExponential(exp);
                lexer.next();
            }
            return expr;
        } else if (this.lexer.peek().equals("sin") || this.lexer.peek().equals("cos")) {
            // 三角函数因子
            String type = lexer.peek();
            lexer.next();  // 跳过sin/cos
            lexer.next();  // 跳过左括号
            Factor factor = parseFactor();
            lexer.next();  // 跳过右括号
            if (this.lexer.peek().equals("^")) {
                lexer.next();
                BigInteger exp = new BigInteger(lexer.peek());
                lexer.next();
                return new TrigonometricFunction(type, factor, exp);
            } else {
                return new TrigonometricFunction(type, factor, BigInteger.ONE);
            }
        } else if (this.lexer.peek().equals("x")) {
            // 变量因子
            lexer.next();
            if (this.lexer.peek().equals("^")) {
                // x ^ exp
                lexer.next();
                String exp = lexer.peek();
                lexer.next();
                return new Variable(exp);
            } else {
                return new Variable("1");
            }
            // x ^ 1
        } else if (this.lexer.peek().equals("f")) {
            // 函数
            String FuncName = "f";
            lexer.next();   // {
            lexer.next();   // n
            Integer n = Integer.parseInt(lexer.peek());
            lexer.next();   // }
            lexer.next();   // (

            ArrayList<Factor> factors = new ArrayList<>();
            for (int i = 0; i < RecursiveFunc.paraLength(FuncName); i++) {
                lexer.next(); // factor
                factors.add(parseExpression());
            }
            lexer.next(); // )
            String funcN = RecursiveFunc.callFunc(FuncName, n, factors);
            Lexer funcLexer = new Lexer(funcN);
            Parser funcParser = new Parser(funcLexer);
            Expression funcExpr = funcParser.parseExpression();
            return funcExpr;
        }
        else {
            // 常数因子
            // 只接受一个前导符号
            String constant;
            if (this.lexer.peek().equals("-")) {
                // 负号常数
                // 通过StringBuilder来处理负号
                StringBuilder sb = new StringBuilder();
                sb.append('-');
                lexer.next();
                sb.append(this.lexer.peek());
                constant = sb.toString();
                lexer.next();
            } else if (this.lexer.peek().equals("+")) {
                // 正号常数
                lexer.next();
                constant = this.lexer.peek();
                lexer.next();
            } else {
                // 无号常数
                constant = this.lexer.peek();
                lexer.next();
            }

            return new Constant(constant);
        }
    }

    public void parseFunc() {

    }
}
