import expr.Expression;
import expr.Factor;
import expr.Term;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expression parseExpression() {
        Expression expression = new Expression();
        expression.addTerm(parseTerm());
        return expression;
    }

    public Term parseTerm() {

        Term term = new Term();
        term.addFactor(parseFactor());
        lexer.next();
        if (lexer.peek().equals("*")) {
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        if(this.lexer.peek().equals("(")) {
            lexer.next();
            Expression expr = parseExpression();
            lexer.next();
        }
        // else if ...
    }
}
