package poly;

import expr.Expression;
import parse.Parser;
import parse.Lexer;
import org.junit.Test;

public class DerivateTest {

    @Test
    public void dExpr() {
        Expression expr = new Parser(new Lexer("x^2+x+1")).parseExpression();

    }

    @Test
    public void dTerm() {
    }

    @Test
    public void dFactor() {
    }
}