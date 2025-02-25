import expr.*;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }



    public Expression parseExpression() {
        Expression expression = new Expression();
        expression.addTerm(parseTerm());
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            expression.getOperators().add(lexer.peek());
            lexer.next();
            expression.addTerm(parseTerm());
        }
        expression.setExponential("1");
        return expression;
    }

    public Term parseTerm() {
        Term term = new Term();
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
            String constant = this.lexer.peek();
            lexer.next();
            return new Constant(constant);
        }
    }
}
