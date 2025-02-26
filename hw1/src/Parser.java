import expr.*;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }



    public Expression parseExpression() {
        Expression expression = new Expression();
        expression.addTerm(parseTerm(true));
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
//            expression.getOperators().add(lexer.peek()); // 可能舍弃
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
        if(this.lexer.peek().equals("(")) {
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
        } else if (this.lexer.peek().equals("x")) {
            // 变量因子
            lexer.next();
            if (this.lexer.peek().equals("^")) {
                // x ^ exp
                lexer.next();
                String exp = lexer.peek();
                lexer.next();// ?
                return new Variable(exp);
            }
            else return new Variable("1");
            // x ^ 1
        } else {
            // 常数因子
            // 只接受一个前导符号
            String constant;
            if (this.lexer.peek().equals("-")) {
                // 负号常数
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
}
